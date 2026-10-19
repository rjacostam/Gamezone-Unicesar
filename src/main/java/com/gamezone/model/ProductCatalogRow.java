package com.gamezone.model;

import java.util.List;

/**
 * Contract of the items that are stored in the product catalogue file,
 * `data/products.csv`, with the columns that follow the common ones.
 *
 * <p>This contract was created by the correction of violation V-08 of the SOLID
 * audit, which found that {@code Product} declared a single
 * {@code getExtraFields()} that its two hierarchies answered with two different
 * meanings: {@code VideoGame} and {@code Console} answered with the columns of
 * {@code products.csv}, and the accessories with the columns of
 * {@code accessories.csv}. A caller holding a {@code Product} could not tell
 * which file the returned columns belonged to.
 *
 * <p>Each hierarchy now declares only the file it is stored in. This interface
 * is the one of the product catalogue, it is answered by {@code VideoGame} and
 * {@code Console}, and {@code ProductRepository} is its only reader. The
 * accessories use {@link AccessoryCatalogRow} and the promotions use
 * {@link PromotionCatalogRow}.
 */
public interface ProductCatalogRow {

    /**
     * Returns the three columns that follow the common ones in a row of the
     * product catalogue, in the order the file expects them: the platform, the
     * genre and the age rating of a video game, and the brand, the model and the
     * generation of a console.
     *
     * @return the three extra columns of this row, in file order
     */
    List<String> getExtraFields();
}
