package com.gamezone.service;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.PersistenceException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Customer;
import com.gamezone.model.Product;
import com.gamezone.model.Promotion;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.model.Warranty;
import com.gamezone.persistence.SaleRepository;
import com.gamezone.validation.PersonValidator;
import com.gamezone.validation.ProductValidator;
import com.gamezone.validation.SaleValidator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Business rules for sales.
 * <p>
 * It checks the sale rules, creates the sale with items from the product and accessory
 * catalogs, applies the best active promotion, assigns the warranties of the consoles, and
 * asks the service that owns each item to update the inventory. It never touches another
 * module's repository.
 * <p>
 * File errors do not reach the caller as a checked exception. The repository
 * already turns them into a {@link PersistenceException}, because a failure of
 * the disk is a failure of the application and not a mistake of the user.
 * <p>
 * A rejected operation throws an {@link InvalidDataException} for a bad value, a
 * {@link ResourceNotFoundException} when the customer, the seller or a product of
 * the sale does not exist, or a {@link BusinessRuleException} when a rule of the
 * store is broken, always with a message in Spanish that the menu can show to
 * the user.
 */
public class SaleService implements SaleFinder {

    private final SaleRepository repository;
    private final StoreCatalog catalog;
    private final ProductService productService;
    private final PersonService personService;
    private final AccessoryService accessoryService;
    private final PromotionService promotionService;
    private final WarrantyService warrantyService;
    private final List<Sale> sales;
    /** Catalogues that own the stock of the items of a sale, in search order. */
    private final List<InventoryCatalog> inventoryCatalogs;

    /**
     * Creates the service and loads the stored sales, linking them to the products,
     * accessories and people already loaded by the other services.
     *
     * @param repository       repository used to persist sales
     * @param catalog          catalogue of everything the store sells
     * @param productService   service that manages video games and consoles
     * @param personService    service that manages customers and sellers
     * @param accessoryService service that manages accessories
     * @param promotionService service that chooses the promotion of each sale
     * @param warrantyService  service that creates the warranties of the consoles sold
     * @throws PersistenceException if the sales file cannot be read
     */
    public SaleService(SaleRepository repository, StoreCatalog catalog, ProductService productService,
                       PersonService personService,
                       AccessoryService accessoryService, PromotionService promotionService,
                       WarrantyService warrantyService) {
        this.repository = repository;
        this.catalog = catalog;
        this.productService = productService;
        this.personService = personService;
        this.accessoryService = accessoryService;
        this.inventoryCatalogs = List.of(productService, accessoryService);
        this.promotionService = promotionService;
        this.warrantyService = warrantyService;
        this.sales = repository.loadAll(catalog.listAll(),
                personService.listCustomers(), personService.listSellers());
    }

