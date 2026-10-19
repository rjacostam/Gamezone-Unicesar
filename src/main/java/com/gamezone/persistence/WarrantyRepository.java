package com.gamezone.persistence;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.BasicWarranty;
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
 * product, and it does not even import them: reading the file produces
 * {@link WarrantyRecord} values, and {@code WarrantyService} is the class that
 * turns each record into a {@code Warranty}, looking the sale and the product up
 * among the objects that are already loaded. That is what keeps the persistence
 * layer depending on the model alone, with no reference to the service layer,
 * and it is what avoids the cycle
 * {@code SaleService → WarrantyService → WarrantyRepository → SaleService},
 * because the repository can no longer reach a service.
 * <p>
 * A line is skipped, and no record is produced, when it is the header, when it
 * does not have the expected number of columns, when an identifier is empty, or
 * when its date does not follow the format. An empty list is returned when the
 * file does not exist, which is the normal case before the first warranty is
 * assigned.
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
     * @throws PersistenceException if the file cannot be written
     */
    public void saveAll(List<Warranty> warranties) {
        List<String> lines = new ArrayList<>();
        lines.add(String.join(DELIMITER, "type", "id", "productId", "saleId", "startDate"));
        for (Warranty warranty : warranties) {
            lines.add(serialize(warranty));
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
     * Loads every warranty stored in the file, as plain records.
     * <p>
     * The header line and the damaged records are ignored. A line is only
     * rejected when the line itself cannot be read: the wrong number of
     * columns, an empty identifier or a date that does not follow the format.
     * Whether a record can actually become a warranty, which depends on its sale
     * and its product still existing, is not decided here, because this layer
     * does not know them: {@code WarrantyService} is the class that resolves
     * them.
     *
     * @return list of the records contained in the file, empty when the file does
     *         not exist
     * @throws PersistenceException if the file cannot be read
     */
    public List<WarrantyRecord> loadAll() {
        List<WarrantyRecord> records = new ArrayList<>();
        try {
            for (String record : readRecords()) {
                WarrantyRecord parsed = toRecord(record);
                if (parsed != null) {
                    records.add(parsed);
                }
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error de lectura en el archivo " + filePath, e);
        }
        return records;
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
     /**
     * Builds a record from one line of the file, keeping only the identifiers.
     * <p>
     * Null is returned when the line is the header, when its columns are
     * incomplete, when an identifier is empty, or when its date does not follow
     * the format. The type is stored as it is read, without deciding whether it
     * is a known one, because turning a record into a warranty is not the job of
     * this layer.
     *
     * @param line text record read from the file
     * @return the record represented by the line, or null when the line cannot
     *         be interpreted
     */
    private WarrantyRecord toRecord(String line) {
        String[] columns = line.split(DELIMITER, -1);
        if (columns.length < COLUMN_COUNT || columns.length > COLUMN_COUNT) {
            return null;
        }
        for (int index = 0; index < COLUMN_COUNT; index++) {
            columns[index] = columns[index].trim();
        }
        if (HEADER_TYPE.equalsIgnoreCase(columns[0])) {
            return null;
        }
        if (columns[1].isEmpty() || columns[2].isEmpty() || columns[3].isEmpty()) {
            return null;
        }
        try {
            return new WarrantyRecord(columns[0], columns[1], columns[2], columns[3],
                    LocalDate.parse(columns[4]));
        } catch (java.time.format.DateTimeParseException e) {
            return null;
        }
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