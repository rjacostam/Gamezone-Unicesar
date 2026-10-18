package com.gamezone.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Base class for the accessories sold by the store. An accessory is a product,
 * so it inherits the identifier, the title, the price, the stock and the
 * inventory rules from Product. On top of that it keeps the list of console ids
 * it works with, and declares the type discriminator that each concrete
 * accessory type must provide.
 */
public abstract class Accessory extends Product {
    private List<String> compatibleConsoleIds;

    /**
     * Creates an accessory with its common data and the consoles it supports.
     * A null list is accepted and treated as an accessory without compatible
     * consoles.
     * @param id product identifier
     * @param title product title
     * @param price unit price
     * @param stock available stock
     * @param compatibleConsoleIds ids of the compatible consoles
     */
    protected Accessory(String id, String title, double price, int stock,
                        List<String> compatibleConsoleIds) {
        super(id, title, price, stock);
        this.compatibleConsoleIds = new ArrayList<>();
        if (compatibleConsoleIds != null) {
            for (String consoleId : compatibleConsoleIds) {
                if (consoleId != null && !consoleId.isBlank() && !containsConsoleId(consoleId)) {
                    this.compatibleConsoleIds.add(consoleId);
                }
            }
        }
    }

    /**
     * Returns the type marker of this accessory, used to filter the catalogue
     * and to rebuild the correct class when the catalogue file is read.
     * @return CONTROLLER, CABLE or MEMORY
     */
    public abstract String getAccessoryType();

    /**
     * Builds the description of the accessory with the data common to every
     * accessory: its identifier, its title, its price, its stock and the
     * consoles it is compatible with. Each subclass overrides this method, calls
     * super.getDescription() and appends its own attributes.
     * @return description ready to be shown to the user
     */
    @Override
    public String getDescription() {
        return "Accesorio [" + getId() + "] " + getTitle()
                + " | Precio: " + String.format(Locale.US, "$%,.2f", getPrice())
                + " | Stock: " + getStock()
                + " | Compatible con: " + compatibleConsoleIds;
    }

    /**
     * Tells whether this accessory works with the given console, ignoring letter
     * case in the identifier.
     * @param consoleId identifier of the console to check
     * @return true when the console is in the compatible list
     */
    public boolean isCompatibleWith(String consoleId) {
        if (consoleId == null) {
            return false;
        }
        return containsConsoleId(consoleId);
    }

    /**
     * Looks for a console identifier in the compatible list, ignoring letter
     * case. It is private on purpose so the constructor can reuse it without
     * calling an overridable method before the subclass is initialized.
     * @param consoleId identifier to look for
     * @return true when the identifier is already in the list
     */
    private boolean containsConsoleId(String consoleId) {
        for (String compatibleId : compatibleConsoleIds) {
            if (compatibleId.equalsIgnoreCase(consoleId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a compatible console to the list. Identifiers already present, and
     * null or blank identifiers, are ignored so the list never repeats an entry.
     * @param consoleId identifier of the console to add
     */
    public void addCompatibleConsole(String consoleId) {
        if (consoleId == null || consoleId.isBlank() || isCompatibleWith(consoleId)) {
            return;
        }
        compatibleConsoleIds.add(consoleId);
    }

    /**
     * Removes a compatible console from the list. When the console is not in the
     * list, nothing changes.
     * @param consoleId identifier of the console to remove
     */
    public void removeCompatibleConsole(String consoleId) {
        if (consoleId == null) {
            return;
        }
        compatibleConsoleIds.removeIf(compatibleId -> compatibleId.equalsIgnoreCase(consoleId));
    }

    /**
     * Returns the ids of the compatible consoles.
     * @return ids of the compatible consoles
     */
    public List<String> getCompatibleConsoleIds() {
        return compatibleConsoleIds;
    }
}
