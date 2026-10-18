package com.gamezone.persistence;

import com.gamezone.model.BasicWarranty;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Warranty;

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
 * Repository in charge of saving and loading the warranties from a file managed
 * by the application itself.
 * <p>
 * The file is {@code data/warranties.csv}. Every record uses the semicolon
 * character as the field delimiter and has five columns:
 * {@code type;id;productId;saleId;startDate}, for example:
 * <pre>
 * type;id;productId;saleId;startDate
 * BASIC;W0001;C-01;S0003;2026-10-18
 * EXTENDED;W0002;C-01;S0003;2026-10-18
 * </pre>
 * The end date is not stored,
 * because the constructor of the warranty computes it again from its duration,
 * so storing it would risk a duplicated value that could disagree with the
 * duration.
 * <p>
 * This repository stores **only identifiers**. It never rebuilds a sale or a
 * product, because those objects are already held by the rest of the system.
 * {@link #loadAll(List, List)} receives the sales and the catalogue that were
 * already loaded and links every warranty to them, the same way
 * {@code SaleRepository} does. That is what keeps the dependency graph free of
 * a cycle: the repository needs no service, so the objects can be built in
 * {@code Main} in any order.
 * <p>
 * A line is skipped, and no warranty is created, when it is the header, when it
 * does not have the expected number of columns, when its date does not follow
 * the format, when its type is unknown, or when the sale or the product it
 * references no longer exist. An empty list is returned when the file does not
 * exist, which is the normal case before the first warranty is assigned.
 * <p>
 * This class belongs to the persistence layer and therefore is one of the few
 * classes allowed to perform file access. It depends only on the model layer,
 * never on the services nor on the user interface.
 */
public class WarrantyRepository {

    /** Character used to separate the columns of a record. */
    private static final String DELIMITER = ";";

    /** Value of the first column of the header line. */
    private static final String HEADER_TYPE = "type";

    /** Number of columns of a record, including the header layout. */
    private static final int COLUMN_COUNT = 5;

    /** Type discriminator of a basic warranty record. */
    private static final String TYPE_BASIC = "BASIC";

    /** Type discriminator of an extended warranty record. */
    private static final String TYPE_EXTENDED = "EXTENDED";

    /** Location of the file where the warranties are stored. */
    private final Path filePath;

    /**
     * Builds the repository pointing to the file it uses.
     *
     * @param filePath path of the warranties file
     */
    public WarrantyRepository(String filePath) {
        this.filePath = Paths.get(filePath);
    }

    /**
     * Saves every warranty, replacing the previous content of the file. The
     * header line is written first so the file keeps its documented layout.
     *
     * @param warranties warranties to persist
     * @throws IOException if the file cannot be written
     */
    public void saveAll(List<Warranty> warranties) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(String.join(DELIMITER, "type", "id", "productId", "saleId", "startDate"));
        for (Warranty warranty : warranties) {
            lines.add(serialize(warranty));
        }
        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(filePath, lines, StandardCharsets.UTF_8);
    }

    /**
     * Loads every warranty stored in the file and links each one to the sale and
     * the product it references.
     * <p>
     * The header line, the damaged records and the records whose sale or product
     * no longer exist are skipped.
     *
     * @param sales   sales already loaded, used to resolve the sale of a warranty
     * @param catalog products already loaded, used to resolve the product of a
     *                warranty
     * @return list of the warranties that could be rebuilt, empty when the file
     *         does not exist
     * @throws IOException if the file cannot be read
     */
    public List<Warranty> loadAll(List<Sale> sales, List<Product> catalog) throws IOException {
        List<Warranty> warranties = new ArrayList<>();
        for (String record : readRecords()) {
            Warranty warranty = deserialize(record, sales, catalog);
            if (warranty != null) {
                warranties.add(warranty);
            }
        }
        return warranties;
    }

    /**
     * Converts a warranty into the record that represents it in the file. Only
     * the identifiers of the product and the sale are written, never the objects.
     *
     * @param warranty warranty to convert
     * @return the text record that represents the warranty
     */
    private String serialize(Warranty warranty) {
        return String.join(DELIMITER,
                getTypeOf(warranty),
                warranty.getId(),
                warranty.getProduct().getId(),
                warranty.getSale().getId(),
                String.valueOf(warranty.getStartDate()));
    }

    /**
     * Rebuilds a warranty from a record, using the type of its first column to
     * decide which concrete class has to be instantiated.
     * <p>
     * Null is returned when the record cannot be used: it is the header, its
     * columns are incomplete, its type is unknown, its date does not follow the
     * format, or the sale or the product it references is not among the objects
     * that were already loaded. Skipping instead of failing is what allows a
     * warranty of a deleted product not to stop the application from starting.
     *
     * @param record  text record read from the file
     * @param sales   sales already loaded
     * @param catalog products already loaded
     * @return the warranty represented by the record, or null when the record
     *         cannot be interpreted
     */
    private Warranty deserialize(String record, List<Sale> sales, List<Product> catalog) {
        String[] columns = record.split(DELIMITER, -1);
        if (columns.length < COLUMN_COUNT || columns.length > COLUMN_COUNT) {
            return null;
        }
        for (int index = 0; index < COLUMN_COUNT; index++) {
            columns[index] = columns[index].trim();
        }
        String type = columns[0];
        if (HEADER_TYPE.equalsIgnoreCase(type)) {
            return null;
        }
        if (columns[1].isEmpty() || columns[2].isEmpty() || columns[3].isEmpty()) {
            return null;
        }
        Product product = findProduct(catalog, columns[2]);
        Sale sale = findSale(sales, columns[3]);
        if (product == null || sale == null) {
            return null;
        }
        try {
            LocalDate startDate = LocalDate.parse(columns[4]);
            if (TYPE_BASIC.equalsIgnoreCase(type)) {
                return new BasicWarranty(columns[1], product, sale, startDate);
            }
            if (TYPE_EXTENDED.equalsIgnoreCase(type)) {
                return new ExtendedWarranty(columns[1], product, sale, startDate);
            }
        } catch (java.time.format.DateTimeParseException e) {
            return null;
        }
        return null;
    }

    /**
     * Looks for the product a warranty covers.
     *
     * @param catalog products already loaded
     * @param id      product identifier stored in the record
     * @return the product found, or null when it no longer exists
     */
    private Product findProduct(List<Product> catalog, String id) {
        for (Product product : catalog) {
            if (product.getId().equals(id)) {
                return product;
            }
        }
        return null;
    }

    /**
     * Looks for the sale a warranty comes from.
     *
     * @param sales sales already loaded
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
     * Returns the type marker stored in the first column of the file for a
     * warranty. The marker is derived from the concrete class instead of using
     * {@code getWarrantyType()}, because that method returns the name shown to
     * the user, {@code "Garantía Básica"}, and not the marker of the file.
     *
     * @param warranty warranty to inspect
     * @return the type marker of the warranty
     */
    private String getTypeOf(Warranty warranty) {
        if (warranty instanceof BasicWarranty) {
            return TYPE_BASIC;
        }
        return TYPE_EXTENDED;
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