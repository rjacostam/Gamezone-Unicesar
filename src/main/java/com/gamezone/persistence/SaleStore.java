package com.gamezone.persistence;

import com.gamezone.model.Customer;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;

import java.util.List;

/**
 * Storage of the sales, as the sale service needs it: load every stored sale and save the
 * whole list again.
 * <p>
 * The contract lives in the persistence layer, so the service depends on an abstraction of
 * this layer and never on the class that knows the file format.
 */
public interface SaleStore {

    /**
     * Loads the stored sales, linking each one to the products, customers and sellers already
     * loaded.
     *
     * @param products  products and accessories of the store
     * @param customers customers of the store
     * @param sellers   sellers of the store
     * @return stored sales, empty when nothing was saved yet
     */
    List<Sale> loadAll(List<Product> products, List<Customer> customers, List<Seller> sellers);

    /**
     * Saves every sale, replacing the previous content of the storage.
     *
     * @param sales sales to save
     */
    void saveAll(List<Sale> sales);
}
