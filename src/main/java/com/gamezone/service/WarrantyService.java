package com.gamezone.service;

import com.gamezone.model.BasicWarranty;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Warranty;
import com.gamezone.persistence.WarrantyRecord;
import com.gamezone.persistence.WarrantyRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Business rules of the warranty module. It keeps the warranties of the store
 * and saves them after every change so the data survives between executions.
 * <p>
 * The service starts with an empty list on purpose. A warranty points to a sale
 * and to a product, and the sales need this service to create their warranties,
 * so this service cannot ask another service for those objects. Instead
 * {@link #loadWarranties(List)} receives the sales that were already loaded and
 * the records that the repository read, and it resolves them in that order:
 * repository, then sales, then warranties. {@code Main} calls it once the sales
 * exist. That ordering is what keeps the dependency graph free of a cycle.
 * <p>
 * The repository works with only identifiers, through {@code WarrantyRecord}, so
 * the persistence layer never builds a domain object and never references this
 * service. Building the warranties is the job of this class, which already knows
 * the sales and is the layer that owns the warranty rules.
 * <p>
 * File errors do not reach the caller as a checked exception. They become an
 * {@link IllegalStateException}, the same way the other services do it, because
 * a failure of the disk is a failure of the application and not a mistake of the
 * user.
 */
public class WarrantyService {

    /** Type marker of a basic warranty, as stored in the file. */
    private static final String TYPE_BASIC = "BASIC";

    /** Type marker of an extended warranty, as stored in the file. */
    private static final String TYPE_EXTENDED = "EXTENDED";

    /** Prefix of the generated warranty identifiers. */
    private static final String ID_PREFIX = "W";

    /** Number of digits of a warranty identifier after the prefix. */
    private static final int ID_DIGITS = 4;

    private WarrantyRepository repository;
    private List<Warranty> warranties;

    /**
     * Builds the service with an empty list of warranties. Nothing is loaded
     * here: the sales and the catalogue have to exist first.
     *
     * @param repository repository used to read and write the warranties
     */
    public WarrantyService(WarrantyRepository repository) {
        this.repository = repository;
        this.warranties = new ArrayList<>();
    }

    /**
     * Loads the records stored in the file and turns each one into a warranty.
     * <p>
     * The repository returns plain records with only identifiers, because the
     * persistence layer must not build domain objects. This method is the place
     * where those records become real warranties: it looks the sale up by id
     * among the sales that were already registered, and then looks the product up
     * inside that sale. The sale alone is enough to find the product, because a
     * product can only be covered by a warranty when it belongs to the sale.
     * <p>
     * A record whose sale or whose product no longer exists, or whose type is
     * unknown, is ignored, so a warranty of a deleted product does not stop the
     * application from starting.
     *
     * @param sales sales already registered, used to resolve the sale and the
     *              product of each record
     * @throws IllegalStateException if the warranties file cannot be read
     */
    public void loadWarranties(List<Sale> sales) {
        try {
            List<WarrantyRecord> records = repository.loadAll();
            List<Warranty> loaded = new ArrayList<>();
            for (WarrantyRecord record : records) {
                Warranty warranty = toWarranty(record, sales);
                if (warranty != null) {
                    loaded.add(warranty);
                }
            }
            warranties.clear();
            warranties.addAll(loaded);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar el catalogo de garantias", e);
        }
    }

    /**
     * Turns one record into the warranty it represents.
     *
     * @param record record read from the file
     * @param sales  sales already registered
     * @return the warranty, or null when the record cannot be used because its
     *         type is unknown or its sale or its product no longer exists
     */
    private Warranty toWarranty(WarrantyRecord record, List<Sale> sales) {
        if (record.getStartDate() == null) {
            return null;
        }
        Sale sale = findSale(sales, record.getSaleId());
        if (sale == null) {
            return null;
        }
        Product product = findProduct(sale, record.getProductId());
        if (product == null) {
            return null;
        }
        if (TYPE_BASIC.equalsIgnoreCase(record.getType())) {
            return new BasicWarranty(record.getId(), product, sale, record.getStartDate());
        }
        if (TYPE_EXTENDED.equalsIgnoreCase(record.getType())) {
            return new ExtendedWarranty(record.getId(), product, sale, record.getStartDate());
        }
        return null;
    }

    /**
     * Looks for the sale a record comes from.
     *
     * @param sales  sales already registered
     * @param saleId identifier stored in the record
     * @return the sale found, or null when it no longer exists
     */
    private Sale findSale(List<Sale> sales, String saleId) {
        if (saleId == null || saleId.isBlank()) {
            return null;
        }
        String searched = saleId.trim();
        for (Sale sale : sales) {
            if (sale.getId().equalsIgnoreCase(searched)) {
                return sale;
            }
        }
        return null;
    }

    /**
     * Looks for the product a record covers inside the units of its sale.
     *
     * @param sale      sale the warranty comes from
     * @param productId identifier stored in the record
     * @return the product found, or null when the sale does not contain it
     */
    private Product findProduct(Sale sale, String productId) {
        if (productId == null || productId.isBlank()) {
            return null;
        }
        String searched = productId.trim();
        for (Product product : sale.getProducts()) {
            if (product.getId().equalsIgnoreCase(searched)) {
                return product;
            }
        }
        return null;
    }

    /**
     * Assigns a basic warranty to a product that was sold. The basic warranty
     * covers factory defects for six months and has no additional cost.
     *
     * @param product   product covered by the warranty
     * @param sale      sale the product was bought in
     * @param startDate first day the warranty is valid
     * @return the assigned warranty
     * @throws IllegalArgumentException if the product, the sale or the date is
     *                                  missing
     * @throws IllegalStateException    if the warranties file cannot be written
     */
    public BasicWarranty assignBasicWarranty(Product product, Sale sale, LocalDate startDate) {
        validateAssignment(product, sale, startDate);
        BasicWarranty warranty = new BasicWarranty(nextWarrantyId(), product, sale, startDate);
        addWarranty(warranty);
        return warranty;
    }

    /**
     * Assigns an extended warranty to a product that was sold. The extended
     * warranty covers factory defects and accidental damage for twelve months,
     * and its cost is a percentage of the price of the product.
     *
     * @param product   product covered by the warranty
     * @param sale      sale the product was bought in
     * @param startDate first day the warranty is valid
     * @return the assigned warranty
     * @throws IllegalArgumentException if the product, the sale or the date is
     *                                  missing
     * @throws IllegalStateException    if the warranties file cannot be written
     */
    public ExtendedWarranty assignExtendedWarranty(Product product, Sale sale, LocalDate startDate) {
        validateAssignment(product, sale, startDate);
        ExtendedWarranty warranty =
                new ExtendedWarranty(nextWarrantyId(), product, sale, startDate);
        addWarranty(warranty);
        return warranty;
    }

    /**
 * Searches the warranty of a product in a specific sale, ignoring letter case
 * on both identifiers.
 * <p>
 * A product sold in a sale can carry two warranties, a basic one and an extended
 * one. When both exist, the one that lasts longer is returned, because it is
 * the one that covers the customer for more time.
 *
 * @param productId identifier of the product covered
 * @param saleId    identifier of the sale the product was bought in
 * @return the warranty found, or null when the product has no warranty in that
 *         sale
 */
    public Warranty findWarrantyByProduct(String productId, String saleId) {
        if (productId == null || productId.isBlank() || saleId == null || saleId.isBlank()) {
            return null;
        }
        String searchedProduct = productId.trim();
        String searchedSale = saleId.trim();
        Warranty found = null;
        for (Warranty warranty : warranties) {
            if (!covers(warranty, searchedProduct, searchedSale)) {
                continue;
            }
            if (found == null || lastsLongerThan(warranty, found)) {
                found = warranty;
            }
        }
        return found;
    }

    /**
     * Tells whether a warranty covers the given product in the given sale,
     * ignoring letter case on both identifiers.
     *
     * @param warranty      warranty to inspect
     * @param productId     identifier of the product covered
     * @param saleId        identifier of the sale
     * @return true when the warranty covers that product in that sale
     */
    private boolean covers(Warranty warranty, String productId, String saleId) {
        Product product = warranty.getProduct();
        Sale sale = warranty.getSale();
        return product != null && sale != null
                && product.getId().equalsIgnoreCase(productId)
                && sale.getId().equalsIgnoreCase(saleId);
    }

    /**
     * Tells whether a warranty lasts longer than another one.
     *
     * @param warranty warranty to compare
     * @param other    warranty to compare against
     * @return true when the first warranty ends after the second one
     */
    private boolean lastsLongerThan(Warranty warranty, Warranty other) {
        LocalDate endDate = warranty.getEndDate();
        LocalDate otherEndDate = other.getEndDate();
        if (endDate == null) {
            return false;
        }
        return otherEndDate == null || endDate.isAfter(otherEndDate);
    }

    /**
 * Returns a copy of every warranty currently registered, in assignment order,
 * including the ones that already expired.
 *
 * @return warranties currently registered
 */
    public List<Warranty> listAllWarranties() {
        return new ArrayList<>(warranties);
    }

    /**
     * Cancels the warranties of one unit of a product in one sale, and returns
     * how much has to be given back to the customer for them.
     * <p>
     * Cancelling removes **one unit**: the basic warranty of that product in that
     * sale and, when it exists, the extended one. Two units of the same console in
     * the same sale have their own pair of warranties, so returning two consoles
     * calls this method twice.
     * <p>
     * The money to give back is the cost of the cancelled warranties. The basic
     * warranty has no cost and contributes zero; the extended one costs a
     * percentage of the price of the product, and that is what the customer gets
     * back. A product without warranties in that sale cancels nothing and returns
     * zero.
     * <p>
     * The warranties are cancelled because the covered product is going back to
     * the store: a console that is no longer with the customer cannot be covered
     * any more, so it must not keep showing as active.
     *
     * @param productId identifier of the product whose warranties are cancelled
     * @param saleId    identifier of the sale the warranties come from
     * @return amount to give back for the cancelled warranties, zero when there
     *         was none
     * @throws IllegalStateException if the warranties file cannot be written
     */
    public double cancelWarranties(String productId, String saleId) {
        if (productId == null || productId.isBlank()
                || saleId == null || saleId.isBlank()) {
            return 0;
        }
        double refund = removeOneWarranty(productId, saleId, BasicWarranty.class)
                + removeOneWarranty(productId, saleId, ExtendedWarranty.class);
        if (refund != 0) {
            saveCatalogue();
        }
        return refund;
    }

    /**
     * Removes the first warranty of a given type that covers a product in a sale,
     * and returns its cost.
     * <p>
     * The type is checked against the concrete class and not against
     * {@code getWarrantyType()}, because that method returns the name shown to the
     * user, {@code "Garantía Básica"}, and not the marker of the file.
     *
     * @param productId identifier of the covered product
     * @param saleId    identifier of the sale
     * @param type      concrete class of the warranty to remove
     * @return cost of the removed warranty, zero when there was none
     */
    private double removeOneWarranty(String productId, String saleId,
                                     Class<? extends Warranty> type) {
        for (int index = 0; index < warranties.size(); index++) {
            Warranty warranty = warranties.get(index);
            if (!type.isInstance(warranty) || !covers(warranty, productId, saleId)) {
                continue;
            }
            warranties.remove(index);
            return warranty.getAdditionalCost();
        }
        return 0;
    }

    /**
     * Returns the warranties that are valid today. A warranty is valid when
     * today is between its start date and its end date, both included.
     *
     * @return warranties valid today
 */
    public List<Warranty> listActiveWarranties() {
        return listActiveOn(LocalDate.now());
    }

    /**
     * Returns the warranties that are valid today and end within the next
     * days, so the store can call the customers before their coverage runs out.
     * <p>
     * Both ends of the window are included: a warranty that ends exactly today
     * is still listed, and so is one that ends exactly {@code daysAhead} days
     * from today. Warranties that already expired are never listed, because
     * they do not belong to {@link #listActiveWarranties()}.
     *
     * @param daysAhead how many days ahead are considered
     * @return warranties that are valid today and end within that window
     * @throws IllegalArgumentException if the number of days is not greater than
     *                                  zero
     */
    public List<Warranty> listWarrantiesExpiringSoon(int daysAhead) {
        if (daysAhead <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad de días debe ser mayor que cero");
        }
        LocalDate limit = LocalDate.now().plusDays(daysAhead);
        List<Warranty> expiring = new ArrayList<>();
        for (Warranty warranty : listActiveWarranties()) {
            LocalDate endDate = warranty.getEndDate();
            if (endDate != null && !endDate.isAfter(limit)) {
                expiring.add(warranty);
            }
        }
        return expiring;
    }

    /**
     * Returns the warranties that are valid on a given date. It is the shared
     * implementation of the active queries, which all use today's date.
     *
     * @param date date to check the validity period against
     * @return warranties valid on that date
 */
    private List<Warranty> listActiveOn(LocalDate date) {
        List<Warranty> active = new ArrayList<>();
        for (Warranty warranty : warranties) {
            if (warranty.isActive(date)) {
                active.add(warranty);
            }
        }
        return active;
    }

    /**
     * Checks that a warranty can be assigned to the given product and sale. Only
     * the values needed to build a warranty are checked, because deciding
     * whether a product is eligible for a warranty is a rule of the sale, not
     * of this service.
     *
     * @param product   product covered by the warranty
     * @param sale      sale the product was bought in
     * @param startDate first day the warranty is valid
     * @throws IllegalArgumentException if the product, the sale or the date is
     *                                  missing
     */
    private void validateAssignment(Product product, Sale sale, LocalDate startDate) {
        if (product == null) {
            throw new IllegalArgumentException("El producto de la garantía es obligatorio");
        }
        if (sale == null) {
            throw new IllegalArgumentException("La venta de la garantía es obligatoria");
        }
        if (startDate == null) {
            throw new IllegalArgumentException(
                    "La fecha de inicio de la garantía es obligatoria");
        }
    }

    /**
     * Builds the identifier of the next warranty, following the documented
     * pattern {@code W0001}, {@code W0002} and so on, so the first warranty
     * assigned is {@code W0001} and the third is {@code W0003}.
     * <p>
     * The number is taken from the highest identifier already in use instead of
     * from the size of the list, so an identifier is never repeated even if the
     * list were to lose a warranty later.
     *
     * @return identifier for the next warranty
     */
    private String nextWarrantyId() {
        int highest = 0;
        for (Warranty warranty : warranties) {
            int number = parseIdNumber(warranty.getId());
            if (number > highest) {
                highest = number;
            }
        }
        return String.format(Locale.ROOT, "%s%0" + ID_DIGITS + "d", ID_PREFIX, highest + 1);
    }

    /**
     * Reads the number of a warranty identifier such as {@code W0007}. An
     * identifier with another shape counts as zero and never breaks the
     * sequence.
     *
     * @param id identifier to read
     * @return the number of the identifier, or zero when it has no number
     */
    private int parseIdNumber(String id) {
        if (id == null || !id.startsWith(ID_PREFIX) || id.length() <= ID_PREFIX.length()) {
            return 0;
        }
        try {
            return Integer.parseInt(id.substring(ID_PREFIX.length()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Adds an assigned warranty and saves the file, so the content of the file
     * always reflects the content of the service.
     *
     * @param warranty warranty to add
     * @throws IllegalStateException if the warranties file cannot be written
     */
    private void addWarranty(Warranty warranty) {
        warranties.add(warranty);
        saveCatalogue();
    }

    /**
     * Saves the warranties currently registered.
     *
     * @throws IllegalStateException if the warranties file cannot be written
     */
    private void saveCatalogue() {
        try {
            repository.saveAll(warranties);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar el catalogo de garantias", e);
        }
    }
}