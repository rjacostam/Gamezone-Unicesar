package com.gamezone.service;

import com.gamezone.model.BasicWarranty;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;

import java.time.LocalDate;

/**
 * Contract of the warranties module that a sale needs.
 *
 * <p>Part of the correction of violation V-11: a sale grants warranties and never
 * cancels one, so it depends on these two operations and not on the cancellation
 * the return module uses. Keeping the two apart is what stops this correction
 * from creating a new Interface Segregation violation.
 */
public interface WarrantyAssigner {

    /**
     * Grants the basic warranty of a product in a sale.
     *
     * @param product   product the warranty covers
     * @param sale      sale the warranty belongs to
     * @param startDate first day of the coverage
     * @return the warranty granted
     */
    BasicWarranty assignBasicWarranty(Product product, Sale sale, LocalDate startDate);

    /**
     * Grants the extended warranty of a product in a sale.
     *
     * @param product   product the warranty covers
     * @param sale      sale the warranty belongs to
     * @param startDate first day of the coverage
     * @return the warranty granted
     */
    ExtendedWarranty assignExtendedWarranty(Product product, Sale sale, LocalDate startDate);
}