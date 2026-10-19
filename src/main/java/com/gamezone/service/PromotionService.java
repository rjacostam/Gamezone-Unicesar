package com.gamezone.service;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.Promotion;
import com.gamezone.model.PromotionFactory;
import com.gamezone.model.Sale;
import com.gamezone.persistence.PromotionRepository;
import com.gamezone.validation.PromotionValidator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Business rules of the promotion module. It loads the promotions when the
 * service is created, rejects invalid data and duplicated identifiers, and
 * saves the promotions after every change so the data survives between
 * executions.
 * <p>
 * File errors do not reach the caller as a checked exception. The repository
 * already turns them into a {@link PersistenceException}, because a failure of
 * the disk is a failure of the application and not a mistake of the user.
 * <p>
 * Rejected data becomes an {@link InvalidDataException}, or a
 * {@link BusinessRuleException} when a rule of the store is broken, with a
 * message in Spanish, because the console menu shows it directly to the user.
 */
public class PromotionService implements PromotionChooser {

    private PromotionRepository repository;
    private List<Promotion> promotions;

    /**
     * Creates the service and loads the promotions stored in the repository.
     *
     * @param repository repository used to read and write the promotions
     * @throws PersistenceException if the promotions file cannot be read
     */
    public PromotionService(PromotionRepository repository) {
        this.repository = repository;
        this.promotions = new ArrayList<>(repository.loadAll());
    }

    /**
     * Registers a promotion of any kind, once it has been built by
     * {@link PromotionFactory}.
     *
     * <p>This is the single entry point of the correction of violation V-06:
     * the service no longer needs one method per kind, because it no longer
     * decides which kind is being registered. The three methods that exist for
     * each kind stay until the menus are moved to this one.
     *
     * @param promotion promotion to register
     * @return the registered promotion
     * @throws InvalidDataException  if the data is invalid
     * @throws BusinessRuleException if the identifier is already registered
     * @throws PersistenceException  if the promotions file cannot be written
     */
    public Promotion register(Promotion promotion) {
        validateCommonData(promotion.getId(), promotion.getName(),
                promotion.getStartDate(), promotion.getEndDate());
        validateExtraData(promotion);
        addPromotion(promotion);
        return promotion;
    }

    /**
     * Validates the columns the kind of promotion adds, using only what the
     * promotion itself publishes: its {@code getTypeCode} says which kind it is
     * and its {@code getExtraFields} carries the values, so no test on the
     * concrete class is needed and a new kind only changes the factory.
     * <p>
     * The order of the checks is the one each of the three
     * {@code register...} methods had: a bulk purchase discount rejects its
     * minimum quantity before its percentage, and the other two reject the
     * percentage first. A registration that fails shows the same message it
     * showed before.
     *
     * @param promotion promotion to validate
     * @throws InvalidDataException  if one of the values of the kind is not valid
     * @throws BusinessRuleException if the percentage or the minimum quantity is
     *                              not allowed
     */
    private void validateExtraData(Promotion promotion) {
        List<String> extras = promotion.getExtraFields();
        String typeCode = promotion.getTypeCode();
        if (PromotionFactory.TYPE_BULK.equals(typeCode)) {
            validateMinimumQuantity(Integer.parseInt(valueAt(extras, 1)));
        }
        validatePercentage(Double.parseDouble(valueAt(extras, 0)));
        if (PromotionFactory.TYPE_CATEGORY.equals(typeCode)) {
            validateTargetCategory(valueAt(extras, 1));
        }
    }

    /**
     * Returns one of the published columns, or an empty string when the promotion
     * does not publish that position, so a value that was never set is validated
     * as empty instead of failing to be read.
     *
     * @param extras columns published by the promotion
     * @param index  position of the wanted column
     * @return the value, or an empty string
     */
    private String valueAt(List<String> extras, int index) {
        if (extras == null || index >= extras.size()) {
            return "";
        }
        return extras.get(index);
    }

    /**
     * Returns a copy of every promotion currently registered, in registration
     * order, including the ones that already expired and the ones that have not
     * started yet.
     *
     * @return promotions currently registered
     */
    public List<Promotion> listAllPromotions() {
        return new ArrayList<>(promotions);
    }

    /**
     * Returns the promotions that are valid today. A promotion is valid when
     * today is between its start date and its end date, both included.
     *
     * @return promotions valid today
     */
    public List<Promotion> listActivePromotions() {
        return listActiveOn(LocalDate.now());
    }

