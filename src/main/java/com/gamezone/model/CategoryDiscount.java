package com.gamezone.model;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Promotion that applies a percentage only to the products of one category. The
 * base of the discount is the sum of the prices of the units whose category
 * matches the target category, and the percentage is applied only to that sum,
 * so the products of other categories are not discounted.
 *
 * <p>The three categories of the store are accepted: {@code VIDEOGAME},
 * {@code CONSOLE} and, since adjustment A1, {@code ACCESSORY}. The category of
 * every product comes from {@link Product#getCategory()}, and every accessory
 * answers {@code ACCESSORY}, so accessories are recognised without changing this
 * class when they were added to the system.</p>
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
     * @param targetCategory category the discount applies to: {@code VIDEOGAME},
     *                          {@code CONSOLE} or {@code ACCESSORY}
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
     * applied to that base alone. Accessories are included when the target
     * category is {@code ACCESSORY}, because their category comes from the
     * same {@link Product#getCategory()} method as the other products.
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
                + " | " + String.format(Locale.US, "%.2f%%", percentage) + " solo en " + targetCategory;
    }

    /**
     * Returns the marker of this promotion type, the one already written in the
     * promotion file.
     * @return CATEGORY
     */
    @Override
    public String getTypeCode() {
        return "CATEGORY";
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
     * @return target category of the promotion: {@code VIDEOGAME},
     *         {@code CONSOLE} or {@code ACCESSORY}
     */
    public String getTargetCategory() {
        return targetCategory;
    }

    /**
     * Changes the category this promotion discounts.
     * @param targetCategory new target category: {@code VIDEOGAME},
     *                       {@code CONSOLE} or {@code ACCESSORY}
     */
    public void setTargetCategory(String targetCategory) {
        this.targetCategory = targetCategory;
    }
}
