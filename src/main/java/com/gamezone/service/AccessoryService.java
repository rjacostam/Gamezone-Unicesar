package com.gamezone.service;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.PersistenceException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Accessory;
import com.gamezone.model.AccessoryFactory;
import com.gamezone.model.Cable;
import com.gamezone.model.Controller;
import com.gamezone.model.Memory;
import com.gamezone.persistence.AccessoryRepository;
import com.gamezone.validation.AccessoryValidator;
import com.gamezone.validation.ProductValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Business rules of the accessory module. It loads the accessories when the
 * service is created, rejects invalid data and duplicated identifiers, checks
 * that every compatible console really exists in the catalogue, and saves the
 * accessories after every change so the data survives between executions.
 * <p>
 * File errors do not reach the caller as a checked exception. The repository
 * already turns them into a {@link PersistenceException}, the same way
 * {@code ProductService} does it, because a failure of the disk is a failure of
 * the application and not a mistake of the user.
 * <p>
 * Rejected data becomes an {@link InvalidDataException}, a
 * {@link ResourceNotFoundException} when a searched console does not exist, or a
 * {@link BusinessRuleException} when a rule of the store is broken, with a
 * message in Spanish, because the console menu shows it directly to the user.
 * <p>
 * The service needs the catalogue for two reasons: to check that the
 * identifier of a new accessory is not already used by a product, and to check
 * that every compatible console identifier belongs to a console of the
 * inventory.
 */
public class AccessoryService implements InventoryCatalog, StockUpdater, StockRestorer {

    private AccessoryRepository repository;
    private ItemFinder productService;
    private List<Accessory> accessories;

    /**
     * Creates the service and loads the accessories stored in the repository.
     *
     * @param repository     repository used to read and write the accessories
     * @param productService service of the catalogue, used to validate
     *                       identifiers and compatible consoles
     * @throws PersistenceException if the accessories file cannot be read
     */
    public AccessoryService(AccessoryRepository repository, ItemFinder productService) {
        this.repository = repository;
        this.productService = productService;
        this.accessories = new ArrayList<>(repository.loadAll());
    }

    /**
     * Registers an accessory of any kind, once it has been built by
     * {@link AccessoryFactory}.
     *
     * <p>This is the single entry point of the correction of violation V-06:
     * the service no longer needs one method per kind, because it no longer
     * decides which kind is being registered. The three methods that exist for
     * each kind stay until the menus are moved to this one.
     *
     * @param accessory accessory to register
     * @return the registered accessory
     * @throws InvalidDataException      if the data is invalid
     * @throws ResourceNotFoundException if a compatible console does not exist
     * @throws BusinessRuleException     if the identifier is already in use, or
     *                                   if a compatible product is not a console
     * @throws PersistenceException      if the accessories file cannot be written
     */
    public Accessory register(Accessory accessory) {
        validateAccessoryData(accessory.getId(), accessory.getTitle(),
                accessory.getPrice(), accessory.getStock());
        validateCompatibleConsoles(accessory.getCompatibleConsoleIds());
        addAccessory(accessory);
        return accessory;
    }

    /**
     * Registers a new controller. Its identifier cannot be used by another
     * accessory or by another product, and every compatible console identifier
     * must belong to a console of the inventory.
     *
     * @param id                   accessory identifier
     * @param title                accessory title
     * @param price                unit price
     * @param stock                available stock
     * @param compatibleConsoleIds ids of the consoles the controller works with
     * @param connectionType       wireless or wired connection
     * @return the registered controller
     * @throws InvalidDataException      if the data is invalid
     * @throws ResourceNotFoundException if a compatible console does not exist
     * @throws BusinessRuleException     if the identifier is already in use, or
     *                                   if a compatible product is not a console
     * @throws PersistenceException      if the accessories file cannot be written
     */
    public Controller registerController(String id, String title, double price, int stock,
                                         List<String> compatibleConsoleIds, String connectionType) {
        validateAccessoryData(id, title, price, stock);
        validateCompatibleConsoles(compatibleConsoleIds);
        Controller controller = new Controller(id.trim(), title.trim(), price, stock,
                compatibleConsoleIds, connectionType);
        addAccessory(controller);
        return controller;
    }

