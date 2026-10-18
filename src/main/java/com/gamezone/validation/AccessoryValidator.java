package com.gamezone.validation;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Accessory;
import com.gamezone.model.Product;

/**
 * Checks the data and the rules of the accessories before one is registered.
 * Every method is static: a validator holds no state and is never instantiated,
 * so it cannot be created from outside this class.
 */
public final class AccessoryValidator {

    /** Type accepted for a controller. */
    private static final String TYPE_CONTROLLER = "CONTROLLER";

    /** Type accepted for a cable. */
    private static final String TYPE_CABLE = "CABLE";

    /** Type accepted for a memory. */
    private static final String TYPE_MEMORY = "MEMORY";

    /**
     * Hides the constructor because the class only offers static methods.
     */
    private AccessoryValidator() {
    }

    /**
     * Checks that the type of an accessory is one the store knows.
     * @param type type to check
     * @throws InvalidDataException if the type is empty or is not CONTROLLER,
     *                              CABLE or MEMORY
     */
    public static void validateAccessoryType(String type) {
        if (type == null || type.isBlank()) {
            throw new InvalidDataException("tipo", "es obligatorio");
        }
        String value = type.trim();
        if (!TYPE_CONTROLLER.equalsIgnoreCase(value)
                && !TYPE_CABLE.equalsIgnoreCase(value)
                && !TYPE_MEMORY.equalsIgnoreCase(value)) {
            throw new InvalidDataException("tipo",
                    "debe ser CONTROLLER, CABLE o MEMORY");
        }
    }

    /**
     * Checks that a console listed as compatible really exists in the catalogue
     * and that it really is a console.
     * @param product product found with that identifier, or {@code null}
     * @param consoleId identifier of the console
     * @throws ResourceNotFoundException if the console does not exist
     * @throws BusinessRuleException if the product found is not a console
     */
    public static void validateCompatibleConsole(Product product, String consoleId) {
        if (product == null) {
            throw new ResourceNotFoundException("la consola", consoleId);
        }
        if (!product.getCategory().equalsIgnoreCase("CONSOLE")) {
            throw new BusinessRuleException(
                    "El producto " + product.getId() + " no es una consola.");
        }
    }

    /**
     * Checks that an accessory was found by its identifier.
     * @param accessory accessory found, or {@code null} when it does not exist
     * @param accessoryId identifier that was searched
     * @throws ResourceNotFoundException if the accessory is {@code null}
     */
    public static void validateAccessoryExists(Accessory accessory, String accessoryId) {
        if (accessory == null) {
            throw new ResourceNotFoundException("el accesorio", accessoryId);
        }
    }
}
