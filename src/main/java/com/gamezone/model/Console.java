package com.gamezone.model;

/**
 * Game console sold by the store. On top of the data inherited from Product it
 * keeps its brand, its model name and the generation it belongs to.
 */
public class Console extends Product {
    private String brand;
    private String model;
    private String generation;

    /**
     * Creates a console with its own attributes.
     * @param id product identifier
     * @param title product title
     * @param price unit price
     * @param stock available stock
     * @param brand brand that manufactures the console
     * @param model model name of the console
     * @param generation generation of the console
     */
    public Console(String id, String title, double price, int stock,
                   String brand, String model, String generation) {
        super(id, title, price, stock);
        this.brand = brand;
        this.model = model;
        this.generation = generation;
    }

    /**
     * Builds the description of this console with its own attributes next to the
     * common data. The price is shown formatted with thousands separators and
     * two decimals so every product line looks the same in the menu.
     * @return description ready to be shown to the user
     */
    @Override
    public String getDescription() {
        return "Consola [" + getId() + "] " + getTitle()
                + " | Marca: " + brand
                + " | Modelo: " + model
                + " | Generacion: " + generation
                + " | Precio: " + String.format("$%,.2f", getPrice())
                + " | Stock: " + getStock();
    }

    /**
     * Returns the category of this product, so a promotion can target consoles
     * only.
     * @return CONSOLE
     */
    @Override
    public String getCategory() {
        return "CONSOLE";
    }

    /**
     * Returns the brand that manufactures this console.
     * @return brand of the console
     */
    public String getBrand() {
        return brand;
    }

    /**
     * Changes the brand that manufactures this console.
     * @param brand new brand
     */
    public void setBrand(String brand) {
        this.brand = brand;
    }

    /**
     * Returns the model name of this console.
     * @return model of the console
     */
    public String getModel() {
        return model;
    }

    /**
     * Changes the model name of this console.
     * @param model new model name
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * Returns the generation this console belongs to.
     * @return generation of the console
     */
    public String getGeneration() {
        return generation;
    }

    /**
     * Changes the generation this console belongs to.
     * @param generation new generation
     */
    public void setGeneration(String generation) {
        this.generation = generation;
    }
}