    /**
     * Registers a new cable. Its identifier cannot be used by another accessory
     * or by another product, and every compatible console identifier must belong
     * to a console of the inventory.
     *
     * @param id                   accessory identifier
     * @param title                accessory title
     * @param price                unit price
     * @param stock                available stock
     * @param compatibleConsoleIds ids of the consoles the cable works with
     * @param lengthInMeters       length of the cable in meters
     * @param connectorType        connector of the cable, for example HDMI
     * @return the registered cable
     * @throws InvalidDataException      if the data is invalid
     * @throws ResourceNotFoundException if a compatible console does not exist
     * @throws BusinessRuleException     if the identifier is already in use, or
     *                                   if a compatible product is not a console
     * @throws PersistenceException      if the accessories file cannot be written
     */
    public Cable registerCable(String id, String title, double price, int stock,
                               List<String> compatibleConsoleIds, double lengthInMeters,
                               String connectorType) {
        validateAccessoryData(id, title, price, stock);
        validateCompatibleConsoles(compatibleConsoleIds);
        Cable cable = new Cable(id.trim(), title.trim(), price, stock,
                compatibleConsoleIds, lengthInMeters, connectorType);
        addAccessory(cable);
        return cable;
    }

    /**
     * Registers a new memory card. Its identifier cannot be used by another
     * accessory or by another product, and every compatible console identifier
     * must belong to a console of the inventory.
     *
     * @param id                   accessory identifier
     * @param title                accessory title
     * @param price                unit price
     * @param stock                available stock
     * @param compatibleConsoleIds ids of the consoles the memory card works with
     * @param capacityInGb         storage capacity in gigabytes
     * @param memoryType           type of memory, for example microSD
     * @return the registered memory card
     * @throws InvalidDataException      if the data is invalid
     * @throws ResourceNotFoundException if a compatible console does not exist
     * @throws BusinessRuleException     if the identifier is already in use, or
     *                                   if a compatible product is not a console
     * @throws PersistenceException      if the accessories file cannot be written
     */
    public Memory registerMemory(String id, String title, double price, int stock,
                                 List<String> compatibleConsoleIds, int capacityInGb,
                                 String memoryType) {
        validateAccessoryData(id, title, price, stock);
        validateCompatibleConsoles(compatibleConsoleIds);
        Memory memory = new Memory(id.trim(), title.trim(), price, stock,
                compatibleConsoleIds, capacityInGb, memoryType);
        addAccessory(memory);
        return memory;
    }

    /**
     * Returns a copy of every accessory currently registered, in registration
     * order.
     *
     * @return accessories currently registered
     */
    public List<Accessory> listAllAccessories() {
        return new ArrayList<>(accessories);
    }

    /**
     * Returns the accessories of one concrete type. The type is compared
     * ignoring letter case, so the menu can pass the value the user typed.
     *
     * @param type type to filter by, {@code CONTROLLER}, {@code CABLE} or
     *             {@code MEMORY}
     * @return accessories of the given type, empty when there is none
     * @throws InvalidDataException if the type is not one of the three types
     *                              of accessory
     */
    public List<Accessory> listAccessoriesByType(String type) {
        AccessoryValidator.validateAccessoryType(type);
        String searched = type.trim();
        List<Accessory> result = new ArrayList<>();
        for (Accessory accessory : accessories) {
            if (accessory.getAccessoryType().equalsIgnoreCase(searched)) {
                result.add(accessory);
            }
        }
        return result;
    }

    /**
     * Returns the accessories that work with a given console, so the seller can
     * answer the question of a customer before the purchase.
     *
     * @param consoleId identifier of the console
     * @return accessories compatible with the console, empty when there is none
     */
    public List<Accessory> findAccessoriesCompatibleWith(String consoleId) {
        List<Accessory> result = new ArrayList<>();
        if (consoleId == null || consoleId.isBlank()) {
            return result;
        }
        String searched = consoleId.trim();
        for (Accessory accessory : accessories) {
            if (accessory.isCompatibleWith(searched)) {
                result.add(accessory);
            }
        }
        return result;
    }

