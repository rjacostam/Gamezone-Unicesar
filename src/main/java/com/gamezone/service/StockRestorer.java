package com.gamezone.service;

/**
 * Contract of a catalogue whose stock can go back up, which is what a return
 * does.
 *
 * <p>It extends {@link ItemFinder} because a catalogue that gives units back
 * has to answer for them first. The sale module never uses it, so after the
 * correction of violation V-09 that module depends on {@link StockUpdater}
 * instead and is not forced to implement this one.
 *
 * @see ItemFinder
 * @see StockUpdater
 */
public interface StockRestorer extends ItemFinder {

    /**
     * Adds returned units back to the stock of an item and saves the catalogue.
     * The inventory of a catalogue is only changed through this method, so the
     * stock of an item is always changed in one place per module.
     *
     * @param id       identifier of the item to update
     * @param quantity amount of units to add to the stock
     * @throws com.gamezone.exceptions.InvalidDataException      if the quantity is not
     *                                                            greater than zero
     * @throws com.gamezone.exceptions.ResourceNotFoundException if this catalogue
     *                                                          has no item with that identifier
     * @throws com.gamezone.exceptions.PersistenceException      if the file of this
     *                                                          catalogue cannot be written
     */
    void restoreStock(String id, int quantity);
}