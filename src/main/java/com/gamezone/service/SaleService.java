package com.gamezone.service;

import com.gamezone.model.Accessory;
import com.gamezone.model.Customer;
import com.gamezone.model.Product;
import com.gamezone.model.Promotion;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
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
 * catalogs, applies the best active promotion, and asks the service that owns each item
 * to update the inventory. It never touches another module's repository. A rejected
 * operation throws an {@link IllegalArgumentException} with a message in Spanish that the
 * menu can show to the user.
 */
public class SaleService {

    private final SaleRepository repository;
    private final ProductService productService;
    private final PersonService personService;
    private final AccessoryService accessoryService;
    private final PromotionService promotionService;
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
     * @throws IllegalStateException if the sales file cannot be read
     */
    public SaleService(SaleRepository repository, ProductService productService, PersonService personService,
                       AccessoryService accessoryService, PromotionService promotionService) {
        this.repository = repository;
        this.productService = productService;
        this.personService = personService;
        this.accessoryService = accessoryService;
        this.promotionService = promotionService;
        try {
            this.sales = repository.loadAll(getCatalog(),
                    personService.listCustomers(), personService.listSellers());
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar el historial de ventas", e);
        }
    }

    /**
     * Registers a sale, applies the best active promotion, discounts the sold units from
     * the inventory and saves it. The items can be video games, consoles or accessories.
     * Every rule is checked before changing anything, so a rejected sale leaves the
     * inventory untouched.
     *
     * @param customerId customer identification
     * @param sellerId   seller identification or employee code
     * @param productIds identifiers of the units sold (products or accessories), one entry
     *                   per unit
     * @return the registered sale
     * @throws IllegalArgumentException if there are no products, if the customer, the
     *                                  seller or a product does not exist, or if there is
     *                                  not enough stock
     */
    public Sale registerSale(String customerId, String sellerId, List<String> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("La venta debe contener al menos un producto.");
        }
        Customer customer = personService.findCustomerById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("El cliente con identificación " + customerId + " no existe.");
        }
        Seller seller = personService.findSellerById(sellerId);
        if (seller == null) {
            throw new IllegalArgumentException("El vendedor " + sellerId + " no existe.");
        }

        Map<Product, Integer> unitsByProduct = new LinkedHashMap<>();
        List<Product> soldProducts = new ArrayList<>();
        for (String productId : productIds) {
            Product product = findItem(productId);
            if (product == null) {
                throw new IllegalArgumentException(
                        "El producto o accesorio con identificador " + productId + " no existe.");
            }
            unitsByProduct.merge(product, 1, Integer::sum);
            soldProducts.add(product);
        }
        for (Map.Entry<Product, Integer> entry : unitsByProduct.entrySet()) {
            if (!entry.getKey().hasStock(entry.getValue())) {
                throw new IllegalArgumentException("Stock insuficiente para " + entry.getKey().getTitle()
                        + ". Disponible: " + entry.getKey().getStock() + ", solicitado: " + entry.getValue() + ".");
            }
        }

        Sale sale = new Sale(nextSaleId(), LocalDate.now(), customer, seller, soldProducts);
        Promotion promotion = promotionService.findBestPromotionFor(sale);
        if (promotion != null) {
            sale.applyDiscount(promotion.getName(), promotion.calculateDiscount(sale));
        }
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
