package com.gamezone.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Venta con cliente, vendedor y productos.
 */
public class Sale {
    private String id;
    private String date;
    private Customer customer;
    private Seller seller;
    private List<Product> products;

    /**
     * Crea una venta, exige al menos un producto.
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
     * Suma precios para sacar el total.
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
