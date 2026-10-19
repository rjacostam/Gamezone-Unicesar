package com.gamezone.service;

import com.gamezone.model.Product;

/**
 * Common contract of the services that own the stock of the items the store sells.
 *
 * <p>The catalogue and the accessories are two separate modules with two separate
 * services, but a sale and a return have to update the stock of an item without
 * asking first which kind of item it is. Implementing this interface lets those
 * services hold one list of catalogues and ask each catalogue to change its own
 * stock, instead of holding one collaborator per module and testing the concrete
 * type of every item.
 *
 * <p>This is the interface agreed in the correction of violation V-07 of the
 * consolidated GRASP audit, and it removes the type tests that V-07 registered in
 * {@code SaleService.updateInventory} and {@code ReturnService.restoreStock}.
 *
 * <p>Implemented by {@link ProductService} and {@link AccessoryService}. Both keep
 * their own rules and their own file; the interface only says that a catalogue can
 * be searched and that its stock can be taken out or given back.
 *
 * @see ProductService
 * @see AccessoryService
 */
public interface InventoryCatalog {

    /**
     * Searches the item that carries an identifier, ignoring letter case, so a
     * user can type "vg-01" and still find the item registered as "VG-01".
     *
     * @param id identifier to look for
     * @return the item found, or {@code null} when this catalogue has no item
     *         with that identifier
     */
    Product findById(String id);

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