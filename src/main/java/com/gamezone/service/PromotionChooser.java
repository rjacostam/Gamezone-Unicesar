package com.gamezone.service;

import com.gamezone.model.Promotion;
import com.gamezone.model.Sale;

/**
 * Contract of the promotions module that a sale needs.
 *
 * <p>Part of the correction of violation V-11: a sale needs the promotion that
 * gives the best discount and nothing else, so it does not depend on the whole
 * {@code PromotionService} with its three registration methods.
 */
public interface PromotionChooser {

    /**
     * Finds the promotion that discounts a sale the most.
     *
     * @param sale sale being registered
     * @return the best promotion, or {@code null} when none applies
     */
    Promotion findBestPromotionFor(Sale sale);
}