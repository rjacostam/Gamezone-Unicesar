package com.gamezone.persistence;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.BulkPurchaseDiscount;
import com.gamezone.model.CategoryDiscount;
import com.gamezone.model.PercentageDiscount;
import com.gamezone.model.Promotion;

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
 * Repository in charge of saving and loading the promotions from a file managed
 * by the application itself.
 * <p>
 * The file is {@code data/promotions.csv}. Every record uses the semicolon
 * character as the field delimiter and has seven columns:
 * {@code type;id;name;startDate;endDate;percentage;extra}. The first column is
 * the type of the promotion and it is what tells this repository which concrete
 * class has to be rebuilt. The last column, {@code extra}, is the target
 * category for a category discount, the minimum quantity for a bulk purchase
 * discount, and empty for a percentage discount.
 * <p>
 * The dates use the ISO format {@code yyyy-MM-dd}, which is the format
 * {@link LocalDate#parse(CharSequence)} reads, so the file can be written and
 * read without any custom format.
 * <p>
 * The file starts with a header line, which is ignored while reading. A record
 * whose columns are incomplete, whose type is unknown, whose dates do not
 * follow the format or whose numbers cannot be parsed is ignored as well, so
 * one damaged line never prevents the application from starting. An empty list
 * is returned when the file does not exist, so the first execution works
 * without pre-existing data.
 * <p>
 * This class belongs to the persistence layer and therefore is one of the few
 * classes allowed to perform file access. It depends only on the model layer,
 * never on the services nor on the user interface.
 */
public class PromotionRepository {

    /** Character used to separate the columns of a record. */
    private static final String DELIMITER = ";";

    /** Value of the first column of the header line. */
    private static final String HEADER_TYPE = "type";

    /** Number of columns of a record, including the header layout. */
    private static final int COLUMN_COUNT = 7;

    /** Type discriminator of a percentage discount record. */
    private static final String TYPE_PERCENTAGE = "PERCENTAGE";

    /** Type discriminator of a category discount record. */
    private static final String TYPE_CATEGORY = "CATEGORY";

    /** Type discriminator of a bulk purchase discount record. */
    private static final String TYPE_BULK = "BULK";

    /** Location of the file where the promotions are stored. */
    private final Path filePath;

    /**
     * Builds the repository pointing to the file it uses.
     *
     * @param filePath path of the promotions file
     */
    public PromotionRepository(String filePath) {
        this.filePath = Paths.get(filePath);
    }

    /**
     * Saves every promotion, replacing the previous content of the file. The
     * header line is written first so the file keeps its documented layout.
     *
     * @param promotions promotions to persist
     * @throws PersistenceException if the file cannot be written
     */
    public void saveAll(List<Promotion> promotions) {
        List<String> lines = new ArrayList<>();
        lines.add(String.join(DELIMITER,
                "type", "id", "name", "startDate", "endDate", "percentage", "extra"));
        for (Promotion promotion : promotions) {
            lines.add(serialize(promotion));
        }
        Path parent = filePath.getParent();
        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(filePath, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error de escritura en el archivo " + filePath, e);
        }
    }

    /**
     * Loads every promotion stored in the file.
     * <p>
     * The header line and the damaged records are ignored.
     *
     * @return list of the stored promotions, empty when the file does not exist
     * @throws PersistenceException if the file cannot be read
     */
    public List<Promotion> loadAll() {
        List<Promotion> promotions = new ArrayList<>();
        try {
            for (String record : readRecords()) {
                Promotion promotion = deserialize(record);
                if (promotion != null) {
                    promotions.add(promotion);
                }
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error de lectura en el archivo " + filePath, e);
        }
        return promotions;
    }

    /**
     * Converts a promotion into the record that represents it in the file.
     *
     * @param promotion promotion to convert
     * @return the text record that represents the promotion
     */
    private String serialize(Promotion promotion) {
        StringBuilder builder = new StringBuilder();
        builder.append(promotion.getTypeCode()).append(DELIMITER);
        builder.append(promotion.getId()).append(DELIMITER);
        builder.append(promotion.getName()).append(DELIMITER);
        builder.append(promotion.getStartDate()).append(DELIMITER);
        builder.append(promotion.getEndDate()).append(DELIMITER);
        if (promotion instanceof CategoryDiscount) {
            CategoryDiscount discount = (CategoryDiscount) promotion;
            builder.append(discount.getPercentage()).append(DELIMITER)
                    .append(valueOf(discount.getTargetCategory()));
        } else if (promotion instanceof BulkPurchaseDiscount) {
            BulkPurchaseDiscount discount = (BulkPurchaseDiscount) promotion;
            builder.append(discount.getPercentage()).append(DELIMITER)
                    .append(discount.getMinimumQuantity());
        } else if (promotion instanceof PercentageDiscount) {
            builder.append(((PercentageDiscount) promotion).getPercentage())
                    .append(DELIMITER).append("");
        }
        return builder.toString();
    }

    /**
     * Rebuilds a promotion from a record, using the type of its first column to
     * decide which concrete class has to be instantiated.
     * <p>
     * A record is ignored, and null is returned, when it is the header, when it
     * does not have the expected number of columns, when its type is unknown,
     * when one of its numbers cannot be parsed or when one of its dates does not
     * follow the {@code yyyy-MM-dd} format.
     *
     * @param record text record read from the file
     * @return the promotion represented by the record, or null when the record
     *         cannot be interpreted
     */
    private Promotion deserialize(String record) {
        String[] fields = record.split(DELIMITER, -1);
        if (fields.length < COLUMN_COUNT - 1 || fields.length > COLUMN_COUNT) {
            return null;
        }
        String[] columns = new String[COLUMN_COUNT];
        System.arraycopy(fields, 0, columns, 0, fields.length);
        for (int index = fields.length; index < COLUMN_COUNT; index++) {
            columns[index] = "";
        }
        for (int index = 0; index < COLUMN_COUNT; index++) {
            columns[index] = columns[index].trim();
        }

        String type = columns[0];
        if (HEADER_TYPE.equalsIgnoreCase(type)) {
            return null;
        }
        if (columns[1].isEmpty() || columns[2].isEmpty()) {
            return null;
        }

        try {
            LocalDate startDate = LocalDate.parse(columns[3]);
            LocalDate endDate = LocalDate.parse(columns[4]);
            double percentage = Double.parseDouble(columns[5]);
            if (TYPE_PERCENTAGE.equalsIgnoreCase(type)) {
                return new PercentageDiscount(columns[1], columns[2], startDate, endDate, percentage);
            }
            if (TYPE_CATEGORY.equalsIgnoreCase(type)) {
                return new CategoryDiscount(columns[1], columns[2], startDate, endDate,
                        percentage, columns[6]);
            }
            if (TYPE_BULK.equalsIgnoreCase(type)) {
                return new BulkPurchaseDiscount(columns[1], columns[2], startDate, endDate,
                        Integer.parseInt(columns[6]), percentage);
            }
        } catch (NumberFormatException | java.time.format.DateTimeParseException e) {
            return null;
        }
        return null;
    }

    /**
     * Returns the text to write for a value that may be null, so a null never
     * reaches the file as the word {@code null}.
     *
     * @param value value to write
     * @return the value, or an empty string when it is null
     */
    private String valueOf(String value) {
        return value == null ? "" : value;
    }

    /**
     * Reads every non blank record of the file. An empty list is returned when
     * the file does not exist yet.
     *
     * @param file file to read
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