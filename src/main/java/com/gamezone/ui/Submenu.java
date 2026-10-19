package com.gamezone.ui;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.PersistenceException;
import com.gamezone.exceptions.ResourceNotFoundException;

/**
 * Base class of the submenus of the console, one per module of the store.
 * <p>
 * Each submenu shows its own options and talks only to the services of its module. What
 * they all share lives here: the reader of the console and the way an error of the system
 * becomes a clear message for the user, so no operation ever shows a stack trace.
 */
public abstract class Submenu {

    /** Reader shared by every submenu. */
    protected final ConsoleInput input;

    /**
     * Creates the submenu.
     *
     * @param input reader of the console
     */
    protected Submenu(ConsoleInput input) {
        this.input = input;
    }

    /**
     * Shows the options of the submenu and runs the one the user chooses.
     */
    public abstract void show();

    /**
     * Runs one operation and turns any error of the system into a clear message for the
     * user, with a different text for each kind of error.
     *
     * @param action operation chosen by the user
     */
    protected void runSafely(Runnable action) {
        try {
            action.run();
        } catch (ResourceNotFoundException e) {
            System.out.println("No se encontró el recurso solicitado: " + e.getMessage());
        } catch (BusinessRuleException e) {
            System.out.println("No se puede completar la operación: " + e.getMessage());
        } catch (InvalidDataException e) {
            System.out.println("Los datos ingresados no son válidos: " + e.getMessage());
        } catch (PersistenceException e) {
            System.out.println("Ocurrió un problema al acceder a los datos del sistema. Contacte al administrador.");
        }
    }
}
