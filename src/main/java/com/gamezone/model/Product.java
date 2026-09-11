package com.gamezone.model;

/**
 * Base class for store products with inventory control.
 */
public abstract class Product {
    private String id;
    private String title;
    private double price;
    private int stock;

    /**
     * Creates a product.
     * @param id identifier
     * @param title title
     * @param price price
     * @param stock available stock
     */
    public Product(String id, String title, double price, int stock) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.stock = stock;
    }

    /** @return id */
    public String getId() { return id; }
    /** @return title */
    public String getTitle() { return title; }
    /** @return price */
    public double getPrice() { return price; }
    /** @param price price */
    public void setPrice(double price) { this.price = price; }
    /** @return stock */
    public int getStock() { return stock; }
    /** @param stock stock */
    public void setStock(int stock) { this.stock = stock; }

    /**
     * Full description including subclass details.
     * @return description
     */
    public abstract String getFullDescription();
}

