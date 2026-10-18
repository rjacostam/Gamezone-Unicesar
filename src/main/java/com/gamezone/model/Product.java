package com.gamezone.model;

/**
 * Base class for every item the store sells. It holds the data and the inventory
 * behaviour shared by all product types, and declares the description method
 * that each concrete product type must implement on its own.
 */
public abstract class Product {
    private String id;
    private String title;
    private double price;
    private int stock;

    /**
     * Creates a product with its common attributes.
     * @param id product identifier
     * @param title product title
     * @param price unit price
     * @param stock available stock
     */
    protected Product(String id, String title, double price, int stock) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.stock = stock;
    }

    /**
     * Builds the full description of this product, including the attributes
     * that are specific to its type.
     * @return description ready to be shown to the user
     */
    public abstract String getDescription();

    /**
     * Returns the category this product belongs to, used by the promotions that
     * apply a discount only to one kind of product. Each concrete product type
     * reports its own category, and Accessory answers for all its subclasses.
     * @return VIDEOGAME, CONSOLE or ACCESSORY
     */
    public abstract String getCategory();

    /**
     * Tells whether the store has enough units to sell the requested quantity.
     * A negative quantity is never valid, so it is always rejected.
     * @param quantity units the store wants to sell
     * @return true when the quantity is valid and the available stock covers it
     */
    public boolean hasStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        return stock >= quantity;
    }

    /**
     * Removes the sold units from the available stock. The sale is rejected
     * when the quantity is negative or the stock is not enough.
     * @param quantity units being sold
     */
    public void decreaseStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        if (!hasStock(quantity)) {
            throw new IllegalArgumentException("No hay stock suficiente para el producto " + id);
        }
        this.stock -= quantity;
    }

    /**
     * Returns the identifier of this product.
     * @return product identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the name of this product.
     * @return product title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Changes the name of this product.
     * @param title new product title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the selling price of one unit.
     * @return unit price
     */
    public double getPrice() {
        return price;
    }

    /**
     * Changes the selling price of one unit.
     * @param price new unit price
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Returns how many units are available for sale.
     * @return available stock
     */
    public int getStock() {
        return stock;
    }

    /**
     * Sets how many units are available for sale.
     * @param stock new available stock
     */
    public void setStock(int stock) {
        this.stock = stock;
    }

    /**
     * Builds a single line with the common data of the product, used when the
     * catalogue is listed.
     * @return summary line of this product
     */
    @Override
    public String toString() {
        return id + " - " + title + " - $" + price + " - Stock: " + stock;
    }
}
