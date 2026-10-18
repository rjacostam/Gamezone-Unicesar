package com.gamezone.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sale registered in the store.
 * <p>
 * Each entry in the product list is one unit sold, so a product that appears twice was
 * sold twice. The sale owns the product list and the discount applied to it, which makes
 * it the right place to compute its own subtotal and total.
 */
public class Sale {

    private String id;
    private LocalDate date;
    private Customer customer;
    private Seller seller;
    private List<Product> products;
    private double subtotal;
    private String appliedPromotionName;
    private double discountAmount;
    private double total;

    /**
     * Creates a sale without discount and computes its subtotal and total.
     *
     * @param id       unique sale identifier
     * @param date     date of the sale
     * @param customer customer who buys
     * @param seller   seller who serves the customer
     * @param products units sold, one entry per unit
     */
    public Sale(String id, LocalDate date, Customer customer, Seller seller, List<Product> products) {
        this.id = id;
        this.date = date;
        this.customer = customer;
        this.seller = seller;
        this.products = new ArrayList<>(products);
        this.appliedPromotionName = null;
        this.discountAmount = 0;
        calculateSubtotal();
        calculateTotal();
    }

    /**
     * Adds up the price of every unit sold and stores the result as the subtotal.
     *
     * @return subtotal
     */
    public double calculateSubtotal() {
        double sum = 0;
        for (Product product : products) {
            sum += product.getPrice();
        }
        subtotal = sum;
        return subtotal;
    }

    /**
     * Computes the final total: the subtotal minus the applied discount.
     *
     * @return final total
     */
    public double calculateTotal() {
        total = subtotal - discountAmount;
        return total;
    }

    /**
     * Records the promotion applied to the sale and updates the total.
     *
     * @param promotionName name of the applied promotion
     * @param amount        discount in pesos
     */
    public void applyDiscount(String promotionName, double amount) {
        this.appliedPromotionName = promotionName;
        this.discountAmount = amount;
        calculateTotal();
    }

    /**
     * Builds a printable receipt with the sale data, its products and its total.
     *
     * @return receipt text
     */
    public String generateReceipt() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("Venta ").append(id).append(" | Fecha: ").append(date).append('\n');
        receipt.append("  Cliente: ").append(customer.getName()).append(" (").append(customer.getId()).append(")\n");
        receipt.append("  Vendedor: ").append(seller.getName()).append(" (").append(seller.getEmployeeCode()).append(")\n");
        receipt.append("  Productos:\n");
        for (Product product : products) {
            receipt.append(String.format("    - %s %s: $%,.2f%n", product.getId(), product.getTitle(), product.getPrice()));
        }
        receipt.append(String.format("  Total: $%,.2f", total));
        return receipt.toString();
    }

    /**
     * Returns the sale identifier.
     *
     * @return identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the date of the sale.
     *
     * @return date
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Returns the customer of the sale.
     *
     * @return customer
     */
    public Customer getCustomer() {
        return customer;
    }

    /**
     * Returns the seller who registered the sale.
     *
     * @return seller
     */
    public Seller getSeller() {
        return seller;
    }

    /**
     * Returns the units sold.
     *
     * @return read-only product list
     */
    public List<Product> getProducts() {
        return Collections.unmodifiableList(products);
    }

    /**
     * Returns the sale total.
     *
     * @return total
     */
    public double getTotal() {
        return total;
    }

    /**
     * Returns the sum of the prices of every unit sold, before the discount.
     *
     * @return subtotal
     */
    public double getSubtotal() {
        return subtotal;
    }

    /**
     * Returns the name of the applied promotion.
     *
     * @return promotion name, or {@code null} if no promotion was applied
     */
    public String getAppliedPromotionName() {
        return appliedPromotionName;
    }

    /**
     * Changes the name of the applied promotion.
     *
     * @param appliedPromotionName promotion name
     */
    public void setAppliedPromotionName(String appliedPromotionName) {
        this.appliedPromotionName = appliedPromotionName;
    }

    /**
     * Returns the discount applied to the sale.
     *
     * @return discount in pesos, {@code 0} if there is none
     */
    public double getDiscountAmount() {
        return discountAmount;
    }

    /**
     * Changes the discount and updates the total.
     *
     * @param discountAmount discount in pesos
     */
    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
        calculateTotal();
    }
}
