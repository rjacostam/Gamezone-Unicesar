package com.gamezone.model;

import java.time.LocalDate;

/**
 * Basic warranty granted with every console sold. It lasts six months from the
 * date of the sale and only covers factory defects, and it adds no cost to the
 * sale.
 */
public class BasicWarranty extends Warranty {

    /**
     * Creates a basic warranty for a product sold in a sale.
     * @param id warranty identifier
     * @param product product covered by the warranty
     * @param sale sale the warranty comes from
     * @param startDate first day the warranty is in force
     */
    public BasicWarranty(String id, Product product, Sale sale, LocalDate startDate) {
        super(id, product, sale, startDate);
    }

    /**
     * Returns how long this warranty lasts.
     * @return six months
     */
    @Override
    public int getDurationInMonths() {
        return 6;
    }

    /**
     * Returns the name of this warranty type.
     * @return name of the warranty type
     */
    @Override
    public String getWarrantyType() {
        return "Garantía Básica";
    }

    /**
     * Returns the cost this warranty adds to the sale, which is zero because the
     * basic warranty comes included with the console.
     * @return zero, the basic warranty is free
     */
    @Override
    public double getAdditionalCost() {
        return 0.0;
    }
}
