package com.gamezone.exceptions;

/**
 * Raised when an operation breaks a rule of the store, such as not having enough
 * stock, registering a customer that already exists or selling with no products.
 */
public class BusinessRuleException extends GameZoneException {

    /** Keeps the same serial version across compilations. */
    private static final long serialVersionUID = 1L;

    /** Error code that identifies this kind of problem. */
    private static final String ERROR_CODE = "BUSINESS_RULE";

    /**
     * Creates the exception with the message that explains the broken rule.
     * @param message message shown to the user
     */
    public BusinessRuleException(String message) {
        super(message, ERROR_CODE);
    }
}
