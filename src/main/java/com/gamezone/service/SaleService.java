package com.gamezone.service;

import com.gamezone.model.Customer;
import com.gamezone.model.Product;
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
 * It checks the sale rules, creates the sale and asks the product service to update the
 * inventory. It never touches the product or person repositories directly. A rejected
 * operation throws an {@link IllegalArgumentException} with a message in Spanish that the
 * menu can show to the user.
 */
public class SaleService {

    private final SaleRepository repository;
    private final ProductService productService;
    private final PersonService personService;
    private final List<Sale> sales;

    /**
     * Creates the service and loads the stored sales, linking them to the products and
     * people already loaded by the other services.
     *
     * @param repository     repository used to persist sales
     * @param productService service that manages the inventory
     * @param personService  service that manages customers and sellers
     * @throws IllegalStateException if the sales file cannot be read
     */
    public SaleService(SaleRepository repository, ProductService productService, PersonService personService) {
        this.repository = repository;
        this.productService = productService;
        this.personService = personService;
        try {
            this.sales = repository.loadAll(productService.listAllProducts(),
                    personService.listCustomers(), personService.listSellers());
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar el historial de ventas", e);
        }
    }

    /**
     * Registers a sale, discounts the sold units from the inventory and saves it. Every
     * rule is checked before changing anything, so a rejected sale leaves the inventory
     * untouched.
     *
     * @param customerId customer identification
     * @param sellerId   seller identification or employee code
     * @param productIds identifiers of the units sold, one entry per unit
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
            Product product = productService.findById(productId);
            if (product == null) {
                throw new IllegalArgumentException("El producto con identificador " + productId + " no existe.");
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
        for (Map.Entry<Product, Integer> entry : unitsByProduct.entrySet()) {
            productService.updateStock(entry.getKey().getId(), entry.getValue());
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
