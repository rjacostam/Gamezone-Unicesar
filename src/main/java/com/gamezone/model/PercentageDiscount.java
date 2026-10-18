package com.gamezone.model;

import java.time.LocalDate;

/**
 * Promotion that applies a percentage to the whole sale. It is the simplest
 * rule: the discount is the sum of the prices of the units in the sale, minus
 * the given percentage.
 */
public class PercentageDiscount extends Promotion {
    private double percentage;

    /**
     * Creates a percentage discount promotion.
     * @param id promotion identifier
     * @param name promotion name
     * @param startDate first day the promotion applies, included
     * @param endDate last day the promotion applies, included
     * @param percentage discount percentage, greater than zero and up to 100
     */
    public PercentageDiscount(String id, String name, LocalDate startDate, LocalDate endDate,
                              double percentage) {
        super(id, name, startDate, endDate);
        this.percentage = percentage;
    }

    /**
     * Returns the discount for the given sale, which is the percentage applied
     * to the sum of the prices of every unit in the sale.
     * @param sale sale to evaluate
     * @return discount in pesos, zero when the promotion is not active
     */
    @Override
    public double calculateDiscount(Sale sale) {
        if (sale == null || !isActive(sale.getDate())) {
            return 0;
        }
        double base = 0;
        for (Product product : sale.getProducts()) {
            base += product.getPrice();
        }
        return base * percentage / 100.0;
    }

    /**
     * Builds the line shown when the promotions are listed, explaining that the
     * percentage applies to the whole sale.
     * @return description ready to be shown to the user
     */
    @Override
    public String getDescription() {
        return "[" + getId() + "] " + getName()
                + " | " + getStartDate() + " a " + getEndDate()
                + " | " + String.format("%g%%", percentage) + " sobre el total";
    }

    /**
     * Returns the discount percentage of this promotion.
     * @return percentage applied to the sale
     */
    public double getPercentage() {
        return percentage;
    }

    /**
     * Changes the discount percentage of this promotion.
     * @param percentage new percentage
     */
    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}
