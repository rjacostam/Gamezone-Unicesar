package com.gamezone.service;

import com.gamezone.model.Customer;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.persistence.SaleRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Business rules for sales: validation, stock update and queries.
 */
public class SaleService {
    private SaleRepository repository;
    private ProductService productService;
    private List<Sale> sales;

    /**
     * Creates service.
     * @param repository sale repository
     * @param productService product service for stock
     * @param personService person service for lookups
     */
    public SaleService(SaleRepository repository, ProductService productService, PersonService personService) {
        this.repository = repository;
        this.productService = productService;
        this.sales = new ArrayList<>(repository.loadAll(
            personService::findCustomerById,
            personService::findSellerByCode,
            productService::findById));
    }

    /**
     * Registers a sale validating stock and updating inventory.
     * @param customer customer
     * @param seller seller
     * @param items products to sell
     * @return created sale
     */
    public Sale registerSale(Customer customer, Seller seller, List<Product> items) {
        if (customer == null || seller == null) {
            throw new IllegalArgumentException("Customer and seller are required");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Sale requires at least one product");
        }
        for (Product p : items) {
            Product current = productService.findById(p.getId());
            if (current == null) {
                throw new IllegalArgumentException("Product not found: " + p.getId());
            }
            if (current.getStock() <= 0) {
                throw new IllegalStateException("Insufficient stock for " + p.getId());
            }
        }
        for (Product p : items) {
            productService.decreaseStock(p.getId());
        }
        Sale sale = new Sale(UUID.randomUUID().toString().substring(0, 8),
            LocalDate.now().toString(), customer, seller, items);
        sales.add(sale);
        repository.saveAll(sales);
        return sale;
    }

    /**
     * Lists all sales.
     * @return sales copy
     */
    public List<Sale> findAll() {
        return new ArrayList<>(sales);
    }

    /**
     * Sales by customer.
     * @param nationalId customer id
     * @return matching sales
     */
    public List<Sale> findByCustomer(String nationalId) {
        List<Sale> out = new ArrayList<>();
        for (Sale s : sales) {
            if (s.getCustomer().getNationalId().equalsIgnoreCase(nationalId)) {
                out.add(s);
            }
        }
        return out;
    }

    /**
     * Sales by seller.
     * @param code seller code
     * @return matching sales
     */
    public List<Sale> findBySeller(String code) {
        List<Sale> out = new ArrayList<>();
        for (Sale s : sales) {
            if (s.getSeller().getEmployeeCode().equalsIgnoreCase(code)) {
                out.add(s);
            }
        }
        return out;
    }
}
