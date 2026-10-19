package com.gamezone.service;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.PersistenceException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.model.VideoGame;
import com.gamezone.persistence.ProductRepository;
import com.gamezone.validation.ProductValidator;
import java.util.ArrayList;
import java.util.List;

/**
 * Business rules of the product catalogue. It loads the catalogue when the
 * service is created, rejects duplicated identifiers, and saves the catalogue
 * after every change so the data survives between executions.
 * <p>
 * File errors do not reach the caller as a checked exception. The repository
 * already turns them into a {@link PersistenceException}, because a failure of
 * the disk is a failure of the application and not a mistake of the user.
 * <p>
 * Rejected data becomes an {@link InvalidDataException}, a
 * {@link ResourceNotFoundException} when a searched product does not exist, or a
 * {@link BusinessRuleException} when a rule of the store is broken, with a
 * message in Spanish, because the console menu shows it directly to the user.
 */
public class ProductService {
    private ProductRepository repository;
    private List<Product> products;

    /**
     * Creates the service and loads the catalogue stored in the repository.
     * @param repository repository used to read and write the catalogue
     * @throws PersistenceException if the catalogue file cannot be read
     */
    public ProductService(ProductRepository repository) {
        this.repository = repository;
        this.products = new ArrayList<>(repository.loadAll());
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
     * @throws InvalidDataException  if the identifier or the title are empty, if
     *                               the price is not greater than zero, or if
     *                               the stock is negative
     * @throws BusinessRuleException if the identifier is already used by another
     *                               product
     * @throws PersistenceException  if the catalogue file cannot be written
     */
    public VideoGame registerVideoGame(String id, String title, double price, int stock,
                                       String platform, String genre, String ageRating) {
        validateProductData(id, title, price, stock);
        if (findById(id) != null) {
            ProductValidator.validateUniqueProduct(findById(id), id);
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
     * @throws InvalidDataException  if the identifier or the title are empty, if
     *                               the price is not greater than zero, or if
     *                               the stock is negative
     * @throws BusinessRuleException if the identifier is already used by another
     *                               product
     * @throws PersistenceException  if the catalogue file cannot be written
     */
    public Console registerConsole(String id, String title, double price, int stock,
                                   String brand, String model, String generation) {
        validateProductData(id, title, price, stock);
        if (findById(id) != null) {
            ProductValidator.validateUniqueProduct(findById(id), id);
        }
        Console console = new Console(id, title, price, stock, brand, model, generation);
        products.add(console);
        saveCatalogue();
        return console;
    }

    /**
     * Checks the common data of a product before it is registered.
     * @param id product identifier
     * @param title product title
     * @param price unit price
     * @param stock available stock
     * @throws InvalidDataException if the identifier or the title are empty, if
     *                              the price is not greater than zero or if the
     *                              stock is negative
     */
    private void validateProductData(String id, String title, double price, int stock) {
        ProductValidator.validateProductData(id, title, price, stock);
    }

    /**
     * Returns a copy of the whole catalogue, in registration order.
     * @return products currently registered
     */
    public List<Product> listAllProducts() {
        return new ArrayList<>(products);
    }

    /**
     * Searches a product by its identifier, ignoring letter case, so a user can
     * type "vg-01" and still find the product registered as "VG-01".
     * @param id identifier to look for
     * @return the product found, or null when no product uses that identifier
     */
    public Product findById(String id) {
        for (Product product : products) {
            if (product.getId().equalsIgnoreCase(id)) {
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
     * @throws ResourceNotFoundException if the product does not exist
     * @throws BusinessRuleException     if the quantity is negative or if the
     *                                   stock is not enough
     * @throws PersistenceException      if the catalogue file cannot be written
     */
    public void updateStock(String productId, int quantity) {
        Product product = findById(productId);
        ProductValidator.validateProductExists(product, productId);
        product.decreaseStock(quantity);
        saveCatalogue();
    }

    /**
     * Adds returned units back to the stock of a product and saves the catalogue.
     * It is used by the return module, so the inventory is only changed here.
     * @param productId identifier of the product to update
     * @param quantity amount of units to add to the stock
     * @throws InvalidDataException      if the quantity is not greater than zero
     * @throws ResourceNotFoundException if the product does not exist
     * @throws PersistenceException      if the catalogue file cannot be written
     */
    public void restoreStock(String productId, int quantity) {
        ProductValidator.validateQuantity(quantity);
        Product product = findById(productId);
        ProductValidator.validateProductExists(product, productId);
        product.setStock(product.getStock() + quantity);
        saveCatalogue();
    }

    /**
     * Saves the catalogue currently registered.
     *
     * @throws PersistenceException if the catalogue file cannot be written
     */
    private void saveCatalogue() {
        repository.saveAll(products);
    }
}
