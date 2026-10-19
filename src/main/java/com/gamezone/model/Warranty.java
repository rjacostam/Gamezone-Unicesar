package com.gamezone.model;

import java.time.LocalDate;
import java.util.Locale;

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
     * Returns the marker of this warranty type, which is the text the warranty
     * writes in the data file. Each warranty type declares its own marker, so the
     * repository does not have to find out which type it is holding. It is not the
     * name the user reads: {@link #getWarrantyType()} answers that one.
     * @return type marker of this warranty
     */
    public abstract String getTypeCode();

    /**
     * Tells whether the warranty is in force on the given date, which happens
     * when the date is between the start date and the end date, both included.
     * It returns false when the date is missing or when the warranty has no
     * period at all.
     * @param date date to check
     * @return true when the warranty covers that date
     */
    public boolean isActive(LocalDate date) {
        if (date == null || startDate == null || endDate == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

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

    /**
     * Builds the certificate the customer receives, in Spanish. It shows the
     * identifier and the type of the warranty, the product it covers, the sale
     * it comes from with the customer who bought it, the period it is in force,
     * what it covers and whether it is still active today.
     * @return certificate ready to be shown to the user
     */
    public String generateWarrantyCertificate() {
        boolean active = isActive(LocalDate.now());
        StringBuilder certificate = new StringBuilder();
        certificate.append("=== GameZone Unicesar - Certificado de garantia ===\n");
        certificate.append("Garantia: ").append(id)
                .append(" (").append(getWarrantyType()).append(")\n");
        if (product != null) {
            certificate.append("Producto: ").append(product.getId())
                    .append(" - ").append(product.getTitle()).append("\n");
        }
        if (sale != null) {
            certificate.append("Venta: ").append(sale.getId()).append("\n");
            if (sale.getCustomer() != null) {
                certificate.append("Cliente: ").append(sale.getCustomer().getName()).append("\n");
            }
        }
        certificate.append("Inicio: ").append(startDate)
                .append(" | Fin: ").append(endDate).append("\n");
        certificate.append("Cobertura: ").append(coverageDescription()).append("\n");
        certificate.append("Costo adicional: ")
                .append(String.format(Locale.US, "$%,.2f", getAdditionalCost())).append("\n");
        certificate.append("Estado: ")
                .append(active ? "Vigente" : "No vigente").append("\n");
        return certificate.toString();
    }

    /**
     * Returns the text describing what this warranty covers. It is read from the
     * type name, so it stays correct without adding another abstract method to
     * the class diagram: only the extended warranty covers accidental damage.
     * @return coverage of this warranty, in Spanish
     */
    private String coverageDescription() {
        if ("Garantía Extendida".equals(getWarrantyType())) {
            return "defectos de fábrica y daños accidentales";
        }
        return "defectos de fábrica";
    }
}
