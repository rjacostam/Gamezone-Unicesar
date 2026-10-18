package com.gamezone.service;

import com.gamezone.model.BulkPurchaseDiscount;
import com.gamezone.model.CategoryDiscount;
import com.gamezone.model.PercentageDiscount;
import com.gamezone.model.Promotion;
import com.gamezone.persistence.PromotionRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Business rules of the promotion module. It loads the promotions when the
 * service is created, rejects invalid data and duplicated identifiers, and
 * saves the promotions after every change so the data survives between
 * executions.
 * <p>
 * File errors do not reach the caller as a checked exception. They become an
 * {@link IllegalStateException}, the same way {@code ProductService} and
 * {@code AccessoryService} do it, because a failure of the disk is a failure of
 * the application and not a mistake of the user.
 * <p>
 * Rejected data becomes an {@link IllegalArgumentException} with a message in
 * Spanish, because the console menu shows it directly to the user.
 */
public class PromotionService {

    /** Category accepted by a category discount for video games. */
    private static final String CATEGORY_VIDEOGAME = "VIDEOGAME";

    /** Category accepted by a category discount for consoles. */
    private static final String CATEGORY_CONSOLE = "CONSOLE";

    /** Highest percentage accepted by any promotion. */
    private static final double MAX_PERCENTAGE = 100;

    private PromotionRepository repository;
    private List<Promotion> promotions;

    /**
     * Creates the service and loads the promotions stored in the repository.
     *
     * @param repository repository used to read and write the promotions
     * @throws IllegalStateException if the promotions file cannot be read
     */
    public PromotionService(PromotionRepository repository) {
        this.repository = repository;
        try {
            this.promotions = new ArrayList<>(repository.loadAll());
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar el catalogo de promociones", e);
        }
    }

    /**
     * Registers a new promotion that applies a percentage to the whole total.
     *
     * @param id        promotion identifier
     * @param name      name of the promotion
     * @param startDate first day the promotion is valid
     * @param endDate   last day the promotion is valid
     * @param percentage discount percentage, greater than zero and at most 100
     * @return the registered promotion
     * @throws IllegalArgumentException if the data is invalid or the identifier
     *                                  is already registered
     * @throws IllegalStateException    if the promotions file cannot be written
     */
    public PercentageDiscount registerPercentageDiscount(String id, String name,
                                                          LocalDate startDate, LocalDate endDate,
                                                          double percentage) {
        validateCommonData(id, name, startDate, endDate);
        validatePercentage(percentage);
        PercentageDiscount promotion = new PercentageDiscount(id.trim(), name.trim(),
                startDate, endDate, percentage);
        addPromotion(promotion);
        return promotion;
    }

    /**
     * Registers a new promotion that applies a percentage only to the products
     * of one category.
     *
     * @param id             promotion identifier
     * @param name           name of the promotion
     * @param startDate      first day the promotion is valid
     * @param endDate        last day the promotion is valid
     * @param percentage     discount percentage, greater than zero and at most 100
     * @param targetCategory category the discount applies to, {@code VIDEOGAME}
     *                       or {@code CONSOLE}
     * @return the registered promotion
     * @throws IllegalArgumentException if the data is invalid, if the category
     *                                  is not accepted, or if the identifier is
     *                                  already registered
     * @throws IllegalStateException    if the promotions file cannot be written
     */
    public CategoryDiscount registerCategoryDiscount(String id, String name,
                                                     LocalDate startDate, LocalDate endDate,
                                                     double percentage, String targetCategory) {
        validateCommonData(id, name, startDate, endDate);
        validatePercentage(percentage);
        validateTargetCategory(targetCategory);
        CategoryDiscount promotion = new CategoryDiscount(id.trim(), name.trim(),
                startDate, endDate, percentage, targetCategory.trim());
        addPromotion(promotion);
        return promotion;
    }

