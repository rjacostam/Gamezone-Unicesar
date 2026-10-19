package com.gamezone.service;

import com.gamezone.model.Product;

/**
 * Contract of a catalogue that can look for one of the items it owns.
 *
 * <p>This is the smallest operation both clients of a catalogue need, so it is
 * the root of the three contracts that replaced {@code InventoryCatalog} in the
 * correction of violation V-09: the sale path needs this one and
 * {@code StockUpdater}, the return path needs this one and
 * {@code StockRestorer}. Each client therefore depends on the two operations it
 * uses and never on the third.
 *
 * @see StockUpdater
 * @see StockRestorer
 */
public interface ItemFinder {

    /**
     * Searches the item that carries an identifier, ignoring letter case, so a
     * user can type "vg-01" and still find the item registered as "VG-01". A
     * null or blank identifier answers {@code null}.
     *
     * @param id identifier to look for
     * @return the item found, or {@code null} when this catalogue has no item
     *         with that identifier
     */
    Product findById(String id);
}