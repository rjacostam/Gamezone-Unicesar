package com.gamezone.model;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Promotion that applies a percentage to the whole sale only when the sale
 * reaches a minimum number of units. Below that quantity the promotion does not
 * apply at all and the discount is zero.
 */
public class BulkPurchaseDiscount extends Promotion {
    private int minimumQuantity;
    private double percentage;

    /**
     * Creates a bulk purchase discount promotion.
     * @param id promotion identifier
     * @param name promotion name
     * @param startDate first day the promotion applies, included
     * @param endDate last day the promotion applies, included
     * @param minimumQuantity units the sale must have for the discount to apply
     * @param percentage discount percentage, greater than zero and up to 100
     */
    public BulkPurchaseDiscount(String id, String name, LocalDate startDate, LocalDate endDate,
                                int minimumQuantity, double percentage) {
        super(id, name, startDate, endDate);
        this.minimumQuantity = minimumQuantity;
        this.percentage = percentage;
    }

    /**
     * Returns the discount for the given sale. The percentage is applied to the
     * sum of the prices of every unit, but only when the sale has at least the
     * minimum quantity of units; in any other case the discount is zero.
     * @param sale sale to evaluate
     * @return discount in pesos, zero when the promotion is not active or the
     *         sale is below the minimum quantity
     */
    @Override
    public double calculateDiscount(Sale sale) {
        if (sale == null || !isActive(sale.getDate())) {
            return 0;
        }
        if (sale.getProducts().size() < minimumQuantity) {
            return 0;
        }
        double base = 0;
        for (Product product : sale.getProducts()) {
            base += product.getPrice();
        }
        return base * percentage / 100.0;
    }

    /**
     * Builds the line shown when the promotions are listed, explaining how many
     * units are needed and the percentage applied.
     * @return description ready to be shown to the user
     */
    @Override
    public String getDescription() {
        return "[" + getId() + "] " + getName()
                + " | " + getStartDate() + " a " + getEndDate()
                + " | " + String.format(Locale.US, "%.2f%%", percentage)
                + " al comprar " + minimumQuantity + " o mas";
    }

    /**
     * Returns the marker of this promotion type, the one already written in the
     * promotion file.
     * @return BULK
     */
    @Override
    public String getTypeCode() {
        return "BULK";
    }

    /**
     * Returns how many units the sale must have for the discount to apply.
     * @return minimum quantity of units
     */
    public int getMinimumQuantity() {
        return minimumQuantity;
    }

    /**
     * Changes how many units the sale must have for the discount to apply.
     * @param minimumQuantity new minimum quantity of units
     */
    public void setMinimumQuantity(int minimumQuantity) {
        this.minimumQuantity = minimumQuantity;
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
