package com.gamezone.validation;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import java.util.List;

/**
 * Checks the data and the rules of the returns before one is registered.
 * Every method is static: a validator holds no state and is never instantiated,
 * so it cannot be created from outside this class.
 */
public final class ReturnValidator {

    /**
     * Hides the constructor because the class only offers static methods.
     */
    private ReturnValidator() {
    }

    /**
     * Checks that a return can be registered: it needs at least one product and a
     * reason.
     * @param productIds identifiers of the units being returned
     * @param reason reason of the return
     * @throws BusinessRuleException if no product is being returned
     * @throws InvalidDataException if the reason is empty
     */
    public static void validateReturnData(List<String> productIds, String reason) {
        if (productIds == null || productIds.isEmpty()) {
            throw new BusinessRuleException(
                    "La devolución debe tener al menos un producto.");
        }
        if (reason == null || reason.isBlank()) {
            throw new InvalidDataException("motivo", "es obligatorio");
        }
    }

    /**
     * Checks that the original sale is still inside the 30 day return window.
     * @param sale original sale the return comes from
     * @throws BusinessRuleException if the sale can no longer be returned
     */
    public static void validateReturnWindow(Sale sale) {
        if (sale == null || !sale.canBeReturned()) {
            String saleId = sale == null ? "" : sale.getId();
            throw new BusinessRuleException(
                    "La venta " + saleId + " está fuera del plazo de 30 días.");
        }
    }

    /**
     * Checks that the units being returned belong to the sale and that there are
     * still units left to return.
     * @param sale original sale the return comes from
     * @param productId identifier of the unit being returned
     * @param requested units the customer wants to return now
     * @param alreadyReturned units of that product returned before
     * @throws BusinessRuleException if the product is not in the sale or if the
     *                                requested units were already returned
     */
    public static void validateUnitsAvailable(Sale sale, String productId,
                                              int requested, int alreadyReturned) {
        int sold = 0;
        for (Product product : sale.getProducts()) {
            if (product.getId().equalsIgnoreCase(productId)) {
                sold++;
            }
        }
        if (sold == 0) {
            throw new BusinessRuleException(
                    "El producto " + productId + " no pertenece a la venta "
                            + sale.getId() + ".");
        }
        if (requested + alreadyReturned > sold) {
            throw new BusinessRuleException("El producto " + productId
                    + " solo tiene " + sold + " unidad(es) y ya se devolvieron "
                    + alreadyReturned + ".");
        }
    }

    /**
     * Checks that the month given to look for a balance is a real month.
     * @param month month number, from 1 to 12
     * @throws InvalidDataException if the month is outside that range
     */
    public static void validateMonth(int month) {
        if (month < 1 || month > 12) {
            throw new InvalidDataException("mes", "debe estar entre 1 y 12");
        }
    }
}
