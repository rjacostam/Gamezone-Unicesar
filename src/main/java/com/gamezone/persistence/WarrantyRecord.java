package com.gamezone.persistence;

import java.time.LocalDate;

/**
 * Plain data holder for one line of the warranties file.
 * <p>
 * The record keeps **only identifiers**, never objects. It exists because the
 * persistence layer must not build the domain objects: a warranty points to a
 * sale and to a product, and the objects that own those links live in the other
 * layers. Reading the file therefore produces records, and
 * {@code WarrantyService} is the class that turns each record into a real
 * {@code Warranty}, looking the sale and the product up among the objects that
 * are already loaded.
 * <p>
 * The class has no behaviour beyond storing its values and handing them back.
 * All the fields are final, so a record cannot change after it is created, and
 * there are no setters.
 */
public class WarrantyRecord {

    /** Type marker of the warranty, {@code BASIC} or {@code EXTENDED}. */
    private final String type;

    /** Identifier of the warranty. */
    private final String id;

    /** Identifier of the product the warranty covers. */
    private final String productId;

    /** Identifier of the sale the warranty comes from. */
    private final String saleId;

    /** First day the warranty is valid. */
    private final LocalDate startDate;

    /**
     * Builds a record with the values read from one line of the file.
     *
     * @param type      type marker of the warranty
     * @param id        identifier of the warranty
     * @param productId identifier of the covered product
     * @param saleId    identifier of the original sale
     * @param startDate first day the warranty is valid
     */
    public WarrantyRecord(String type, String id, String productId, String saleId,
                          LocalDate startDate) {
        this.type = type;
        this.id = id;
        this.productId = productId;
        this.saleId = saleId;
        this.startDate = startDate;
    }

    /**
     * Returns the type marker of the warranty.
     *
     * @return type marker of the warranty
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the identifier of the warranty.
     *
     * @return identifier of the warranty
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the identifier of the product the warranty covers.
     *
     * @return identifier of the covered product
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Returns the identifier of the sale the warranty comes from.
     *
     * @return identifier of the original sale
     */
    public String getSaleId() {
        return saleId;
    }

    /**
     * Returns the first day the warranty is valid.
     *
     * @return first day the warranty is valid
     */
    public LocalDate getStartDate() {
        return startDate;
    }
}