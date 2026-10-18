package com.gamezone.service;

import com.gamezone.model.Accessory;
import com.gamezone.model.Customer;
import com.gamezone.model.Product;
import com.gamezone.model.Return;
import com.gamezone.model.Sale;
import com.gamezone.persistence.ReturnRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Business rules of the return module. It loads the returns when the service is
 * created, validates every registration before it changes anything, restores the
 * stock of the returned units and saves the returns after every change so the
 * data survives between executions.
 * <p>
 * The service receives {@code SaleService}, {@code ProductService} and
 * {@code AccessoryService} and uses them instead of reaching the repositories
 * itself. That is what the statement asks for and it also keeps a single owner
 * for every rule: the sales know whether a sale can still be returned, the
 * catalogue owns the stock of the products, and the accessory service owns the
 * stock of the accessories.
 * <p>
 * File errors do not reach the caller as a checked exception. They become an
 * {@link IllegalStateException}, the same way the other services do it, because
 * a failure of the disk is a failure of the application and not a mistake of the
 * user.
 * <p>
 * Rejected data becomes an {@link IllegalArgumentException} with a message in
 * Spanish, because the console menu shows it directly to the user.
 */
public class ReturnService {

    /** Prefix of the generated return identifiers. */
    private static final String ID_PREFIX = "R";

    /** Number of digits of a return identifier after the prefix. */
    private static final int ID_DIGITS = 4;

    private ReturnRepository repository;
    private SaleService saleService;
    private ProductService productService;
    private AccessoryService accessoryService;
    private List<Return> returns;

    /**
     * Builds the service and loads the returns, linking each one to the sales
     * that were already registered.
     *
     * @param repository     repository used to read and write the returns
     * @param saleService    service of the sales, needed because a return always
     *                       comes from a sale
     * @param productService service of the catalogue, needed to restore the stock
     *                       of the returned video games and consoles
     * @param accessoryService service of the accessories, needed to restore the
     *                         stock of the returned accessories
     * @throws IllegalStateException if the returns file cannot be read
     */
    public ReturnService(ReturnRepository repository, SaleService saleService,
                         ProductService productService, AccessoryService accessoryService) {
        this.repository = repository;
        this.saleService = saleService;
        this.productService = productService;
        this.accessoryService = accessoryService;
        try {
            this.returns = new ArrayList<>(repository.loadAll(saleService.listAllSales()));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar el historial de devoluciones", e);
        }
    }

