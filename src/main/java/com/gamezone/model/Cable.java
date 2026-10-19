package com.gamezone.model;

import java.util.List;

/**
 * Cable accessory sold by the store. On top of the data inherited from
 * Accessory it keeps its length in meters and the type of connector it uses.
 */
public class Cable extends Accessory {
    private double lengthInMeters;
    private String connectorType;

    /**
     * Creates a cable accessory.
     * @param id product identifier
     * @param title product title
     * @param price unit price
     * @param stock available stock
     * @param compatibleConsoleIds ids of the compatible consoles
     * @param lengthInMeters length of the cable in meters
     * @param connectorType type of connector, such as HDMI or USB
     */
    public Cable(String id, String title, double price, int stock,
                 List<String> compatibleConsoleIds, double lengthInMeters, String connectorType) {
        super(id, title, price, stock, compatibleConsoleIds);
        this.lengthInMeters = lengthInMeters;
        this.connectorType = connectorType;
    }

    /**
     * Returns the type marker of this accessory.
     * @return CABLE
     */
    @Override
    public String getAccessoryType() {
        return "CABLE";
    }

    /**
     * Returns the columns that only a cable has, in the order the accessory file
     * expects them: how long it is and how it connects. A null is written as an
     * empty string, so the word {@code null} never reaches the file.
     * @return a two element list with the length and the connector type
     */
    @Override
    public List<String> getExtraFields() {
        return List.of(String.valueOf(lengthInMeters),
                connectorType == null ? "" : connectorType);
    }

    /**
     * Builds the description of this cable by adding its length and its
     * connector to the description of the common accessory data.
     * @return description ready to be shown to the user
     */
    @Override
    public String getDescription() {
        return super.getDescription()
                + " | Longitud: " + lengthInMeters + " m"
                + " | Conector: " + connectorType;
    }

    /**
     * Returns the length of this cable.
     * @return length of the cable in meters
     */
    public double getLengthInMeters() {
        return lengthInMeters;
    }

    /**
     * Changes the length of this cable.
     * @param lengthInMeters new length in meters
     */
    public void setLengthInMeters(double lengthInMeters) {
        this.lengthInMeters = lengthInMeters;
    }

    /**
     * Returns the connector type of this cable.
     * @return connector type of the cable
     */
    public String getConnectorType() {
        return connectorType;
    }

    /**
     * Changes the connector type of this cable.
     * @param connectorType new connector type
     */
    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }
}
