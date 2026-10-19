package com.gamezone.model;

import java.util.List;

/**
 * Controller accessory sold by the store. On top of the data inherited from
 * Accessory it keeps how the controller connects to the console.
 */
public class Controller extends Accessory {
    private String connectionType;

    /**
     * Creates a controller accessory.
     * @param id product identifier
     * @param title product title
     * @param price unit price
     * @param stock available stock
     * @param compatibleConsoleIds ids of the compatible consoles
     * @param connectionType how the controller connects to the console
     */
    public Controller(String id, String title, double price, int stock,
                      List<String> compatibleConsoleIds, String connectionType) {
        super(id, title, price, stock, compatibleConsoleIds);
        this.connectionType = connectionType;
    }

    /**
     * Returns the type marker of this accessory.
     * @return CONTROLLER
     */
    @Override
    public String getAccessoryType() {
        return "CONTROLLER";
    }

    /**
     * Returns the columns that only a controller has, in the order the accessory
     * file expects them. A controller has one: how it connects. A null is written
     * as an empty string, so the word {@code null} never reaches the file.
     * @return a one element list with the connection type
     */
    @Override
    public List<String> getExtraFields() {
        return List.of(connectionType == null ? "" : connectionType);
    }

    /**
     * Builds the description of this controller by adding the connection type to
     * the description of the common accessory data.
     * @return description ready to be shown to the user
     */
    @Override
    public String getDescription() {
        return super.getDescription() + " | Conexión: " + connectionType;
    }

    /**
     * Returns how this controller connects to the console.
     * @return connection type of the controller
     */
    public String getConnectionType() {
        return connectionType;
    }

    /**
     * Changes how this controller connects to the console.
     * @param connectionType new connection type
     */
    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }
}
