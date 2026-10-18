package com.gamezone.exceptions;

/**
 * Root of the exception hierarchy of the system. Every exception thrown by
 * GameZone Unicesar extends this class, so the menu can catch this single type
 * and then handle each concrete case. It extends {@link RuntimeException}
 * because the store wants the operations to fail with a clear message and not
 * force every caller to declare the exception in its signature.
 */
public abstract class GameZoneException extends RuntimeException {

    /** Keeps the same serial version across compilations. */
    private static final long serialVersionUID = 1L;

    /** Error code used when a subclass does not provide its own. */
    private static final String DEFAULT_ERROR_CODE = "GAMEZONE_ERROR";

    /** Code that identifies the kind of problem for the user interface. */
    private String errorCode;

    /**
     * Creates the exception with a generic error code.
     * @param message message shown to the user
     */
    protected GameZoneException(String message) {
        super(message);
        this.errorCode = DEFAULT_ERROR_CODE;
    }

    /**
     * Creates the exception with its own error code.
     * @param message message shown to the user
     * @param errorCode code that identifies the kind of problem
     */
    protected GameZoneException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Creates the exception with its own error code, keeping the original error
     * so the cause is not lost.
     * @param message message shown to the user
     * @param errorCode code that identifies the kind of problem
     * @param cause original error that caused this one
     */
    protected GameZoneException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Returns the code that identifies the kind of problem.
     * @return error code of this exception
     */
    public String getErrorCode() {
        return errorCode;
    }
}
