package com.gamezone.validation;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Product;

/**
 * Checks the data and the rules of the products before the catalogue is
 * modified. Every method is static: a validator holds no state and is never
 * instantiated, so it cannot be created from outside this class.
 */
public final class ProductValidator {

    /**
     * Hides the constructor because the class only offers static methods.
     */
    private ProductValidator() {
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
    public static void validateProductData(String id, String title, double price, int stock) {
        if (id == null || id.isBlank()) {
            throw new InvalidDataException("identificador", "es obligatorio");
        }
        if (title == null || title.isBlank()) {
            throw new InvalidDataException("titulo", "es obligatorio");
        }
        if (price <= 0) {
            throw new InvalidDataException("precio", "debe ser mayor que cero");
        }
        if (stock < 0) {
            throw new InvalidDataException("stock", "no puede ser negativo");
        }
    }

    /**
     * Checks that the identifier is not used by a product already registered.
     * @param existing product that already uses the identifier, or {@code null}
     * @param id identifier being registered
     * @throws BusinessRuleException if the identifier is already in use
     */
    public static void validateUniqueProduct(Product existing, String id) {
        if (existing != null) {
            throw new BusinessRuleException(
                    "Ya existe un producto con el identificador " + id + ".");
        }
    }

    /**
     * Checks that a product was found by its identifier.
     * @param product product found, or {@code null} when it does not exist
     * @param productId identifier that was searched
     * @throws ResourceNotFoundException if the product is {@code null}
     */
    public static void validateProductExists(Product product, String productId) {
        if (product == null) {
            throw new ResourceNotFoundException("el producto", productId);
        }
    }

    /**
     * Checks that the store has enough units of a product to sell.
     * @param product product being sold
     * @param requestedQuantity units the store wants to sell
     * @throws BusinessRuleException if there is not enough stock
     */
    public static void validateStockAvailability(Product product, int requestedQuantity) {
        if (!product.hasStock(requestedQuantity)) {
            throw new BusinessRuleException("Stock insuficiente para "
                    + product.getTitle() + ". Disponible: " + product.getStock()
                    + ", solicitado: " + requestedQuantity + ".");
        }
    }

    /**
     * Checks that a quantity is not negative.
     * @param quantity quantity to check
     * @throws InvalidDataException if the quantity is negative
     */
    public static void validateQuantity(int quantity) {
        if (quantity < 0) {
            throw new InvalidDataException("cantidad", "no puede ser negativa");
        }
    }
}
