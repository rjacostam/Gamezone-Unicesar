package com.gamezone.service;

import com.gamezone.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Catalogue of everything the store sells, made of the catalogues of each module (products
 * and accessories).
 * <p>
 * It only knows each catalogue through two abstractions: a supplier of its list of items and
 * an {@link ItemFinder} that searches it. Adding a new kind of item to the store means giving
 * this class one more supplier and one more finder, without changing the sale service.
 */
public class StoreCatalog implements ItemFinder {

    private final List<Supplier<List<? extends Product>>> listings;
    private final List<ItemFinder> finders;

    /**
     * Creates the catalogue from the catalogues of the store, in the order their items are
     * listed and searched.
     *
     * @param listings suppliers of the items of each catalogue
     * @param finders  searches of each catalogue, in search order
     */
    public StoreCatalog(List<Supplier<List<? extends Product>>> listings, List<ItemFinder> finders) {
        this.listings = List.copyOf(listings);
        this.finders = List.copyOf(finders);
    }

    /**
     * Returns every item the store sells, catalogue after catalogue.
     *
     * @return products followed by accessories
     */
    public List<Product> listAll() {
        List<Product> catalog = new ArrayList<>();
        for (Supplier<List<? extends Product>> listing : listings) {
            catalog.addAll(listing.get());
        }
        return catalog;
    }

    /**
     * Searches an item in the catalogues, in order, and returns the first one found.
     *
     * @param id identifier of the item
     * @return the item, or {@code null} when no catalogue holds it
     */
    @Override
    public Product findById(String id) {
        for (ItemFinder finder : finders) {
            Product item = finder.findById(id);
            if (item != null) {
                return item;
            }
        }
        return null;
    }
}