    /**
     * Registers a sale following the integrated flow, always in this order:
     * <ol>
     *   <li>check that the sale has at least one item, a customer and a seller;</li>
     *   <li>resolve every item as a product or an accessory and check its stock;</li>
     *   <li>create the sale, which computes its subtotal;</li>
     *   <li>apply the best active promotion, computed only on the subtotal;</li>
     *   <li>assign the basic warranty of every console and the requested extended ones;</li>
     *   <li>leave the total as subtotal - discount + extended warranty cost;</li>
     *   <li>update the inventory through the service that owns each item;</li>
     *   <li>save the sale (each warranty is saved when it is assigned).</li>
     * </ol>
     * Every rule is checked in steps 1 and 2, before changing anything, so a rejected sale
     * leaves the inventory and the warranties untouched.
     *
     * @param customerId customer identification
     * @param sellerId   seller identification or employee code
     * @param productIds                     identifiers of the units sold (products or
     *                                       accessories), one entry per unit
     * @param productIdsWithExtendedWarranty consoles of the sale that get an extended
     *                                       warranty; empty or {@code null} for none
     * @return the registered sale
     * @throws InvalidDataException      if the customer or the seller are missing
     * @throws BusinessRuleException     if there are no products, if there is not
     *                                   enough stock, or if an extended warranty is
     *                                   requested for an item that is not a
     *                                   console of the sale
     * @throws ResourceNotFoundException if the customer, the seller or a product
     *                                   does not exist
     * @throws PersistenceException      if the sales file cannot be written
     */
    public Sale registerSale(String customerId, String sellerId, List<String> productIds,
                             List<String> productIdsWithExtendedWarranty) {
        // 1. The sale needs an existing customer and an existing seller.
        Customer customer = requireCustomer(customerId);
        Seller seller = requireSeller(sellerId);

        // 2. Resolve every item as a product or an accessory, so the validator
        //    receives the objects and not only their identifiers.
        List<Product> soldProducts = resolveItems(productIds);

        // 3. The sale needs at least one item.
        SaleValidator.validateSaleData(customer, seller, soldProducts);

        // 4. Check the stock of every item.
        Map<Product, Integer> unitsByProduct = countUnits(soldProducts);
        checkStock(unitsByProduct);
        List<String> extendedRequests = validateExtendedRequests(soldProducts, productIdsWithExtendedWarranty);

        // 5. Create the sale, which computes its subtotal.
        Sale sale = new Sale(nextSaleId(), LocalDate.now(), customer, seller, soldProducts);

        // 6. Apply the best active promotion; its discount is computed only on the subtotal.
        applyBestPromotion(sale);

        // 7. Assign the basic warranty of every console and the requested extended warranties.
        assignWarranties(sale, extendedRequests);
        // 8. Inside that call, the sale adds each extended warranty cost:
        //    total = subtotal - discount + warranties.

        // 9. Update the inventory through the service that owns each item.
        updateInventory(unitsByProduct);

        // 10. Save the sale (WarrantyService already saved each warranty when it was assigned).
        sales.add(sale);
        saveSales();
        return sale;
    }

    /**
     * Returns every registered sale.
     *
     * @return copy of the sale list
     */
    @Override
    public List<Sale> listAllSales() {
        return new ArrayList<>(sales);
    }

    /**
     * Looks for a sale by its identifier, ignoring letter case.
     *
     * @param saleId sale identifier
     * @return the sale, or {@code null} if it does not exist
     */
    @Override
    public Sale findById(String saleId) {
        for (Sale sale : sales) {
            if (sale.getId().equalsIgnoreCase(saleId)) {
                return sale;
            }
        }
        return null;
    }

    /**
     * Returns the purchase history of a customer.
     *
     * @param customerId customer identification
     * @return sales made to that customer, empty if there are none
     */
    public List<Sale> findSalesByCustomer(String customerId) {
        List<Sale> result = new ArrayList<>();
        for (Sale sale : sales) {
            if (sale.getCustomer().getId().equals(customerId)) {
                result.add(sale);
            }
        }
        return result;
    }

    /**
     * Returns the sales served by a seller.
     *
     * @param sellerId seller identification or employee code
     * @return sales registered by that seller, empty if the seller does not exist
     */
    public List<Sale> findSalesBySeller(String sellerId) {
        Seller seller = personService.findSellerById(sellerId);
        List<Sale> result = new ArrayList<>();
        if (seller == null) {
            return result;
        }
        for (Sale sale : sales) {
            if (sale.getSeller().getId().equals(seller.getId())) {
                result.add(sale);
            }
        }
        return result;
    }

    /**
     * Looks for the customer of the sale and makes sure it exists.
     *
     * @param customerId customer identification
     * @return the customer found
     * @throws ResourceNotFoundException if no customer uses that identification
     */
    private Customer requireCustomer(String customerId) {
        Customer customer = personService.findCustomerById(customerId);
        PersonValidator.validatePersonExists(customer, customerId);
        return customer;
    }

    /**
     * Looks for the seller of the sale and makes sure it exists.
     *
     * @param sellerId seller identification or employee code
     * @return the seller found
     * @throws ResourceNotFoundException if no seller uses that identification
     */
    private Seller requireSeller(String sellerId) {
        Seller seller = personService.findSellerById(sellerId);
        PersonValidator.validatePersonExists(seller, sellerId);
        return seller;
    }