    /**
     * Searches an accessory by its identifier, ignoring letter case, so a user
     * can type "a-01" and still find the accessory registered as "A-01".
     *
     * @param id identifier to look for
     * @return the accessory found, or null when no accessory uses that
     *         identifier
     */
    @Override
    public Accessory findById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        String searched = id.trim();
        for (Accessory accessory : accessories) {
            if (accessory.getId().equalsIgnoreCase(searched)) {
                return accessory;
            }
        }
        return null;
    }

    /**
     * Removes sold units from the stock of an accessory and saves the catalogue.
     * <p>
     * The accessory itself rejects the update when the quantity is negative or
     * when the stock is not enough, so this method does not repeat that check.
     * The meaning of the operation is the same as in {@code ProductService}:
     * the quantity is what the store sells.
     *
     * @param accessoryId identifier of the accessory to update
     * @param quantity    units being removed from the stock
     * @throws ResourceNotFoundException if the accessory does not exist
     * @throws BusinessRuleException     if the quantity is negative or if the
     *                                   stock is not enough
     * @throws PersistenceException      if the accessories file cannot be written
     */
    @Override
    public void updateStock(String accessoryId, int quantity) {
        Accessory accessory = findById(accessoryId);
        AccessoryValidator.validateAccessoryExists(accessory, accessoryId);
        accessory.decreaseStock(quantity);
        saveCatalogue();
    }

    /**
 * Adds returned units back to the stock of an accessory and saves the catalogue.
 * It is used by the return module, so the inventory of the accessories is only
 * changed here. The method mirrors {@code ProductService.restoreStock}, so both
 * kinds of stock behave in the same way.
 *
 * @param accessoryId identifier of the accessory to update
 * @param quantity    amount of units to add to the stock
 * @throws InvalidDataException      if the quantity is not greater than zero
 * @throws ResourceNotFoundException if the accessory does not exist
 * @throws PersistenceException      if the accessories file cannot be written
 */
    @Override
    public void restoreStock(String accessoryId, int quantity) {
        ProductValidator.validateQuantity(quantity);
        Accessory accessory = findById(accessoryId);
        AccessoryValidator.validateAccessoryExists(accessory, accessoryId);
        accessory.setStock(accessory.getStock() + quantity);
        saveCatalogue();
    }

    /**
     * Checks the data shared by every accessory before it is registered.
     *
     * @param id    accessory identifier
     * @param title accessory title
     * @param price unit price
     * @param stock available stock
     * @throws InvalidDataException  if the identifier or the title is empty, if
     *                               the price is not greater than zero, or if
     *                               the stock is negative
     * @throws BusinessRuleException if the identifier is already used by an
     *                               accessory or by a product
     */
    private void validateAccessoryData(String id, String title, double price, int stock) {
        ProductValidator.validateProductData(id, title, price, stock);
        if (isIdentifierInUse(id.trim())) {
            throw new BusinessRuleException(
                    "Ya existe un accesorio o un producto con el identificador " + id.trim());
        }
    }

    /**
     * Checks that every compatible console identifier belongs to a console of
     * the inventory. An accessory can only be compatible with consoles, never
     * with a video game.
     *
     * @param compatibleConsoleIds identifiers to check, may be null or empty
     * @throws InvalidDataException      if an identifier is empty
     * @throws ResourceNotFoundException if an identifier does not exist in the
     *                                   catalogue
     * @throws BusinessRuleException     if an identifier does not belong to a
     *                                   console
     */
    private void validateCompatibleConsoles(List<String> compatibleConsoleIds) {
        if (compatibleConsoleIds == null) {
            return;
        }
        for (String consoleId : compatibleConsoleIds) {
            if (consoleId == null || consoleId.isBlank()) {
                throw new InvalidDataException(
                        "consola compatible", "no puede estar vacia");
            }
            AccessoryValidator.validateCompatibleConsole(
                    productService.findById(consoleId.trim()), consoleId.trim());
        }
    }

    /**
     * Tells whether an identifier is already used by an accessory or by a
     * product of the catalogue, ignoring letter case. Accessories are products,
     * so both lists have to be checked to keep every identifier unique in the
     * catalogue.
     *
     * @param id identifier to look for
     * @return true when the identifier is already in use
     */
    private boolean isIdentifierInUse(String id) {
        for (Accessory accessory : accessories) {
            if (accessory.getId().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return productService.findById(id) != null;
    }

    /**
     * Adds a validated accessory to the catalogue and saves it. The saving
     * happens after the accessory is added, so the file always reflects the
     * content of the service.
     *
     * @param accessory accessory to add
     * @throws PersistenceException if the accessories file cannot be written
     */
    private void addAccessory(Accessory accessory) {
        accessories.add(accessory);
        saveCatalogue();
    }

    /**
     * Saves the accessories currently registered.
     *
     * @throws PersistenceException if the accessories file cannot be written
     */
    private void saveCatalogue() {
        repository.saveAll(accessories);
    }
}
