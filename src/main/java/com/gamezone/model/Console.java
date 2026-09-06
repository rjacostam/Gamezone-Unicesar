package com.gamezone.model;

/**
 * Consola de la tienda.
 */
public class Console extends Product {
    private String brand;
    private String model;
    private String generation;

    /**
     * Crea una consola nueva.
     * @param id id
     * @param title title
     * @param price price
     * @param stock stock
     * @param brand brand
     * @param model model
     * @param generation generation
     */
    public Console(String id, String title, double price, int stock,
                   String brand, String model, String generation) {
        super(id, title, price, stock);
        this.brand = brand;
        this.model = model;
        this.generation = generation;
    }

    /** @return brand */
    public String getBrand() { return brand; }
    /** @return model */
    public String getModel() { return model; }
    /** @return generation */
    public String getGeneration() { return generation; }

    /**
     * Full console description.
     * @return description
     */
    @Override
    public String getFullDescription() {
        return getId() + " | " + getTitle() + " | $" + getPrice()
            + " | stock:" + getStock() + " | " + brand + " " + model + " (" + generation + ")";
    }
}