    /**
     * Turns the requested identifiers into the items they name, one entry per
     * unit, and makes sure every one of them exists.
     *
     * @param productIds identifiers of the units sold
     * @return the items sold, in the order they were requested
     * @throws ResourceNotFoundException if an item does not exist in the product
     *                                   catalog or in the accessory catalog
     */
    private List<Product> resolveItems(List<String> productIds) {
        List<Product> items = new ArrayList<>();
        for (String productId : productIds) {
            Product product = catalog.findById(productId);
            ProductValidator.validateProductExists(product, productId);
            items.add(product);
        }
        return items;
    }

    private Map<Product, Integer> countUnits(List<Product> items) {
        Map<Product, Integer> unitsByProduct = new LinkedHashMap<>();
        for (Product item : items) {
            unitsByProduct.merge(item, 1, Integer::sum);
        }
        return unitsByProduct;
    }

    /**
     * Checks that the store has enough units of every product that the sale
     * takes.
     *
     * @param unitsByProduct units sold per product
     * @throws BusinessRuleException if the stock of a product is not enough
     */
    private void checkStock(Map<Product, Integer> unitsByProduct) {
        for (Map.Entry<Product, Integer> entry : unitsByProduct.entrySet()) {
            ProductValidator.validateStockAvailability(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Checks that every identifier that asked for an extended warranty names a
     * console of the sale.
     *
     * @param items                         units of the sale
     * @param productIdsWithExtendedWarranty identifiers asked for the extended warranty
     * @return the same identifiers, ready to be used
     * @throws BusinessRuleException if an identifier is not a console of the sale
     */
    private List<String> validateExtendedRequests(List<Product> items,
                                                 List<String> productIdsWithExtendedWarranty) {
        List<String> extendedRequests = productIdsWithExtendedWarranty == null
                ? new ArrayList<>() : productIdsWithExtendedWarranty;
        SaleValidator.validateExtendedWarrantyRequest(items, extendedRequests);
        return extendedRequests;
    }

    private void applyBestPromotion(Sale sale) {
        Promotion promotion = promotionService.findBestPromotionFor(sale);
        if (promotion != null) {
            sale.applyDiscount(promotion.getName(), promotion.calculateDiscount(sale));
        }
    }

    private void assignWarranties(Sale sale, List<String> extendedRequests) {
        for (Product product : sale.getProducts()) {
            if (product.isConsole()) {
                warrantyService.assignBasicWarranty(product, sale, sale.getDate());
                if (containsIgnoreCase(extendedRequests, product.getId())) {
                    Warranty extended = warrantyService.assignExtendedWarranty(product, sale, sale.getDate());
                    sale.addExtendedWarrantyCost(extended.getAdditionalCost());
                }
            }
        }
    }

    private void updateInventory(Map<Product, Integer> unitsByProduct) {
        for (Map.Entry<Product, Integer> entry : unitsByProduct.entrySet()) {
            catalogOf(entry.getKey()).updateStock(entry.getKey().getId(), entry.getValue());
        }
    }

    /**
     * Finds the catalogue that owns an item, asking each catalogue for it instead of
     * testing the concrete type of the item.
     *
     * @param item product or accessory of the sale
     * @return the catalogue that holds that same item; the product catalogue if none does
     */
    private InventoryCatalog catalogOf(Product item) {
        for (InventoryCatalog catalog : inventoryCatalogs) {
            if (catalog.findById(item.getId()) == item) {
                return catalog;
            }
        }
        return productService;
    }

    private boolean containsIgnoreCase(List<String> values, String value) {
        for (String candidate : values) {
            if (candidate.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Saves the sales currently registered.
     *
     * @throws PersistenceException if the sales file cannot be written
     */
    private void saveSales() {
        repository.saveAll(sales);
    }

    private String nextSaleId() {
        int max = 0;
        for (Sale sale : sales) {
            int number = Integer.parseInt(sale.getId().substring(1));
            max = Math.max(max, number);
        }
        return String.format("S%04d", max + 1);
    }
}
