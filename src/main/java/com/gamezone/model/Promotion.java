package com.gamezone.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Base class for the promotions of the store. A promotion has an identifier, a
 * name and a validity period, and it is only applied while the date of the sale
 * falls inside that period. Each promotion type declares how its discount is
 * calculated and how it is described, because the rule is different in each
 * case.
 */
public abstract class Promotion implements PromotionCatalogRow {
    private String id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;

    /**
     * Creates a promotion with its identifier, its name and its validity period.
     * @param id promotion identifier
     * @param name promotion name
     * @param startDate first day the promotion applies, included
     * @param endDate last day the promotion applies, included
     */
    protected Promotion(String id, String name, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Calculates the discount this promotion would grant on the given sale. It
     * only reads the sale, it never modifies it. Each promotion type implements
     * it because the rule is different in each case.
     * @param sale sale to evaluate
     * @return discount in pesos, zero when the promotion does not apply
     */
    public abstract double calculateDiscount(Sale sale);

    /**
     * Builds the line shown when the promotions are listed, with the identifier,
     * the name, the validity period and the rule of the promotion.
     * @return description ready to be shown to the user
     */
    public abstract String getDescription();

    /**
     * Returns the marker of this promotion type, which is the text the promotion
     * writes in the data file. Each promotion type declares its own marker, so
     * the repository does not have to find out which type it is holding.
     * @return type marker of this promotion
     */
    public abstract String getTypeCode();

    /**
     * Tells whether the promotion applies on the given date, which happens when
     * the date is between the start date and the end date, both included.
     * @param date date to check
     * @return true when the promotion is in force on that date
     */
    public boolean isActive(LocalDate date) {
        if (date == null || startDate == null || endDate == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Returns the identifier of this promotion.
     * @return promotion identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the name of this promotion.
     * @return promotion name
     */
    public String getName() {
        return name;
    }

    /**
     * Changes the name of this promotion.
     * @param name new promotion name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the first day this promotion applies.
     * @return start date of the promotion
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Changes the first day this promotion applies.
     * @param startDate new start date
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * Returns the last day this promotion applies.
     * @return end date of the promotion
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Changes the last day this promotion applies.
     * @param endDate new end date
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
