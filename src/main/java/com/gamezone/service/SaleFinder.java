package com.gamezone.service;

import com.gamezone.model.Sale;

import java.util.List;

/**
 * Read access to the registered sales, for the modules that only need to find them: the
 * returns, the monthly balance and the return submenu.
 * <p>
 * Those modules depend on this small contract instead of on the whole sale service, so they
 * never see the operations that register a sale.
 */
public interface SaleFinder {

    /**
     * Searches a sale by its identifier, ignoring letter case.
     *
     * @param saleId identifier of the sale
     * @return the sale, or {@code null} when no sale uses that identifier
     */
    Sale findById(String saleId);

    /**
     * Returns every registered sale.
     *
     * @return sales in the order they were registered
     */
    List<Sale> listAllSales();
}
