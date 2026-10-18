package com.gamezone.validation;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.model.Promotion;
import java.time.LocalDate;

/**
 * Checks the data and the rules of the promotions before one is registered.
 * Every method is static: a validator holds no state and is never instantiated,
 * so it cannot be created from outside this class.
 */
public final class PromotionValidator {

    /** Highest percentage accepted by any promotion. */
    private static final double MAX_PERCENTAGE = 100;

    /** Category accepted for video games. */
    private static final String CATEGORY_VIDEOGAME = "VIDEOGAME";

    /** Category accepted for consoles. */
    private static final String CATEGORY_CONSOLE = "CONSOLE";

    /** Category accepted for accessories. */
    private static final String CATEGORY_ACCESSORY = "ACCESSORY";

    /**
     * Hides the constructor because the class only offers static methods.
     */
    private PromotionValidator() {
    }

    /**
     * Checks the common data of a promotion before it is registered.
     * @param id promotion identifier
     * @param name promotion name
     * @param startDate first day the promotion applies
     * @param endDate last day the promotion applies
     * @throws InvalidDataException if the identifier or the name are empty, if a
     *                              date is missing or if the end date is before
     *                              the start date
     */
    public static void validatePromotionData(String id, String name,
                                             LocalDate startDate, LocalDate endDate) {
        if (id == null || id.isBlank()) {
            throw new InvalidDataException("identificador", "es obligatorio");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidDataException("nombre", "es obligatorio");
        }
        if (startDate == null) {
            throw new InvalidDataException("fecha de inicio", "es obligatoria");
        }
        if (endDate == null) {
            throw new InvalidDataException("fecha de fin", "es obligatoria");
        }
        if (endDate.isBefore(startDate)) {
            throw new InvalidDataException("fecha de fin",
                    "no puede ser anterior a la fecha de inicio");
        }
    }

    /**
     * Checks that a percentage is usable in a promotion.
     * @param percentage percentage to check
     * @throws InvalidDataException if the percentage is not greater than zero or
     *                              is above one hundred
     */
    public static void validatePercentage(double percentage) {
        if (percentage <= 0) {
            throw new InvalidDataException("porcentaje", "debe ser mayor que cero");
        }
        if (percentage > MAX_PERCENTAGE) {
            throw new InvalidDataException("porcentaje", "no puede ser mayor que 100");
        }
    }

    /**
     * Checks that the target category of a category discount is one the store
     * knows: video games, consoles or accessories.
     * @param category category to check
     * @throws InvalidDataException if the category is empty or is not one of the
     *                              three accepted values
     */
    public static void validateCategory(String category) {
        if (category == null || category.isBlank()) {
            throw new InvalidDataException("categoria", "es obligatoria");
        }
        String value = category.trim();
        if (!CATEGORY_VIDEOGAME.equalsIgnoreCase(value)
                && !CATEGORY_CONSOLE.equalsIgnoreCase(value)
                && !CATEGORY_ACCESSORY.equalsIgnoreCase(value)) {
            throw new InvalidDataException("categoria",
                    "debe ser VIDEOGAME, CONSOLE o ACCESSORY");
        }
    }

    /**
     * Checks that the minimum quantity of a bulk promotion can be reached.
     * @param minimumQuantity units the sale must include
     * @throws InvalidDataException if the minimum quantity is not greater than zero
     */
    public static void validateMinimumQuantity(int minimumQuantity) {
        if (minimumQuantity <= 0) {
            throw new InvalidDataException("cantidad minima",
                    "debe ser mayor que cero");
        }
    }

    /**
     * Checks that the identifier is not used by a promotion already registered.
     * @param existing promotion that already uses the identifier, or {@code null}
     * @param id identifier being registered
     * @throws BusinessRuleException if the identifier is already in use
     */
    public static void validateUniquePromotion(Promotion existing, String id) {
        if (existing != null) {
            throw new BusinessRuleException(
                    "Ya existe una promoción con el identificador " + id + ".");
        }
    }
}
