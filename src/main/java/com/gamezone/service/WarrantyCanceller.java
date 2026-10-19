package com.gamezone.service;

/**
 * Contract of the warranties module that a return needs.
 *
 * <p>Part of the correction of violation V-11: a return cancels the warranties of
 * the units that come back and never grants one, so it depends on this single
 * operation instead of on the whole {@code WarrantyService}, which also lists
 * warranties and generates their identifiers.
 */
public interface WarrantyCanceller {

    /**
     * Cancels the warranties of a product in a sale and sums what has to be given
     * back for them.
     *
     * @param productId identifier of the product that came back
     * @param saleId    identifier of the sale it belonged to
     * @return total to give back for the cancelled warranties
     */
    double cancelWarranties(String productId, String saleId);
}