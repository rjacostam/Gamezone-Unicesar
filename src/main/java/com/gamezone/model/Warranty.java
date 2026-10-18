package com.gamezone.model;

import java.time.LocalDate;

/**
 * Base class for the warranties granted by the store. A warranty links the
 * product it covers with the sale it comes from, and keeps the period during
 * which it is in force. The end date is not typed by the caller: it is computed
 * in the constructor from the duration of the concrete warranty type, so the
 * duration is written in a single place. There are no setters because a granted
 * warranty never changes afterwards.
 */
public abstract class Warranty {
    private String id;
    private Product product;
    private Sale sale;
    private LocalDate startDate;
    private LocalDate endDate;

    /**
     * Creates a warranty and computes when it expires. The duration comes from
     * getDurationInMonths(), which each subclass answers with its own value, so
     * the end date is always consistent with the type of warranty.
     * @param id warranty identifier
     * @param product product covered by the warranty
     * @param sale sale the warranty comes from
     * @param startDate first day the warranty is in force
     */
    protected Warranty(String id, Product product, Sale sale, LocalDate startDate) {
        this.id = id;
        this.product = product;
        this.sale = sale;
        this.startDate = startDate;
        this.endDate = startDate != null
                ? startDate.plusMonths(getDurationInMonths())
                : null;
    }

    /**
     * Returns how long the warranty lasts, counted in months. The constructor
     * uses this value to compute the end date.
     * @return duration in months
     */
    public abstract int getDurationInMonths();

    /**
     * Returns the name of this warranty type, shown to the user and written to
     * the file that stores the warranties.
     * @return name of the warranty type
     */
    public abstract String getWarrantyType();

    /**
     * Returns the amount this warranty adds to the sale it comes from.
     * @return additional cost in pesos
     */
    public abstract double getAdditionalCost();

    /**
     * Returns the identifier of this warranty.
     * @return warranty identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the product covered by this warranty.
     * @return product covered by the warranty
     */
    public Product getProduct() {
        return product;
    }

    /**
     * Returns the sale this warranty comes from.
     * @return sale the warranty comes from
     */
    public Sale getSale() {
        return sale;
    }

    /**
     * Returns the first day this warranty is in force.
     * @return start date of the warranty
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Returns the last day this warranty is in force, computed in the
     * constructor from the duration of the warranty type.
     * @return end date of the warranty
     */
    public LocalDate getEndDate() {
        return endDate;
    }
}
