package com.gamezone.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Return of one or more products of a sale already registered in the store. The
 * return keeps a reference to the original sale, the units the customer gave
 * back, the reason and the amount that was refunded. The refunded amount is
 * computed when the object is created, so it is always available. There are only
 * getters, because a registered return is never modified afterwards.
 */
public class Return {
    private String id;
    private LocalDate date;
    private Sale sale;
    private List<Product> returnedProducts;
    private String reason;
    private double refundAmount;

    /**
     * Creates a return, keeps a copy of the returned units and computes the
     * amount to refund.
     * @param id return identifier
     * @param date date of the return
     * @param sale original sale the return comes from
     * @param returnedProducts units returned, one entry per unit
     * @param reason reason of the return
     */
    public Return(String id, LocalDate date, Sale sale,
                  List<Product> returnedProducts, String reason) {
        this.id = id;
        this.date = date;
        this.sale = sale;
        this.returnedProducts = new ArrayList<>(returnedProducts);
        this.reason = reason;
        calculateRefundAmount();
    }

    /**
     * Adds up the prices of the returned units, stores the result in the
     * refunded amount and returns it. It is called by the constructor, so a
     * return always knows how much was refunded.
     * @return refunded amount in pesos
     */
    public double calculateRefundAmount() {
        double total = 0;
        for (Product product : returnedProducts) {
            total += product.getPrice();
        }
        this.refundAmount = total;
        return total;
    }

    /**
     * Returns the identifier of this return.
     * @return return identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the date of this return.
     * @return date of the return
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Returns the original sale this return comes from.
     * @return original sale
     */
    public Sale getSale() {
        return sale;
    }

    /**
     * Returns a copy of the units returned, so the list can be read but not
     * changed from outside the class.
     * @return copy of the returned units
     */
    public List<Product> getReturnedProducts() {
        return new ArrayList<>(returnedProducts);
    }

    /**
     * Returns the reason of this return.
     * @return reason of the return
     */
    public String getReason() {
        return reason;
    }

    /**
     * Returns the amount refunded to the customer.
     * @return refunded amount in pesos
     */
    public double getRefundAmount() {
        return refundAmount;
    }
}