    /**
     * Registers a new promotion that applies a percentage when the sale includes
     * a minimum number of units.
     *
     * @param id              promotion identifier
     * @param name            name of the promotion
     * @param startDate       first day the promotion is valid
     * @param endDate         last day the promotion is valid
     * @param minimumQuantity units the sale must include, greater than zero
     * @param percentage      discount percentage, greater than zero and at most 100
     * @return the registered promotion
     * @throws IllegalArgumentException if the data is invalid or the identifier
     *                                  is already registered
     * @throws IllegalStateException    if the promotions file cannot be written
     */
    public BulkPurchaseDiscount registerBulkPurchaseDiscount(String id, String name,
                                                             LocalDate startDate, LocalDate endDate,
                                                             int minimumQuantity, double percentage) {
        validateCommonData(id, name, startDate, endDate);
        validateMinimumQuantity(minimumQuantity);
        validatePercentage(percentage);
        BulkPurchaseDiscount promotion = new BulkPurchaseDiscount(id.trim(), name.trim(),
                startDate, endDate, minimumQuantity, percentage);
        addPromotion(promotion);
        return promotion;
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
     * Checks the data shared by every promotion before it is registered.
     *
     * @param id        promotion identifier
     * @param name      name of the promotion
     * @param startDate first day the promotion is valid
     * @param endDate   last day the promotion is valid
     * @throws IllegalArgumentException if the identifier or the name is empty,
     *                                  if the identifier is already registered,
     *                                  if a date is missing, or if the end date
     *                                  is before the start date
     */
    private void validateCommonData(String id, String name, LocalDate startDate, LocalDate endDate) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El identificador de la promoción es obligatorio");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre de la promoción es obligatorio");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                    "Las fechas de inicio y fin de la promoción son obligatorias");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        if (isIdentifierInUse(id.trim())) {
            throw new IllegalArgumentException(
                    "Ya existe una promoción con el identificador " + id.trim());
        }
    }

    /**
     * Checks that a discount percentage is usable in a calculation.
     *
     * @param percentage discount percentage to check
     * @throws IllegalArgumentException if the percentage is not greater than
     *                                  zero or is greater than 100
     */
    private void validatePercentage(double percentage) {
        if (percentage <= 0 || percentage > MAX_PERCENTAGE) {
            throw new IllegalArgumentException(
                    "El porcentaje debe ser mayor que cero y menor o igual a 100");
        }
    }

    /**
     * Checks that a category discount points to a category that exists in the
     * catalogue. Accessories cannot be the target of a category discount in this
     * requirement, only video games and consoles.
     *
     * @param targetCategory category to check
     * @throws IllegalArgumentException if the category is empty or is not
     *                                  {@code VIDEOGAME} nor {@code CONSOLE}
     */
    private void validateTargetCategory(String targetCategory) {
        if (targetCategory == null || targetCategory.isBlank()) {
            throw new IllegalArgumentException("La categoría de la promoción es obligatoria");
        }
        if (!CATEGORY_VIDEOGAME.equalsIgnoreCase(targetCategory.trim())
                && !CATEGORY_CONSOLE.equalsIgnoreCase(targetCategory.trim())) {
            throw new IllegalArgumentException(
                    "La categoría debe ser VIDEOGAME o CONSOLE");
        }
    }

    /**
     * Checks that the minimum quantity of a bulk promotion makes the rule
     * reachable.
     *
     * @param minimumQuantity units the sale must include
     * @throws IllegalArgumentException if the quantity is not greater than zero
     */
    private void validateMinimumQuantity(int minimumQuantity) {
        if (minimumQuantity <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad mínima debe ser mayor que cero");
        }
    }

    /**
     * Tells whether an identifier is already used by a registered promotion,
     * ignoring letter case.
     *
     * @param id identifier to look for
     * @return true when the identifier is already in use
     */
    private boolean isIdentifierInUse(String id) {
        for (Promotion promotion : promotions) {
            if (promotion.getId().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a validated promotion and saves it. The saving happens after the
     * promotion is added, so the file always reflects the content of the
     * service.
     *
     * @param promotion promotion to add
     * @throws IllegalStateException if the promotions file cannot be written
     */
    private void addPromotion(Promotion promotion) {
        promotions.add(promotion);
        saveCatalogue();
    }

    /**
     * Saves the promotions currently registered.
     *
     * @throws IllegalStateException if the promotions file cannot be written
     */
    private void saveCatalogue() {
        try {
            repository.saveAll(promotions);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar el catalogo de promociones", e);
        }
    }
}