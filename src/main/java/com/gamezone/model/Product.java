package com.gamezone.model;

/**
 * Clase base de productos con inventario.
 */
public abstract class Product {
    private String id;
    private String title;
    private double price;
    private int stock;

    /**
     * Crea un producto base.
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
