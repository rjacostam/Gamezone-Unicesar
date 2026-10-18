package com.gamezone.persistence;

import com.gamezone.model.Product;
import com.gamezone.model.Return;
import com.gamezone.model.Sale;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository in charge of saving and loading the returns from a file managed by
 * the application itself.
 * <p>
 * The file is {@code data/returns.csv}. Every record uses the semicolon
 * character as the field delimiter and has five columns:
 * {@code id;date;saleId;productIds;reason}, for example:
 * <pre>
 * id;date;saleId;productIds;reason
 * R0001;2026-10-18;S0001;C-01,A-01;No funciona
 * </pre>
 * The refunded amount is not stored, because the constructor of {@code Return}
 * computes it again from the returned units. The product identifiers are
 * separated by commas inside their own column. A semicolon inside the reason is
 * written as a comma, because a semicolon is the delimiter and a reason that
 * contains it would break the record; the reason is therefore read back with a
 * comma where the customer had written a semicolon.
 * <p>
 * This repository stores **only identifiers** and never rebuilds a sale, the
 * same way the repositories of sales and warranties do. {@link #loadAll(List)}
 * receives the sales that were already loaded and links each return to its sale
 * by id, and each returned unit to the product of that sale with the same id.
 * The sale alone is enough to find the product, because a product can only be
 * returned when it belongs to the sale, and that also works for video games,
 * consoles and accessories.
 * <p>
 * A line is skipped, and no return is created, when it is the header, when it
 * does not have the expected number of columns, when its date does not follow
 * the format, or when its sale or one of its products cannot be found. An empty
 * list is returned when the file does not exist, which is the normal case
 * before the first return is registered.
 * <p>
 * This class belongs to the persistence layer and therefore is one of the few
 * classes allowed to perform file access. It depends only on the model layer,
 * never on the services nor on the user interface.
 */
public class ReturnRepository {

    /** Character used to separate the columns of a record. */
    private static final String DELIMITER = ";";

    /** Character used to separate the product identifiers inside their column. */
    private static final String ID_SEPARATOR = ",";

    /** Number of columns of a record, including the header layout. */
    private static final int COLUMN_COUNT = 5;

    /** Location of the file where the returns are stored. */
    private final Path filePath;

    /**
     * Builds the repository pointing to the file it uses.
     *
     * @param filePath path of the returns file
     */
    public ReturnRepository(String filePath) {
        this.filePath = Paths.get(filePath);
    }

    /**
     * Saves every return, replacing the previous content of the file. The header
     * line is written first so the file keeps its documented layout.
     *
     * @param returns returns to persist
     * @throws IOException if the file cannot be written
     */
    public void saveAll(List<Return> returns) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(String.join(DELIMITER, "id", "date", "saleId", "productIds", "reason"));
        for (Return registeredReturn : returns) {
            lines.add(serialize(registeredReturn));
        }
        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(filePath, lines, StandardCharsets.UTF_8);
    }

    /**
     * Loads every return stored in the file and links each one to its sale and to
     * the returned units inside that sale.
     * <p>
     * The header line, the damaged records and the records whose sale or product
     * cannot be found are skipped.
     *
     * @param sales sales already registered, used to resolve the sale and the
     *              products of each return
     * @return list of the returns that could be rebuilt, empty when the file does
     *         not exist
     * @throws IOException if the file cannot be read
     */
    public List<Return> loadAll(List<Sale> sales) throws IOException {
        List<Return> returns = new ArrayList<>();
        for (String record : readRecords()) {
            Return registeredReturn = deserialize(record, sales);
            if (registeredReturn != null) {
                returns.add(registeredReturn);
            }
        }
        return returns;
    }

    /**
     * Converts a return into the record that represents it in the file. Only the
     * identifier of the sale is written, never the objects.
     *
     * @param registeredReturn return to convert
     * @return the text record that represents the return
     */
    private String serialize(Return registeredReturn) {
        StringBuilder productIds = new StringBuilder();
        List<Product> returnedProducts = registeredReturn.getReturnedProducts();
        for (int index = 0; index < returnedProducts.size(); index++) {
            if (index > 0) {
                productIds.append(ID_SEPARATOR);
            }
            productIds.append(returnedProducts.get(index).getId());
        }
        String reason = registeredReturn.getReason() == null
                ? "" : registeredReturn.getReason().replace(DELIMITER, ID_SEPARATOR);
        return String.join(DELIMITER,
                registeredReturn.getId(),
                String.valueOf(registeredReturn.getDate()),
                registeredReturn.getSale().getId(),
                productIds.toString(),
                reason);
    }

    /**
     * Rebuilds a return from a record, using the identifier of the sale to
     * resolve the sale and, inside it, the returned products.
     * <p>
     * Null is returned when the record cannot be used: it is the header, its
     * columns are incomplete, its date does not follow the format, its sale no
     * longer exists, or one of its product identifiers is not part of that sale.
     * Skipping instead of failing is what allows a return of a deleted product
     * not to stop the application from starting.
     *
     * @param record text record read from the file
     * @param sales  sales already registered
     * @return the return represented by the record, or null when the record
     *         cannot be interpreted
     */
    private Return deserialize(String record, List<Sale> sales) {
        String[] columns = record.split(DELIMITER, -1);
        if (columns.length < COLUMN_COUNT || columns.length > COLUMN_COUNT) {
            return null;
        }
        for (int index = 0; index < COLUMN_COUNT; index++) {
            columns[index] = columns[index].trim();
        }
        if (columns[0].equalsIgnoreCase("id") && columns[1].equalsIgnoreCase("date")) {
            return null;
        }
        if (columns[0].isEmpty() || columns[2].isEmpty() || columns[3].isEmpty()) {
            return null;
        }
        Sale sale = findSale(sales, columns[2]);
        if (sale == null) {
            return null;
        }
        List<Product> returnedProducts = findProducts(sale, columns[3]);
        if (returnedProducts == null) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(columns[1]);
            return new Return(columns[0], date, sale, returnedProducts, columns[4]);
        } catch (java.time.format.DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Looks for the sale a return comes from.
     *
     * @param sales sales already registered
     * @param id    sale identifier stored in the record
     * @return the sale found, or null when it no longer exists
     */
    private Sale findSale(List<Sale> sales, String id) {
        for (Sale sale : sales) {
            if (sale.getId().equals(id)) {
                return sale;
            }
        }
        return null;
    }

    /**
     * Resolves the returned units inside the sale, keeping one entry per unit so
     * that two units of the same product produce two entries and the refunded
     * amount adds the price twice.
     *
     * @param sale          sale the return comes from
     * @param productIdsCell content of the product identifiers column
     * @return the products found, or null when one of the identifiers is not part
     *         of the sale
     */
    private List<Product> findProducts(Sale sale, String productIdsCell) {
        List<Product> returnedProducts = new ArrayList<>();
        for (String productId : productIdsCell.split(ID_SEPARATOR)) {
            String wanted = productId.trim();
            if (wanted.isEmpty()) {
                continue;
            }
            Product found = findProduct(sale, wanted);
            if (found == null) {
                return null;
            }
            returnedProducts.add(found);
        }
        return returnedProducts.isEmpty() ? null : returnedProducts;
    }

    /**
     * Looks for a product inside the units of a sale.
     *
     * @param sale      sale the return comes from
     * @param productId identifier of the returned unit
     * @return the product found, or null when the sale does not contain it
     */
    private Product findProduct(Sale sale, String productId) {
        for (Product product : sale.getProducts()) {
            if (product.getId().equals(productId)) {
                return product;
            }
        }
        return null;
    }

    /**
     * Reads every non blank record of the file. An empty list is returned when
     * the file does not exist yet.
     *
     * @return the records contained in the file
     * @throws IOException if the file cannot be read
     */
    private List<String> readRecords() throws IOException {
        List<String> records = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return records;
        }
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line = reader.readLine();
            while (line != null) {
                String record = line.trim();
                if (!record.isEmpty()) {
                    records.add(record);
                }
                line = reader.readLine();
            }
        }
        return records;
    }
}