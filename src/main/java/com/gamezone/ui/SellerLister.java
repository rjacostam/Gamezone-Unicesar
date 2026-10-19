package com.gamezone.ui;

/**
 * Something that can print the sellers of the store on the console.
 * <p>
 * The sale submenu depends on this small contract to show the sellers before asking for
 * one, instead of depending on the whole people submenu.
 */
public interface SellerLister {

    /**
     * Prints the sellers of the store.
     */
    void listSellers();
}
