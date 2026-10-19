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
 * <p>
 * The refunded amount has two parts: what is owed for the returned units, and
 * what is owed for the warranties that were cancelled because of this return. A
 * console that comes back can no longer be covered, so the cost of its extended
 * warranty is given back to the customer. The basic warranty has no cost, so it
 * adds nothing.
 */
public class Return {
    private String id;
    private LocalDate date;
    private Sale sale;
    private List<Product> returnedProducts;
    private String reason;
    private double warrantyRefund;
    private double refundAmount;

    /**
     * Creates a return, keeps a copy of the returned units and computes the
     * amount to refund.
     * @param id return identifier
     * @param date date of the return
     * @param sale original sale the return comes from
     * @param returnedProducts units returned, one entry per unit
     * @param reason reason of the return
     * @param warrantyRefund amount to give back for the cancelled warranties,
     *                       zero when the return cancels none
     */
    public Return(String id, LocalDate date, Sale sale,
                  List<Product> returnedProducts, String reason, double warrantyRefund) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El identificador de la devolucion es obligatorio");
        }
        if (sale == null) {
            throw new IllegalArgumentException("La venta original es obligatoria");
        }
        if (returnedProducts == null || returnedProducts.isEmpty()) {
            throw new IllegalArgumentException("La devolucion debe tener al menos un producto");
        }
        this.id = id;
        this.date = date;
        this.sale = sale;
        this.returnedProducts = new ArrayList<>(returnedProducts);
        this.reason = reason;
        this.warrantyRefund = warrantyRefund;
        calculateRefundAmount();
    }

    /**
     * Adds up what is really owed to the customer for the returned units and adds
     * what is owed for the cancelled warranties, stores the result in the
     * refunded amount and returns it. Each unit is refunded at its list price
     * minus the same proportion of the discount the customer got in the original
     * sale, because the store never charged the full price for it. When the
     * original sale has no subtotal, or no discount, nothing is discounted. It is
     * called by the constructor, so a return always knows how much was refunded.
     * @return refunded amount in pesos
     */
    public double calculateRefundAmount() {
        double total = warrantyRefund;
        for (Product product : returnedProducts) {
            total += refundableAmount(product);
        }
        this.refundAmount = total;
        return total;
    }

    /**
     * Returns how much is owed for one returned unit, which is its list price
     * minus the proportional part of the discount applied in the original sale.
     * A sale without subtotal, without discount or with a null link gives the
     * full list price.
     * @param product returned unit
     * @return amount to refund for that unit
     */
    private double refundableAmount(Product product) {
        double listPrice = product.getPrice();
        return listPrice * (1 - discountRatio());
    }

    /**
     * Returns the proportion of the original sale that was discounted, which is
     * its discount divided by its subtotal. The result is never negative and
     * never above one, so a refunded amount can never go below zero.
     *
     * <p>It is public because the return receipt needs the same proportion to
     * print the discount of each returned unit, and the return is the class that
     * owns it. The receipt formatter asks for it here instead of recalculating it
     * from the sale.
     * @return discount ratio between zero and one
     */
    public double discountRatio() {
        if (sale == null) {
            return 0;
        }
        double subtotal = sale.getSubtotal();
        double discount = sale.getDiscountAmount();
        if (subtotal <= 0 || discount <= 0) {
            return 0;
        }
        return Math.min(discount / subtotal, 1.0);
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
     * Returns the amount refunded for the warranties that were cancelled by this
     * return. It is zero when the return cancels none.
     * @return warranty amount refunded in pesos
     */
    public double getWarrantyRefund() {
        return warrantyRefund;
    }

    /**
     * Returns the amount refunded to the customer, which includes both the
     * returned units and the cancelled warranties.
     * @return refunded amount in pesos
     */
    public double getRefundAmount() {
        return refundAmount;
    }
}
