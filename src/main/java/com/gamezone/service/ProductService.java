package com.gamezone.service;

import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.model.VideoGame;
import com.gamezone.persistence.ProductRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Business rules of the product catalogue. It loads the catalogue when the
 * service is created, rejects duplicated identifiers, and saves the catalogue
 * after every change so the data survives between executions.
 */
public class ProductService {
    private ProductRepository repository;
    private List<Product> products;

    /**
     * Creates the service and loads the catalogue stored in the repository.
     * @param repository repository used to read and write the catalogue
     */
    public ProductService(ProductRepository repository) {
        this.repository = repository;
        try {
            this.products = new ArrayList<>(repository.loadAll());
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar el catalogo de productos", e);
        }
    }

    /**
     * Registers a new video game in the catalogue. The identifier must not be
     * used by another product already registered.
     * @param id product identifier
     * @param title product title
     * @param price unit price
     * @param stock available stock
     * @param platform platform the game runs on
     * @param genre genre of the game
     * @param ageRating recommended age rating
     * @return the registered video game
     */
    public VideoGame registerVideoGame(String id, String title, double price, int stock,
                                       String platform, String genre, String ageRating) {
        if (findById(id) != null) {
            throw new IllegalArgumentException("Ya existe un producto con el identificador " + id);
        }
        VideoGame game = new VideoGame(id, title, price, stock, platform, genre, ageRating);
        products.add(game);
        saveCatalogue();
        return game;
    }

    /**
     * Registers a new console in the catalogue. The identifier must not be used
     * by another product already registered.
     * @param id product identifier
     * @param title product title
     * @param price unit price
     * @param stock available stock
     * @param brand brand that manufactures the console
     * @param model model name of the console
     * @param generation generation of the console
     * @return the registered console
     */
    public Console registerConsole(String id, String title, double price, int stock,
                                   String brand, String model, String generation) {
        if (findById(id) != null) {
            throw new IllegalArgumentException("Ya existe un producto con el identificador " + id);
        }
        Console console = new Console(id, title, price, stock, brand, model, generation);
        products.add(console);
        saveCatalogue();
        return console;
    }

    /**
     * Returns a copy of the whole catalogue, in registration order.
     * @return products currently registered
     */
    public List<Product> listAllProducts() {
        return new ArrayList<>(products);
    }

    /**
     * Searches a product by its identifier.
     * @param id identifier to look for
     * @return the product found, or null when no product uses that identifier
     */
    public Product findById(String id) {
        for (Product product : products) {
            if (product.getId().equals(id)) {
                return product;
            }
        }
        return null;
    }

    /**
     * Updates the available stock of a product and saves the catalogue. The
     * product itself rejects the update when the stock is not enough.
     * @param productId identifier of the product to update
     * @param quantity amount of units to remove from the stock
     */
    public void updateStock(String productId, int quantity) {
        Product product = findById(productId);
        if (product == null) {
            throw new IllegalArgumentException(
                    "No existe un producto con el identificador " + productId);
        }
        product.decreaseStock(quantity);
        saveCatalogue();
    }

    private void saveCatalogue() {
        try {
            repository.saveAll(products);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar el catalogo de productos", e);
        }
    }
}
