package com.gamezone.exceptions;

/**
 * Raised when a resource is searched by its identifier and it does not exist in
 * the system, such as a product, a customer, a seller or a sale.
 */
public class ResourceNotFoundException extends GameZoneException {

    /** Error code that identifies this kind of problem. */
    private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";

    /**
     * Builds the message with the kind of resource and the identifier that was
     * searched. The resource type must already include its article, for example
     * "el producto" or "la venta", so the sentence is correct.
     * @param resourceType kind of resource that was searched, with its article
     * @param id identifier that was searched
     */
    public ResourceNotFoundException(String resourceType, String id) {
        super("No existe " + resourceType + " con identificador " + id + ".", ERROR_CODE);
    }
}
