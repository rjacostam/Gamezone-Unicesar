package com.gamezone.model;


import java.time.LocalDate;
import java.util.List;

/**
 * Builds the concrete promotion of a given type.
 *
 * <p>This class is the correction of violation V-06 of the consolidated SOLID
 * audit, on the promotions side. Before it, the list of kinds of promotion was
 * written twice, once as one {@code register...} method per kind in
 * {@code PromotionService} and once as one branch per kind in
 * {@code PromotionRepository.deserialize}. Now the list exists only here: the
 * service registers whatever this factory builds, and the repository rebuilds a
 * record with the same call.
 *
 * <p>The extra values are received as the columns of the file, in the order the
 * promotion publishes them through {@code getExtraFields}, so a kind is described
 * once and the two paths cannot disagree about it.
 */
public final class PromotionFactory {

    /** Marker of a percentage discount record. */
    public static final String TYPE_PERCENTAGE = "PERCENTAGE";

    /** Marker of a category discount record. */
    public static final String TYPE_CATEGORY = "CATEGORY";

    /** Marker of a bulk purchase discount record. */
    public static final String TYPE_BULK = "BULK";

    private PromotionFactory() {
    }

    /**
     * Builds the promotion of the given type.
     *
     * @param type      marker of the kind: {@link #TYPE_PERCENTAGE},
     *                  {@link #TYPE_CATEGORY} or {@link #TYPE_BULK}
     * @param id        promotion identifier
     * @param name      promotion name
     * @param startDate first day the promotion applies
     * @param endDate   last day the promotion applies
     * @param extras    the columns the kind adds, in the order it publishes them
     * @return the promotion built
     * @throws NumberFormatException if an extra column of the kind is a number
     *                                and cannot be read
     */
    public static Promotion create(String type, String id, String name, LocalDate startDate,
            LocalDate endDate, List<String> extras) {
        if (TYPE_PERCENTAGE.equalsIgnoreCase(type)) {
            return new PercentageDiscount(id, name, startDate, endDate,
                    Double.parseDouble(valueAt(extras, 0)));
        }
        if (TYPE_CATEGORY.equalsIgnoreCase(type)) {
            return new CategoryDiscount(id, name, startDate, endDate,
                    Double.parseDouble(valueAt(extras, 0)), valueAt(extras, 1));
        }
        if (TYPE_BULK.equalsIgnoreCase(type)) {
            return new BulkPurchaseDiscount(id, name, startDate, endDate,
                    Integer.parseInt(valueAt(extras, 1)), Double.parseDouble(valueAt(extras, 0)));
        }
        return null;
    }

    /**
     * Returns one of the extra columns, or an empty string when the record does
     * not have it, so a shorter row is read the same way it was written.
     *
     * @param extras extra columns of the record
     * @param index  position of the wanted column
     * @return the value, or an empty string
     */
    private static String valueAt(List<String> extras, int index) {
        if (extras == null || index >= extras.size()) {
            return "";
        }
        return extras.get(index);
    }
}