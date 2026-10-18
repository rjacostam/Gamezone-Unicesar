package com.gamezone.model;

import java.time.LocalDate;

/**
 * Promotion that applies a percentage only to the products of one category. The
 * base of the discount is the sum of the prices of the units whose category
 * matches the target category, and the percentage is applied only to that sum,
 * so the products of other categories are not discounted.
 */
public class CategoryDiscount extends Promotion {
    private double percentage;
    private String targetCategory;

    /**
     * Creates a category discount promotion.
     * @param id promotion identifier
     * @param name promotion name
     * @param startDate first day the promotion applies, included
     * @param endDate last day the promotion applies, included
     * @param percentage discount percentage, greater than zero and up to 100
     * @param targetCategory category the discount applies to
     */
    public CategoryDiscount(String id, String name, LocalDate startDate, LocalDate endDate,
                            double percentage, String targetCategory) {
        super(id, name, startDate, endDate);
        this.percentage = percentage;
        this.targetCategory = targetCategory;
    }

    /**
     * Returns the discount for the given sale. Only the products whose category
     * equals the target category are added to the base, and the percentage is
     * applied to that base alone.
     * @param sale sale to evaluate
     * @return discount in pesos, zero when the promotion is not active or the
     *         sale has no product of the target category
     */
    @Override
    public double calculateDiscount(Sale sale) {
        if (sale == null || !isActive(sale.getDate())) {
            return 0;
        }
        double base = 0;
        for (Product product : sale.getProducts()) {
            if (targetCategory.equalsIgnoreCase(product.getCategory())) {
                base += product.getPrice();
            }
        }
        return base * percentage / 100.0;
    }

    /**
     * Builds the line shown when the promotions are listed, explaining that the
     * percentage applies only to one category.
     * @return description ready to be shown to the user
     */
    @Override
    public String getDescription() {
        return "[" + getId() + "] " + getName()
                + " | " + getStartDate() + " a " + getEndDate()
                + " | " + String.format("%g%%", percentage) + " solo en " + targetCategory;
    }

    /**
     * Returns the discount percentage of this promotion.
     * @return percentage applied to the target category
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

    /**
     * Returns the category this promotion discounts.
     * @return target category of the promotion
     */
    public String getTargetCategory() {
        return targetCategory;
    }

    /**
     * Changes the category this promotion discounts.
     * @param targetCategory new target category
     */
    public void setTargetCategory(String targetCategory) {
        this.targetCategory = targetCategory;
    }
}
