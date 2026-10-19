package com.gamezone.model;

import java.time.LocalDate;

/**
 * Extended warranty the customer can ask for when buying a console. It lasts
 * twelve months from the date of the sale, it also covers accidental damage,
 * and it adds ten percent of the price of the product to the sale.
 */
public class ExtendedWarranty extends Warranty {

    /**
     * Creates an extended warranty for a product sold in a sale.
     * @param id warranty identifier
     * @param product product covered by the warranty
     * @param sale sale the warranty comes from
     * @param startDate first day the warranty is in force
     */
    public ExtendedWarranty(String id, Product product, Sale sale, LocalDate startDate) {
        super(id, product, sale, startDate);
    }

    /**
     * Returns how long this warranty lasts.
     * @return twelve months
     */
    @Override
    public int getDurationInMonths() {
        return 12;
    }

    /**
     * Returns the name of this warranty type.
     * @return name of the warranty type
     */
    @Override
    public String getWarrantyType() {
        return "Garantía Extendida";
    }

    /**
     * Returns the cost this warranty adds to the sale, which is ten percent of
     * the price of the product covered. It is zero when there is no product.
     * @return ten percent of the product price
     */
    @Override
    public double getAdditionalCost() {
        Product covered = getProduct();
        if (covered == null) {
            return 0.0;
        }
        return covered.getPrice() * 0.10;
    }

    /**
     * Returns the marker of this warranty type, the one already written in the
     * warranty file.
     * @return EXTENDED
     */
    @Override
    public String getTypeCode() {
        return "EXTENDED";
    }
}
