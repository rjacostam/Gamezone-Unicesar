package com.gamezone.exceptions;

/**
 * Raised when a data file cannot be read or written, for example when it is
 * damaged, the permission is denied or the disk is full. The original error is
 * kept as the cause so the problem can still be traced.
 */
public class PersistenceException extends GameZoneException {

    /** Keeps the same serial version across compilations. */
    private static final long serialVersionUID = 1L;

    /** Error code that identifies this kind of problem. */
    private static final String ERROR_CODE = "PERSISTENCE_ERROR";

    /**
     * Creates the exception with the message that names the file and the
     * operation, and keeps the original error.
     * @param message message shown to the user, naming the file and the operation
     * @param cause original error that caused this one
     */
    public PersistenceException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }
}
