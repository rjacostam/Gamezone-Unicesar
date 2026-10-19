package com.gamezone.validation;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Customer;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import java.util.List;

/**
 * Checks the data and the rules of the sales before a sale is registered.
 * Every method is static: a validator holds no state and is never instantiated,
 * so it cannot be created from outside this class.
 */
public final class SaleValidator {

    /**
     * Hides the constructor because the class only offers static methods.
     */
    private SaleValidator() {
    }

    /**
     * Checks that a sale can be registered: it needs a customer, a seller and at
     * least one product.
     * @param customer customer buying
     * @param seller seller serving the sale
     * @param products units sold, one entry per unit
     * @throws InvalidDataException if the customer or the seller are missing
     * @throws BusinessRuleException if the list of products is empty
     */
    public static void validateSaleData(Customer customer, Seller seller, List<Product> products) {
        if (customer == null) {
            throw new InvalidDataException("cliente", "es obligatorio");
        }
        if (seller == null) {
            throw new InvalidDataException("vendedor", "es obligatorio");
        }
        if (products == null || products.isEmpty()) {
            throw new BusinessRuleException("La venta debe contener al menos un producto.");
        }
    }

    /**
     * Checks that a sale was found by its identifier.
     * @param sale sale found, or {@code null} when it does not exist
     * @param saleId identifier that was searched
     * @throws ResourceNotFoundException if the sale is {@code null}
     */
    public static void validateSaleExists(Sale sale, String saleId) {
        if (sale == null) {
            throw new ResourceNotFoundException("la venta", saleId);
        }
    }

    /**
     * Checks that every product that should get an extended warranty is a console
     * of the sale, because only consoles can carry that warranty.
     * @param products units of the sale
     * @param productIds identifiers that were asked for an extended warranty
     * @throws BusinessRuleException if an identifier is not a console of the sale
     */
    public static void validateExtendedWarrantyRequest(List<Product> products,
                                                       List<String> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return;
        }
        for (String productId : productIds) {
            boolean foundAsConsole = false;
            for (Product product : products) {
                if (product.getId().equalsIgnoreCase(productId)) {
                    if (product.isConsole()) {
                        foundAsConsole = true;
                    }
                    break;
                }
            }
            if (!foundAsConsole) {
                throw new BusinessRuleException(
                        "Solo las consolas admiten garantia extendida: " + productId + ".");
            }
        }
    }
}
