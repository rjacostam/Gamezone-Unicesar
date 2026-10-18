package com.gamezone.validation;

import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import java.time.LocalDate;

/**
 * Checks the data and the rules of the warranties before one is granted.
 * Every method is static: a validator holds no state and is never instantiated,
 * so it cannot be created from outside this class.
 */
public final class WarrantyValidator {

    /**
     * Hides the constructor because the class only offers static methods.
     */
    private WarrantyValidator() {
    }

    /**
     * Checks that a warranty can be granted: it needs a product, a sale and a
     * start date.
     * @param product product covered by the warranty
     * @param sale sale the warranty comes from
     * @param startDate first day the warranty is in force
     * @throws InvalidDataException if the product, the sale or the start date are
     *                              missing
     */
    public static void validateWarrantyData(Product product, Sale sale, LocalDate startDate) {
        if (product == null) {
            throw new InvalidDataException("producto", "es obligatorio");
        }
        if (sale == null) {
            throw new InvalidDataException("venta", "es obligatoria");
        }
        if (startDate == null) {
            throw new InvalidDataException("fecha de inicio", "es obligatoria");
        }
    }

    /**
     * Checks how many days ahead the user wants to see the warranties that are
     * about to expire.
     * @param daysAhead days of anticipation
     * @throws InvalidDataException if the value is not greater than zero
     */
    public static void validateDaysAhead(int daysAhead) {
        if (daysAhead <= 0) {
            throw new InvalidDataException("dias", "deben ser mayores que cero");
        }
    }
}