    /**
 * Registers a return of one or more units of a sale already registered.
 * <p>
 * Every check is performed before anything is changed: the sale must exist and
 * still be inside the thirty day window, the request must name at least one
 * product and a reason, and every unit requested must still be available in that
 * sale after subtracting the units returned in earlier returns of the same sale.
 * Only when all of that passes, the return is created, the stock is restored and
 * the file is written, so a rejected return never leaves the inventory modified.
 * <p>
 * The stock is restored for every kind of product: video games and consoles
 * through {@code ProductService} and accessories through
 * {@code AccessoryService}.
 *
 * @param saleId    identifier of the sale the products are returned from
 * @param productIds identifiers of the units returned, one entry per unit, so
 *                   returning two units of the same product means repeating its
 *                   identifier
 * @param reason    reason of the return
 * @return the registered return
 * @throws IllegalArgumentException if the sale does not exist, if it is outside
 *                                  the return window, if the request is empty,
 *                                  if a product does not belong to the sale, or
 *                                  if more units are returned than are still
 *                                  available
 * @throws IllegalStateException    if the returns file cannot be written
 */
    public Return registerReturn(String saleId, List<String> productIds, String reason) {
        Sale sale = findSale(saleId);
        if (!sale.canBeReturned()) {
            throw new IllegalArgumentException(
                    "La venta " + sale.getId() + " supera los 30 días y ya no se puede devolver");
        }
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "La devolución debe tener al menos un producto");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("El motivo de la devolución es obligatorio");
        }

        List<Product> returnedProducts = resolveReturnedProducts(sale, productIds);

        Return registeredReturn =
                new Return(nextReturnId(), LocalDate.now(), sale, returnedProducts, reason.trim());
        restoreStock(returnedProducts);
        returns.add(registeredReturn);
        saveCatalogue();
        return registeredReturn;
    }

    /**
     * Looks for the sale a return comes from.
     *
     * @param saleId identifier of the sale
     * @return the sale found
     * @throws IllegalArgumentException if the identifier is empty or no sale uses
     *                                  that identifier
     */
    private Sale findSale(String saleId) {
        if (saleId == null || saleId.isBlank()) {
            throw new IllegalArgumentException("La venta de la devolución es obligatoria");
        }
        Sale sale = saleService.findById(saleId.trim());
        if (sale == null) {
            throw new IllegalArgumentException(
                    "No existe una venta con el identificador " + saleId.trim());
        }
        return sale;
    }

    /**
     * Turns the requested identifiers into the products of the sale, one entry
     * per unit, and checks that those units are still available.
     * <p>
     * For every identifier, the units still available are the units sold minus the
     * units already returned in earlier returns of the same sale. A return can
     * therefore be split into several ones, but never adds up to more units than
     * the customer bought.
     *
     * @param sale       sale the products are returned from
     * @param productIds identifiers of the units returned
     * @return the products to return, in the order they were requested
     * @throws IllegalArgumentException if an identifier is not part of the sale
     *                                  or if more units are requested than are
     *                                  still available
     */
    private List<Product> resolveReturnedProducts(Sale sale, List<String> productIds) {
        List<Product> returnedProducts = new ArrayList<>();
        for (String requestedId : productIds) {
            if (requestedId == null || requestedId.isBlank()) {
                throw new IllegalArgumentException(
                        "El producto de la devolución es obligatorio");
            }
            String wanted = requestedId.trim();
            if (countUnits(sale, wanted) == 0) {
                throw new IllegalArgumentException(
                        "El producto " + wanted + " no pertenece a la venta " + sale.getId());
            }
            returnedProducts.add(findProduct(sale, wanted));
        }
        validateAvailableUnits(sale, productIds);
        return returnedProducts;
    }

    /**
     * Checks that the request does not return more units of any product than the
     * ones that are still available in the sale.
     *
     * @param sale       sale the products are returned from
     * @param productIds identifiers of the units returned
     * @throws IllegalArgumentException if more units are requested than are still
     *                                  available
     */
    private void validateAvailableUnits(Sale sale, List<String> productIds) {
        Map<String, Integer> requested = countById(productIds);
        for (Map.Entry<String, Integer> entry : requested.entrySet()) {
            String productId = entry.getKey();
            int available = countUnits(sale, productId) - countReturnedUnits(sale, productId);
            if (entry.getValue() > available) {
                throw new IllegalArgumentException(
                        "No se pueden devolver " + entry.getValue() + " unidades de " + productId
                                + ", solo quedan " + available + " disponibles");
            }
        }
    }

    /**
     * Counts how many units of a product were sold in a sale.
     *
     * @param sale      sale to inspect
     * @param productId identifier of the product
     * @return number of units of that product in the sale
     */
    private int countUnits(Sale sale, String productId) {
        int total = 0;
        for (Product product : sale.getProducts()) {
            if (product.getId().equalsIgnoreCase(productId)) {
                total++;
            }
        }
        return total;
    }

    /**
     * Counts how many units of a product were already returned in the earlier
     * returns of a sale.
     *
     * @param sale      sale the returns come from
     * @param productId identifier of the product
     * @return number of units of that product already returned
     */
    private int countReturnedUnits(Sale sale, String productId) {
        int total = 0;
        for (Return registeredReturn : returns) {
            if (!isReturnOf(sale, registeredReturn)) {
                continue;
            }
            for (Product product : registeredReturn.getReturnedProducts()) {
                if (product.getId().equalsIgnoreCase(productId)) {
                    total++;
                }
            }
        }
        return total;
    }

    /**
     * Groups a list of identifiers into a map with the number of times each one
     * appears, which is how a partial return is expressed.
     *
     * @param productIds identifiers to group
     * @return number of requested units per identifier
     */
    private Map<String, Integer> countById(List<String> productIds) {
        Map<String, Integer> requested = new LinkedHashMap<>();
        for (String productId : productIds) {
            String wanted = productId.trim();
            requested.merge(wanted, 1, Integer::sum);
        }
        return requested;
    }

    /**
     * Restores the stock of the returned units, delegating each unit to the
     * service that owns it.
     * <p>
     * An accessory is restored through {@code AccessoryService} and everything
     * else, video games and consoles, through {@code ProductService}. Each
     * identifier is restored in a single call with the number of units returned,
     * instead of one call per unit.
     * <p>
     * Before the A4 adjustment the accessories were simply skipped, so the stock
     * of a returned accessory was never given back. Delegating by type is what
     * fixes it, and it keeps a single owner for each inventory: the catalogue
     * owns the products and the accessory service owns the accessories.
     *
     * @param returnedProducts units returned
     */
    private void restoreStock(List<Product> returnedProducts) {
        Map<String, Integer> productUnits = new LinkedHashMap<>();
        Map<String, Integer> accessoryUnits = new LinkedHashMap<>();
        for (Product product : returnedProducts) {
            Map<String, Integer> target =
                    product instanceof Accessory ? accessoryUnits : productUnits;
            target.merge(product.getId(), 1, Integer::sum);
        }
        for (Map.Entry<String, Integer> entry : productUnits.entrySet()) {
            productService.restoreStock(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Integer> entry : accessoryUnits.entrySet()) {
            accessoryService.restoreStock(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Tells whether a return comes from a given sale.
     *
     * @param sale             sale to compare
     * @param registeredReturn return to compare
     * @return true when the return belongs to that sale
     */
    private boolean isReturnOf(Sale sale, Return registeredReturn) {
        Sale returnSale = registeredReturn.getSale();
        return returnSale != null && returnSale.getId().equalsIgnoreCase(sale.getId());
    }

    /**
     * Looks for a product inside the units of a sale.
     *
     * @param sale      sale to inspect
     * @param productId identifier of the product
     * @return the product found, or null when the sale does not contain it
     */
    private Product findProduct(Sale sale, String productId) {
        for (Product product : sale.getProducts()) {
            if (product.getId().equalsIgnoreCase(productId)) {
                return product;
            }
        }
        return null;
    }

    /**
     * Builds the identifier of the next return, following the documented
     * pattern {@code R0001}, {@code R0002} and so on.
     * <p>
     * The number is taken from the highest identifier already in use instead of
     * from the size of the list, so an identifier is never repeated.
     *
     * @return identifier for the next return
     */
    private String nextReturnId() {
        int highest = 0;
        for (Return registeredReturn : returns) {
            int number = parseIdNumber(registeredReturn.getId());
            if (number > highest) {
                highest = number;
            }
        }
        return String.format(Locale.ROOT, "%s%0" + ID_DIGITS + "d", ID_PREFIX, highest + 1);
    }

    /**
     * Reads the number of a return identifier such as {@code R0007}. An
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
 * Returns the returns of the sales of a given customer, ignoring letter case
 * on the identifier. The filter looks at the customer of the original sale,
 * because a return never stores a customer of its own.
 *
 * @param customerId identifier of the customer
 * @return returns of that customer, empty when there is none
 */
    public List<Return> viewReturnsByCustomer(String customerId) {
        List<Return> found = new ArrayList<>();
        if (customerId == null || customerId.isBlank()) {
            return found;
        }
        String searched = customerId.trim();
        for (Return registeredReturn : returns) {
            Sale sale = registeredReturn.getSale();
            Customer customer = sale == null ? null : sale.getCustomer();
            if (customer != null && customer.getId().equalsIgnoreCase(searched)) {
                found.add(registeredReturn);
            }
        }
        return found;
    }

    /**
     * Returns the returns that come from a given sale, ignoring letter case.
 *
 * @param saleId identifier of the sale
 * @return returns of that sale, empty when there is none
 */
    public List<Return> viewReturnsBySale(String saleId) {
        List<Return> found = new ArrayList<>();
        if (saleId == null || saleId.isBlank()) {
            return found;
        }
        String searched = saleId.trim();
        for (Return registeredReturn : returns) {
            if (isReturnOfSaleId(registeredReturn, searched)) {
                found.add(registeredReturn);
            }
        }
        return found;
    }

    /**
     * Tells whether a return comes from the sale with the given identifier,
     * ignoring letter case.
     *
     * @param registeredReturn return to compare
     * @param saleId           identifier of the sale
     * @return true when the return belongs to that sale
     */
    private boolean isReturnOfSaleId(Return registeredReturn, String saleId) {
        Sale sale = registeredReturn.getSale();
        return sale != null && sale.getId().equalsIgnoreCase(saleId);
    }

    /**
     * Sums the total of every sale of a month, that is the final amount of the
     * sale with its discount and its warranties already applied.
     * <p>
     * Each sale is grouped by its own date, so a sale made in September and
     * returned in October counts as income in September only.
     *
     * @param month number of the month, from 1 to 12
     * @param year  number of the year
     * @return total of the sales of that month
     * @throws IllegalArgumentException if the month is not between 1 and 12
     */
    public double calculateMonthlySales(int month, int year) {
        validateMonth(month);
        double total = 0;
        for (Sale sale : saleService.listAllSales()) {
            if (isInMonth(sale.getDate(), month, year)) {
                total += sale.getTotal();
            }
        }
        return total;
    }

    /**
     * Sums the refunded amount of every return of a month, that is the money the
     * store gave back.
     * <p>
     * Each return is grouped by its own date, so the refund of a sale made in a
     * previous month counts in the month the customer handed the product back.
     *
     * @param month number of the month, from 1 to 12
     * @param year  number of the year
     * @return total of the refunds of that month
     * @throws IllegalArgumentException if the month is not between 1 and 12
     */
    public double calculateMonthlyReturns(int month, int year) {
        validateMonth(month);
        double total = 0;
        for (Return registeredReturn : returns) {
            if (isInMonth(registeredReturn.getDate(), month, year)) {
                total += registeredReturn.getRefundAmount();
            }
        }
        return total;
    }

    /**
     * Checks that a month number is one of the twelve months of a year. The rule
     * and its message are written only once and shared by the three reports of the
     * month.
     *
     * @param month number of the month
     * @throws IllegalArgumentException if the month is not between 1 and 12
     */
    private void validateMonth(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("El mes debe estar entre 1 y 12");
        }
    }

    /**
 * Builds the balance of a month: the money the store took in that month minus
     * the money it gave back in that month.
     * <p>
     * The result is {@link #calculateMonthlySales(int, int)} minus
     * {@link #calculateMonthlyReturns(int, int)}, so the net figure and the two
     * figures the report shows next to it can never disagree: all three come from
     * the same two methods.
     * <p>
     * The method reads data of two modules, the sales and the returns, which is
     * why it lives in this service: it is the place that already knows both. It
     * delegates the lookup of the sales to {@code SaleService} and never reads a
     * repository of another module.
     *
     * @param month number of the month, from 1 to 12
     * @param year  number of the year
     * @return the income of the month minus the refunds of the month
     * @throws IllegalArgumentException if the month is not between 1 and 12
     */
    public double generateMonthlyBalance(int month, int year) {
        return calculateMonthlySales(month, year) - calculateMonthlyReturns(month, year);
    }

    /**
     * Tells whether a date falls on a given month and year.
     *
     * @param date  date to check
     * @param month number of the month
     * @param year  number of the year
     * @return true when the date is on that month of that year
     */
    private boolean isInMonth(LocalDate date, int month, int year) {
        return date != null && date.getMonthValue() == month && date.getYear() == year;
    }

    /**
     * Returns a copy of every return currently registered, in registration
     * order.
     *
     * @return returns currently registered
     */
    public List<Return> viewAllReturns() {
        return new ArrayList<>(returns);
    }

    /**
     * Saves the returns currently registered.
     *
     * @throws IllegalStateException if the returns file cannot be written
     */
    private void saveCatalogue() {
        try {
            repository.saveAll(returns);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar el historial de devoluciones", e);
        }
    }
}