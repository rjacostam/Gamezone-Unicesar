package com.gamezone.model;

import java.util.List;

/**
 * Contract of the accessories that are stored in the accessory file,
 * `data/accessories.csv`, with the columns that follow the common ones.
 *
 * <p>This contract was created by the correction of violation V-08 of the SOLID
 * audit. It is one of the three narrow contracts that replaced the single
 * {@code getExtraFields()} that {@code Product} declared for two different
 * files. {@code Accessory} declares this one, so every accessory publishes the
 * columns of the file it is really stored in, and {@code AccessoryRepository}
 * is its only reader.
 *
 * <p>The number of columns differs per kind of accessory, because the file has
 * that layout: {@code Controller} contributes one column, its connection type,
 * while {@code Cable} and {@code Memory} contribute two, the length with the
 * connector type and the capacity with the memory type. All of them answer the
 * same question, "which columns of the accessory file are yours", so they share
 * one contract without sharing a meaning.
 *
 * @see ProductCatalogRow
 * @see PromotionCatalogRow
 */
public interface AccessoryCatalogRow {

    /**
     * Returns the columns that follow the common ones in a row of the accessory
     * file, in the order the file expects them. A null is written as an empty
     * string, so the word {@code null} never reaches the file.
     *
     * @return the extra columns of this row, in file order
     */
    List<String> getExtraFields();
}
