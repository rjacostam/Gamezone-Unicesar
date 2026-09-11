package com.gamezone.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Sale transaction with customer, seller and products.
 * Total is derived from items to keep inventory consistent.
 */
public class Sale {
    private String id;
    private String date;
    private Customer customer;
    private Seller seller;
    private List<Product> products;

    /**
     * Creates a sale. Requires at least one product.
     * @param id sale id
     * @param date date as text
     * @param customer customer
     * @param seller seller
     * @param products products (1..*)
     */
    public Sale(String id, String date, Customer customer, Seller seller, List<Product> products) {
        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Sale requires at least one product");
        }
        this.id = id;
        this.date = date;
        this.customer = customer;
        this.seller = seller;
        this.products = new ArrayList<>(products);
    }

    /** @return id */
    public String getId() { return id; }
    /** @return date */
    public String getDate() { return date; }
    /** @return customer */
    public Customer getCustomer() { return customer; }
    /** @return seller */
    public Seller getSeller() { return seller; }
    /** @return products copy */
    public List<Product> getProducts() { return new ArrayList<>(products); }

    /**
     * Calculates total by summing product prices.
     * @return total amount
     */
    public double calculateTotal() {
        double total = 0;
        for (Product p : products) {
            total += p.getPrice();
        }
        return total;
    }
}
