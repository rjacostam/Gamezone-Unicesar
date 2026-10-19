package com.gamezone.service;

import com.gamezone.model.Accessory;
import com.gamezone.model.Cable;
import com.gamezone.model.Controller;
import com.gamezone.model.Memory;

import java.util.List;

/**
 * Builds the concrete accessory of a given type.
 *
 * <p>This class is the correction of violation V-06 of the consolidated SOLID
 * audit. Before it, the list of kinds of accessory was written twice, once as
 * one {@code register...} method per kind in {@code AccessoryService} and once
 * as one branch per kind in {@code AccessoryRepository.deserialize}, so adding a
 * kind meant editing both and a missed branch wrote a record that could not be
 * read back. Now the list exists only here: the service registers whatever this
 * factory builds, and the repository rebuilds a record with the same call.
 *
 * <p>The extra values are received as the columns of the file, in the order the
 * accessory publishes them through {@code getExtraFields}, so a kind is described
 * once and the two paths cannot disagree about it.
 */
public final class AccessoryFactory {

    /** Marker of a controller record. */
    public static final String TYPE_CONTROLLER = "CONTROLLER";

    /** Marker of a cable record. */
    public static final String TYPE_CABLE = "CABLE";

    /** Marker of a memory record. */
    public static final String TYPE_MEMORY = "MEMORY";

    private AccessoryFactory() {
    }

    /**
     * Builds the accessory of the given type.
     *
     * @param type                  marker of the kind: {@link #TYPE_CONTROLLER},
     *                              {@link #TYPE_CABLE} or {@link #TYPE_MEMORY}
     * @param id                    accessory identifier
     * @param title                 accessory title
     * @param price                 unit price
     * @param stock                 units in stock
     * @param compatibleConsoleIds  consoles the accessory fits
     * @param extras                the columns the kind adds, in the order it
     *                              publishes them
     * @return the accessory built
     * @throws NumberFormatException if an extra column of the kind is a number
     *                                and cannot be read
     */
    public static Accessory create(String type, String id, String title, double price, int stock,
            List<String> compatibleConsoleIds, List<String> extras) {
        if (TYPE_CONTROLLER.equalsIgnoreCase(type)) {
            return new Controller(id, title, price, stock, compatibleConsoleIds,
                    valueAt(extras, 0));
        }
        if (TYPE_CABLE.equalsIgnoreCase(type)) {
            return new Cable(id, title, price, stock, compatibleConsoleIds,
                    Double.parseDouble(valueAt(extras, 0)), valueAt(extras, 1));
        }
        if (TYPE_MEMORY.equalsIgnoreCase(type)) {
            return new Memory(id, title, price, stock, compatibleConsoleIds,
                    Integer.parseInt(valueAt(extras, 0)), valueAt(extras, 1));
        }
        return null;
    }

    /**
     * Returns one of the extra columns, or an empty string when the record does
     * not have it, so a shorter row is read the same way it was written.
     *
     * @param extras extra columns of the record
     * @param index  position of the wanted column
     * @return the value, or an empty string
     */
    private static String valueAt(List<String> extras, int index) {
        if (extras == null || index >= extras.size()) {
            return "";
        }
        return extras.get(index);
    }
}