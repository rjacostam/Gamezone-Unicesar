package com.gamezone.model;

import com.gamezone.format.AmountFormat;

import java.time.LocalDate;
import java.util.List;

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
        double base = sale.getSubtotal();
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
                + " | " + AmountFormat.percent(percentage) + " sobre el total";
    }

    /**
     * Returns the marker of this promotion type, the one already written in the
     * promotion file.
     * @return PERCENTAGE
     */
    @Override
    public String getTypeCode() {
        return "PERCENTAGE";
    }

    /**
     * Returns the two columns that only this promotion writes after its
     * percentage. A percentage discount has no second value, so the second column
     * is empty, exactly as the promotion file already stores it.
     * @return a two element list with the percentage and an empty string
     */
    @Override
    public List<String> getExtraFields() {
        return List.of(String.valueOf(percentage), "");
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
