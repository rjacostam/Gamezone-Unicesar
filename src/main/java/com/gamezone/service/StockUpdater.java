package com.gamezone.service;

/**
 * Contract of a catalogue whose stock can go down, which is what a sale does.
 *
 * <p>It extends {@link ItemFinder} because a catalogue that takes units out of
 * its stock has to answer for them first. The return module never uses it, so
 * after the correction of violation V-09 that module depends on
 * {@link StockRestorer} instead and is not forced to implement this one.
 *
 * @see ItemFinder
 * @see StockRestorer
 */
public interface StockUpdater extends ItemFinder {

    /**
     * Removes sold units from the stock of an item and saves the catalogue.
     *
     * <p>The quantity is what the store sells, so the value is subtracted from
     * the stock. The item itself rejects the update when the quantity is
     * negative or when the stock is not enough, so an implementation does not
     * repeat that check.
     *
     * @param id       identifier of the item to update
     * @param quantity units being removed from the stock
     * @throws com.gamezone.exceptions.ResourceNotFoundException if this catalogue
     *                                                          has no item with that identifier
     * @throws com.gamezone.exceptions.BusinessRuleException     if the quantity is negative
     *                                                          or if the stock is not enough
     * @throws com.gamezone.exceptions.PersistenceException      if the file of this
     *                                                          catalogue cannot be written
     */
    void updateStock(String id, int quantity);
}