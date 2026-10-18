package com.gamezone.service;

import com.gamezone.model.Accessory;
import com.gamezone.model.Cable;
import com.gamezone.model.Console;
import com.gamezone.model.Controller;
import com.gamezone.model.Memory;
import com.gamezone.model.Product;
import com.gamezone.persistence.AccessoryRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Business rules of the accessory module. It loads the accessories when the
 * service is created, rejects invalid data and duplicated identifiers, checks
 * that every compatible console really exists in the catalogue, and saves the
 * accessories after every change so the data survives between executions.
 * <p>
 * File errors do not reach the caller as a checked exception. They become an
 * {@link IllegalStateException}, the same way {@code ProductService} does it,
 * because a failure of the disk is a failure of the application and not a
 * mistake of the user.
 * <p>
 * Rejected data becomes an {@link IllegalArgumentException} with a message in
 * Spanish, because the console menu shows it directly to the user.
 * <p>
 * The service needs {@code ProductService} for two reasons: to check that the
 * identifier of a new accessory is not already used by a product, and to check
 * that every compatible console identifier belongs to a console of the
 * inventory.
 */
public class AccessoryService {

    /** Type discriminator of a controller. */
    private static final String TYPE_CONTROLLER = "CONTROLLER";

    /** Type discriminator of a cable. */
    private static final String TYPE_CABLE = "CABLE";

    /** Type discriminator of a memory card. */
    private static final String TYPE_MEMORY = "MEMORY";

    private AccessoryRepository repository;
    private ProductService productService;
    private List<Accessory> accessories;

    /**
     * Creates the service and loads the accessories stored in the repository.
     *
     * @param repository     repository used to read and write the accessories
     * @param productService service of the catalogue, used to validate
     *                       identifiers and compatible consoles
     * @throws IllegalStateException if the accessories file cannot be read
     */
    public AccessoryService(AccessoryRepository repository, ProductService productService) {
        this.repository = repository;
        this.productService = productService;
        try {
            this.accessories = new ArrayList<>(repository.loadAll());
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar el catalogo de accesorios", e);
        }
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
     * @throws IllegalArgumentException if the data is invalid or the identifier
     *                                  is already in use
     * @throws IllegalStateException   if the accessories file cannot be written
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
     * @throws IllegalArgumentException if the data is invalid or the identifier
     *                                  is already in use
     * @throws IllegalStateException   if the accessories file cannot be written
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
     * @throws IllegalArgumentException if the data is invalid or the identifier
     *                                  is already in use
     * @throws IllegalStateException   if the accessories file cannot be written
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
     * @throws IllegalArgumentException if the type is not one of the three
     *                                  types of accessory
     */
    public List<Accessory> listAccessoriesByType(String type) {
        if (!TYPE_CONTROLLER.equalsIgnoreCase(type)
                && !TYPE_CABLE.equalsIgnoreCase(type)
                && !TYPE_MEMORY.equalsIgnoreCase(type)) {
            throw new IllegalArgumentException(
                    "El tipo de accesorio debe ser CONTROLLER, CABLE o MEMORY");
        }
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
     * Checks the data shared by every accessory before it is registered.
     *
     * @param id    accessory identifier
     * @param title accessory title
     * @param price unit price
     * @param stock available stock
     * @throws IllegalArgumentException if the identifier or the title is empty,
     *                                  if the price is not greater than zero,
     *                                  if the stock is negative, or if the
     *                                  identifier is already used by an
     *                                  accessory or by a product
     */
    private void validateAccessoryData(String id, String title, double price, int stock) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El identificador del accesorio es obligatorio");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("El titulo del accesorio es obligatorio");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        if (isIdentifierInUse(id.trim())) {
            throw new IllegalArgumentException(
                    "Ya existe un accesorio o un producto con el identificador " + id.trim());
        }
    }

    /**
     * Checks that every compatible console identifier belongs to a console of
     * the inventory. An accessory can only be compatible with consoles, never
     * with a video game.
     *
     * @param compatibleConsoleIds identifiers to check, may be null or empty
     * @throws IllegalArgumentException if an identifier does not exist in the
     *                                  catalogue or does not belong to a console
     */
    private void validateCompatibleConsoles(List<String> compatibleConsoleIds) {
        if (compatibleConsoleIds == null) {
            return;
        }
        for (String consoleId : compatibleConsoleIds) {
            if (consoleId == null || consoleId.isBlank()) {
                throw new IllegalArgumentException(
                        "La consola compatible no puede estar vacia");
            }
            Product product = productService.findById(consoleId.trim());
            if (product == null) {
                throw new IllegalArgumentException(
                        "No existe una consola con el identificador " + consoleId.trim());
            }
            if (!(product instanceof Console)) {
                throw new IllegalArgumentException(
                        "El accesorio solo puede ser compatible con consolas y "
                                + consoleId.trim() + " no es una consola");
            }
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
     * @throws IllegalStateException if the accessories file cannot be written
     */
    private void addAccessory(Accessory accessory) {
        accessories.add(accessory);
        saveCatalogue();
    }

    /**
     * Saves the accessories currently registered.
     *
     * @throws IllegalStateException if the accessories file cannot be written
     */
    private void saveCatalogue() {
        try {
            repository.saveAll(accessories);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar el catalogo de accesorios", e);
        }
    }
}