    /**
     * Searches a promotion by its identifier, ignoring letter case, so a user
     * can type "p-01" and still find the promotion registered as "P-01".
     *
     * @param id identifier to look for
     * @return the promotion found, or null when no promotion uses that
     *         identifier
     */
    public Promotion findById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        String searched = id.trim();
        for (Promotion promotion : promotions) {
            if (promotion.getId().equalsIgnoreCase(searched)) {
                return promotion;
            }
        }
        return null;
    }

    /**
     * Returns the promotions that are valid on a given date. It is the shared
     * implementation of {@link #listActivePromotions()} and of
     * {@link #findBestPromotionFor(Sale)}, which use different dates.
     *
     * @param date date to check the validity period against
     * @return promotions valid on that date
     */
    private List<Promotion> listActiveOn(LocalDate date) {
        List<Promotion> active = new ArrayList<>();
        for (Promotion promotion : promotions) {
            if (promotion.isActive(date)) {
                active.add(promotion);
            }
        }
        return active;
    }

    /**
     * Returns the promotion that gives the greatest discount to a sale.
 * <p>
 * Only the promotions that are valid on the date of the sale are considered,
 * because a promotion that was not valid on that day cannot be applied to it.
 * Promotions are not accumulative, so only one is returned. The method does not
 * change the sale: it only answers which promotion should be applied.
 *
 * @param sale sale the promotion is calculated for
 * @return the promotion with the greatest discount, or null when no active
 *         promotion gives a discount greater than zero
 */
    @Override
    public Promotion findBestPromotionFor(Sale sale) {
        if (sale == null || sale.getDate() == null) {
            return null;
        }
        Promotion best = null;
        double bestDiscount = 0;
        for (Promotion promotion : listActiveOn(sale.getDate())) {
            double discount = promotion.calculateDiscount(sale);
            if (discount > bestDiscount) {
                bestDiscount = discount;
                best = promotion;
            }
        }
        return best;
    }

    /**
     * Checks the data shared by every promotion before it is registered.
     *
     * @param id        promotion identifier
     * @param name      name of the promotion
     * @param startDate first day the promotion is valid
     * @param endDate   last day the promotion is valid
     * @throws InvalidDataException  if the identifier or the name is empty, if
     *                               a date is missing, or if the end date is
     *                               before the start date
     * @throws BusinessRuleException if the identifier is already registered
     */
    private void validateCommonData(String id, String name, LocalDate startDate, LocalDate endDate) {
        PromotionValidator.validatePromotionData(id, name, startDate, endDate);
        PromotionValidator.validateUniquePromotion(findById(id.trim()), id.trim());
    }

    /**
     * Checks that a discount percentage is usable in a calculation.
     *
     * @param percentage discount percentage to check
     * @throws InvalidDataException if the percentage is not greater than zero
     *                              or is greater than 100
     */
    private void validatePercentage(double percentage) {
        PromotionValidator.validatePercentage(percentage);
    }

    /**
     * Checks that a category discount points to a category that exists in the
     * catalogue. Video games, consoles and accessories are accepted, since
     * adjustment A1 added accessories as a target category.
     *
     * @param targetCategory category to check
     * @throws InvalidDataException if the category is empty or is not
     *                              {@code VIDEOGAME}, {@code CONSOLE} nor
     *                              {@code ACCESSORY}
     */
    private void validateTargetCategory(String targetCategory) {
        PromotionValidator.validateCategory(targetCategory);
    }

    /**
     * Checks that the minimum quantity of a bulk promotion makes the rule
     * reachable.
     *
     * @param minimumQuantity units the sale must include
     * @throws InvalidDataException if the quantity is not greater than zero
     */
    private void validateMinimumQuantity(int minimumQuantity) {
        PromotionValidator.validateMinimumQuantity(minimumQuantity);
    }

    /**
     * Adds a validated promotion and saves it. The saving happens after the
     * promotion is added, so the file always reflects the content of the
     * service.
     *
     * @param promotion promotion to add
     * @throws PersistenceException if the promotions file cannot be written
     */
    private void addPromotion(Promotion promotion) {
        promotions.add(promotion);
        saveCatalogue();
    }

    /**
     * Saves the promotions currently registered.
     *
     * @throws PersistenceException if the promotions file cannot be written
     */
    private void saveCatalogue() {
        repository.saveAll(promotions);
    }
}
