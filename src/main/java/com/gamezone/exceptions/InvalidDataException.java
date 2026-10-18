package com.gamezone.exceptions;

/**
 * Raised when a value entered by the user does not have the expected format or
 * range, such as a negative price, a negative quantity or an empty field where
 * content is required.
 */
public class InvalidDataException extends GameZoneException {

    /** Error code that identifies this kind of problem. */
    private static final String ERROR_CODE = "INVALID_DATA";

    /**
     * Builds the message with the name of the field and what is wrong with it,
     * for example "El campo precio debe ser mayor que cero."
     * @param field name of the field that is wrong
     * @param problem description of what is wrong with that field
     */
    public InvalidDataException(String field, String problem) {
        super("El campo " + field + " " + problem + ".", ERROR_CODE);
    }
}
