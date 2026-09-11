package com.gamezone.service;

import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.model.VideoGame;
import com.gamezone.persistence.ProductRepository;
import java.util.ArrayList;
import java.util.List;

/**
 * Business rules for products and stock.
 */
public class ProductService {
    private ProductRepository repository;
    private List<Product> products;

    /**
     * Creates service loading persisted data.
     * @param repository product repository
     */
    public ProductService(ProductRepository repository) {
        this.repository = repository;
        this.products = new ArrayList<>(repository.loadAll());
    }

    /**
     * Registers a video game.
     * @param id id
     * @param title title
     * @param price price
     * @param stock stock
     * @param platform platform
     * @param genre genre
     * @param ageRating age rating
     * @return created game
     */
    public VideoGame registerVideoGame(String id, String title, double price, int stock,
                                       String platform, String genre, String ageRating) {
        if (findById(id) != null) {
            throw new IllegalArgumentException("Duplicate product id");
        }
        VideoGame g = new VideoGame(id, title, price, stock, platform, genre, ageRating);
        products.add(g);
        repository.saveAll(products);
        return g;
    }

    /**
     * Registers a console.
     * @param id id
     * @param title title
     * @param price price
     * @param stock stock
     * @param brand brand
     * @param model model
     * @param generation generation
     * @return created console
     */
    public Console registerConsole(String id, String title, double price, int stock,
                                   String brand, String model, String generation) {
        if (findById(id) != null) {
            throw new IllegalArgumentException("Duplicate product id");
        }
        Console c = new Console(id, title, price, stock, brand, model, generation);
        products.add(c);
        repository.saveAll(products);
        return c;
    }

    /**
     * Lists all products.
     * @return copy of inventory
     */
    public List<Product> findAll() {
        return new ArrayList<>(products);
    }

    /**
     * Finds product by id.
     * @param id product id
     * @return product or null
     */
    public Product findById(String id) {
        for (Product p : products) {
            if (p.getId().equalsIgnoreCase(id)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Decreases stock by one, validating availability.
     * @param id product id
     */
    public void decreaseStock(String id) {
        Product p = findById(id);
        if (p == null) {
            throw new IllegalArgumentException("Product not found: " + id);
        }
        if (p.getStock() <= 0) {
            throw new IllegalStateException("Insufficient stock for " + id);
        }
        p.setStock(p.getStock() - 1);
        repository.saveAll(products);
    }
}

