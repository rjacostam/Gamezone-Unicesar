package com.gamezone.model;

import com.gamezone.format.AmountFormat;

import java.util.Arrays;
import java.util.List;

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
                + " | Precio: " + AmountFormat.moneyInComputerLocale(getPrice())
                + " | Stock: " + getStock();
    }

    /**
     * The category a console reports, and the single answer to the question
     * "is this product a console". Both {@link #getCategory()} and
     * {@link #isConsole()} read this constant, so a console cannot be one thing
     * and the other, and a change of the category moves both at once.
     */
    public static final String CATEGORY = "CONSOLE";

    /**
     * Returns the category of this product, so a promotion can target consoles
     * only.
     * @return {@link #CATEGORY}
     */
    @Override
    public String getCategory() {
        return CATEGORY;
    }

    /**
     * Tells whether this product is a console. The answer is read from the
     * category this product reports instead of being written by hand, so the two
     * answers to that question can never disagree.
     * @return true, because this product reports the console category
     */
    @Override
    public boolean isConsole() {
        return CATEGORY.equals(getCategory());
    }

    /**
     * Returns the three columns that only a console has, in the order the
     * catalogue file expects them.
     * @return brand, model and generation of this console
     */
    @Override
    public List<String> getExtraFields() {
        return Arrays.asList(brand, model, generation);
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
