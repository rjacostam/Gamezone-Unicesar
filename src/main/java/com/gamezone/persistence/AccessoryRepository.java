package com.gamezone.persistence;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.Accessory;
import com.gamezone.model.Cable;
import com.gamezone.model.Controller;
import com.gamezone.model.Memory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository in charge of saving and loading the accessories from a file
 * managed by the application itself.
 * <p>
 * The file is {@code data/accessories.csv}. Every record uses the semicolon
 * character as the field delimiter and has eight columns:
 * {@code type;id;title;price;stock;compatibleConsoles;extra1;extra2}. The first
 * column is the type written by {@code getAccessoryType()} and it is what tells
 * this repository which concrete class has to be rebuilt. The two extra columns
 * carry the attributes of the concrete type: connection type for a controller,
 * length and connector for a cable, capacity and memory type for a memory card.
 * The compatible console ids are separated by commas inside their own column.
 * <p>
 * The file starts with a header line, which is ignored while reading. A
 * record whose columns are incomplete, whose type is unknown or whose numbers
 * cannot be parsed is ignored as well, so one damaged line never prevents the
 * application from starting. An empty list is returned when the file does not
 * exist, so the first execution works without pre-existing data.
 * <p>
 * This class belongs to the persistence layer and therefore is one of the few
 * classes allowed to perform file access. It depends only on the model layer,
 * never on the services nor on the user interface.
 */
public class AccessoryRepository {

    /** Character used to separate the columns of a record. */
    private static final String DELIMITER = ";";

    /** Character used to separate the console ids inside their column. */
    private static final String CONSOLE_SEPARATOR = ",";

    /** Value of the first column of the header line. */
    private static final String HEADER_TYPE = "type";

    /** Number of columns of a record, including the header layout. */
    private static final int COLUMN_COUNT = 8;

    /** Type discriminator of a controller record. */
    private static final String TYPE_CONTROLLER = "CONTROLLER";

    /** Type discriminator of a cable record. */
    private static final String TYPE_CABLE = "CABLE";

    /** Type discriminator of a memory record. */
    private static final String TYPE_MEMORY = "MEMORY";

    /** Location of the file where the accessories are stored. */
    private final Path filePath;

    /**
     * Builds the repository pointing to the file it uses.
     *
     * @param filePath path of the accessories file
     */
    public AccessoryRepository(String filePath) {
        this.filePath = Paths.get(filePath);
    }

    /**
     * Saves every accessory, replacing the previous content of the file. The
     * header line is written first so the file keeps its documented layout.
     *
     * @param accessories accessories to persist
     * @throws PersistenceException if the file cannot be written
     */
    public void saveAll(List<Accessory> accessories) {
        List<String> lines = new ArrayList<>();
        lines.add(String.join(DELIMITER,
                "type", "id", "title", "price", "stock", "compatibleConsoles", "extra1", "extra2"));
        for (Accessory accessory : accessories) {
            lines.add(serialize(accessory));
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
     * Loads every accessory stored in the file.
     * <p>
     * The header line and the damaged records are ignored.
     *
     * @return list of the stored accessories, empty when the file does not exist
     * @throws PersistenceException if the file cannot be read
     */
    public List<Accessory> loadAll() {
        List<Accessory> accessories = new ArrayList<>();
        try {
            for (String record : readRecords()) {
                Accessory accessory = deserialize(record);
                if (accessory != null) {
                    accessories.add(accessory);
                }
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error de lectura en el archivo " + filePath, e);
        }
        return accessories;
    }

    /**
     * Converts an accessory into the record that represents it in the file.
     *
     * @param accessory accessory to convert
     * @return the text record that represents the accessory
     */
    private String serialize(Accessory accessory) {
        StringBuilder builder = new StringBuilder();
        builder.append(accessory.getAccessoryType()).append(DELIMITER);
        builder.append(accessory.getId()).append(DELIMITER);
        builder.append(accessory.getTitle()).append(DELIMITER);
        builder.append(accessory.getPrice()).append(DELIMITER);
        builder.append(accessory.getStock()).append(DELIMITER);
        builder.append(String.join(CONSOLE_SEPARATOR, accessory.getCompatibleConsoleIds()))
                .append(DELIMITER);
        builder.append(String.join(DELIMITER, accessory.getExtraFields()));
        return builder.toString();
    }

    /**
     * Rebuilds an accessory from a record, using the type of its first column to
     * decide which concrete class has to be instantiated.
     * <p>
     * A record is ignored, and null is returned, when it is the header, when it
     * does not have the expected number of columns, when its type is unknown or
     * when one of its numbers cannot be parsed.
     *
     * @param record text record read from the file
     * @return the accessory represented by the record, or null when the record
     *         cannot be interpreted
     */
    private Accessory deserialize(String record) {
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

        List<String> compatibleConsoleIds = parseConsoleIds(columns[5]);
        try {
            double price = Double.parseDouble(columns[3]);
            int stock = Integer.parseInt(columns[4]);
            if (TYPE_CONTROLLER.equalsIgnoreCase(type)) {
                return new Controller(columns[1], columns[2], price, stock,
                        compatibleConsoleIds, columns[6]);
            }
            if (TYPE_CABLE.equalsIgnoreCase(type)) {
                return new Cable(columns[1], columns[2], price, stock,
                        compatibleConsoleIds, Double.parseDouble(columns[6]), columns[7]);
            }
            if (TYPE_MEMORY.equalsIgnoreCase(type)) {
                return new Memory(columns[1], columns[2], price, stock,
                        compatibleConsoleIds, Integer.parseInt(columns[6]), columns[7]);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    /**
     * Splits the column of compatible console ids into a list. An empty column
     * produces an empty list, which the accessory treats as an accessory without
     * compatible consoles.
     *
     * @param value content of the compatible consoles column
     * @return the console ids found in the column
     */
    private List<String> parseConsoleIds(String value) {
        List<String> ids = new ArrayList<>();
        if (value.isEmpty()) {
            return ids;
        }
        for (String id : value.split(CONSOLE_SEPARATOR)) {
            String trimmed = id.trim();
            if (!trimmed.isEmpty()) {
                ids.add(trimmed);
            }
        }
        return ids;
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