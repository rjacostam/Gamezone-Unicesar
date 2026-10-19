package com.gamezone.model;

import java.util.List;

/**
 * Contract of the promotions that are stored in the promotion file,
 * `data/promotions.csv`, with the two columns that follow the percentage.
 *
 * <p>This contract was created by the correction of violation V-08 of the SOLID
 * audit. It is one of the three narrow contracts that replaced the single
 * {@code getExtraFields()} that {@code Product} declared for two different
 * files. {@code Promotion} declares this one, so every promotion publishes the
 * columns of the promotion file and {@code PromotionRepository} is its only
 * reader.
 *
 * <p>All three promotion types answer the same question, "which two columns
 * follow the percentage in a row of the promotion file", so they share one
 * contract with one meaning: the target category for a category discount, the
 * minimum quantity for a bulk purchase discount, and an empty second column for
 * a percentage discount.
 *
 * @see ProductCatalogRow
 * @see AccessoryCatalogRow
 */
public interface PromotionCatalogRow {

    /**
     * Returns the two columns that follow the percentage in a row of the
     * promotion file, in the order the file expects them. A null is written as
     * an empty string, so the word {@code null} never reaches the file.
     *
     * @return the two extra columns of this row, in file order
     */
    List<String> getExtraFields();
}
