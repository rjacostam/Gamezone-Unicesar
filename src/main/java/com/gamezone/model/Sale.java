package com.gamezone.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sale registered in the store.
 * <p>
 * Each entry in the product list is one unit sold, so a product that appears twice was
 * sold twice. The sale owns the product list, which makes it the right place to compute
 * its own total.
 */
public class Sale {

    private String id;
    private LocalDate date;
    private Customer customer;
    private Seller seller;
    private List<Product> products;
    private double total;

    /**
     * Creates a sale and computes its total.
     *
     * @param id       unique sale identifier
     * @param date     date of the sale
     * @param customer customer who buys
     * @param seller   seller who serves the customer
     * @param products units sold, one entry per unit
     */
    public Sale(String id, LocalDate date, Customer customer, Seller seller, List<Product> products) {
        this.id = id;
        this.date = date;
        this.customer = customer;
        this.seller = seller;
        this.products = new ArrayList<>(products);
        this.total = calculateTotal();
    }

    /**
     * Adds up the price of every unit sold and stores the result as the sale total.
     *
     * @return sale total
     */
    public double calculateTotal() {
        double sum = 0;
        for (Product product : products) {
            sum += product.getPrice();
        }
        total = sum;
        return total;
    }

    /**
     * Returns the sale identifier.
     *
     * @return identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the date of the sale.
     *
     * @return date
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Returns the customer of the sale.
     *
     * @return customer
     */
    public Customer getCustomer() {
        return customer;
    }

    /**
     * Returns the seller who registered the sale.
     *
     * @return seller
     */
    public Seller getSeller() {
        return seller;
    }

    /**
     * Returns the units sold.
     *
     * @return read-only product list
     */
    public List<Product> getProducts() {
        return Collections.unmodifiableList(products);
    }

    /**
     * Returns the sale total.
     *
     * @return total
     */
    public double getTotal() {
        return total;
    }
}
