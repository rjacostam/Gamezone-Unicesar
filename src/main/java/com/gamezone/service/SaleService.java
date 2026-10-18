package com.gamezone.service;

import com.gamezone.model.Accessory;
import com.gamezone.model.Console;
import com.gamezone.model.Customer;
import com.gamezone.model.Product;
import com.gamezone.model.Promotion;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.model.Warranty;
import com.gamezone.persistence.SaleRepository;

import java.io.IOException;
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
 * module's repository. A rejected
 * operation throws an {@link IllegalArgumentException} with a message in Spanish that the
 * menu can show to the user.
 */
public class SaleService {

    private final SaleRepository repository;
    private final ProductService productService;
    private final PersonService personService;
    private final AccessoryService accessoryService;
    private final PromotionService promotionService;
    private final WarrantyService warrantyService;
    private final List<Sale> sales;

    /**
     * Creates the service and loads the stored sales, linking them to the products,
     * accessories and people already loaded by the other services.
     *
     * @param repository       repository used to persist sales
     * @param productService   service that manages video games and consoles
     * @param personService    service that manages customers and sellers
     * @param accessoryService service that manages accessories
     * @param promotionService service that chooses the promotion of each sale
     * @param warrantyService  service that creates the warranties of the consoles sold
     * @throws IllegalStateException if the sales file cannot be read
     */
    public SaleService(SaleRepository repository, ProductService productService, PersonService personService,
                       AccessoryService accessoryService, PromotionService promotionService,
                       WarrantyService warrantyService) {
        this.repository = repository;
        this.productService = productService;
        this.personService = personService;
        this.accessoryService = accessoryService;
        this.promotionService = promotionService;
        this.warrantyService = warrantyService;
        try {
            this.sales = repository.loadAll(getCatalog(),
                    personService.listCustomers(), personService.listSellers());
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar el historial de ventas", e);
        }
    }

    /**
     * Registers a sale, applies the best active promotion, assigns a basic warranty to
     * every console and the requested extended warranties, discounts the sold units from
     * the inventory and saves it. The items can be video games, consoles or accessories.
     * Every rule is checked before changing anything, so a rejected sale leaves the
     * inventory untouched.
     *
     * @param customerId customer identification
     * @param sellerId   seller identification or employee code
     * @param productIds                     identifiers of the units sold (products or
     *                                       accessories), one entry per unit
     * @param productIdsWithExtendedWarranty consoles of the sale that get an extended
     *                                       warranty; empty or {@code null} for none
     * @return the registered sale
     * @throws IllegalArgumentException if there are no products, if the customer, the
     *                                  seller or a product does not exist, if there is not
     *                                  enough stock, or if an extended warranty is requested
     *                                  for an item that is not a console of the sale
     */
    public Sale registerSale(String customerId, String sellerId, List<String> productIds,
                             List<String> productIdsWithExtendedWarranty) {
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("La venta debe contener al menos un producto.");
        }
        Customer customer = requireCustomer(customerId);
        Seller seller = requireSeller(sellerId);

        List<Product> soldProducts = resolveItems(productIds);
        Map<Product, Integer> unitsByProduct = countUnits(soldProducts);
        checkStock(unitsByProduct);
        List<String> extendedRequests = validateExtendedRequests(soldProducts, productIdsWithExtendedWarranty);

        Sale sale = new Sale(nextSaleId(), LocalDate.now(), customer, seller, soldProducts);
        applyBestPromotion(sale);
        assignWarranties(sale, extendedRequests);
        for (Map.Entry<Product, Integer> entry : unitsByProduct.entrySet()) {
            if (entry.getKey() instanceof Accessory) {
                accessoryService.updateStock(entry.getKey().getId(), entry.getValue());
            } else {
                productService.updateStock(entry.getKey().getId(), entry.getValue());
            }
        }
        sales.add(sale);
        saveSales();
        return sale;
    }

    /**
     * Returns every registered sale.
     *
     * @return copy of the sale list
     */
    public List<Sale> listAllSales() {
        return new ArrayList<>(sales);
    }

    /**
     * Looks for a sale by its identifier, ignoring letter case.
     *
     * @param saleId sale identifier
     * @return the sale, or {@code null} if it does not exist
     */
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
     * Returns every item that can be sold: video games, consoles and accessories.
     *
     * @return products followed by accessories
     */
    public List<Product> getCatalog() {
        List<Product> catalog = new ArrayList<>(productService.listAllProducts());
        catalog.addAll(accessoryService.listAllAccessories());
        return catalog;
    }

    private Customer requireCustomer(String customerId) {
        Customer customer = personService.findCustomerById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("El cliente con identificación " + customerId + " no existe.");
        }
        return customer;
    }

    private Seller requireSeller(String sellerId) {
        Seller seller = personService.findSellerById(sellerId);
        if (seller == null) {
            throw new IllegalArgumentException("El vendedor " + sellerId + " no existe.");
        }
        return seller;
    }

    private List<Product> resolveItems(List<String> productIds) {
        List<Product> items = new ArrayList<>();
        for (String productId : productIds) {
            Product product = findItem(productId);
            if (product == null) {
                throw new IllegalArgumentException(
                        "El producto o accesorio con identificador " + productId + " no existe.");
            }
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

    private void checkStock(Map<Product, Integer> unitsByProduct) {
        for (Map.Entry<Product, Integer> entry : unitsByProduct.entrySet()) {
            if (!entry.getKey().hasStock(entry.getValue())) {
                throw new IllegalArgumentException("Stock insuficiente para " + entry.getKey().getTitle()
                        + ". Disponible: " + entry.getKey().getStock() + ", solicitado: " + entry.getValue() + ".");
            }
        }
    }

    private List<String> validateExtendedRequests(List<Product> items, List<String> productIdsWithExtendedWarranty) {
        List<String> extendedRequests = productIdsWithExtendedWarranty == null
                ? new ArrayList<>() : productIdsWithExtendedWarranty;
        for (String requestedId : extendedRequests) {
            if (!containsConsole(items, requestedId)) {
                throw new IllegalArgumentException(
                        "La garantía extendida solo aplica a consolas incluidas en la venta: " + requestedId + ".");
            }
        }
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
            if (product instanceof Console) {
                warrantyService.assignBasicWarranty(product, sale, sale.getDate());
                if (containsIgnoreCase(extendedRequests, product.getId())) {
                    Warranty extended = warrantyService.assignExtendedWarranty(product, sale, sale.getDate());
                    sale.addExtendedWarrantyCost(extended.getAdditionalCost());
                }
            }
        }
    }

    private boolean containsConsole(List<Product> products, String id) {
        for (Product product : products) {
            if (product instanceof Console && product.getId().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsIgnoreCase(List<String> values, String value) {
        for (String candidate : values) {
            if (candidate.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    private Product findItem(String id) {
        Product product = productService.findById(id);
        if (product == null) {
            product = accessoryService.findById(id);
        }
        return product;
    }

    private void saveSales() {
        try {
            repository.saveAll(sales);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar el historial de ventas", e);
        }
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
