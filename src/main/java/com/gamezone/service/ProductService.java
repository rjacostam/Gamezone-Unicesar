package com.gamezone.service;

import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.model.VideoGame;
import com.gamezone.persistence.ProductRepository;
import java.util.ArrayList;
import java.util.List;

/**
 * Reglas de productos y stock.
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
     * Registra un videojuego.
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
     * Registra una consola.
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
     * Lista todo el inventario.
     * @return copy of inventory
     */
    public List<Product> findAll() {
        return new ArrayList<>(products);
    }

    /**
     * Busca producto por id.
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
     * Baja stock en uno validando que haya.
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
