package com.gamezone.persistence;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.Customer;
import com.gamezone.model.Seller;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository in charge of saving and loading the people of the store from the
 * files managed by the application itself.
 * <p>
 * Customers and sellers are kept in two separate files, {@code customers.csv}
 * and {@code sellers.csv}. Separating them means the role of a record is given
 * by the file it comes from, so no type column is needed to rebuild the correct
 * subclass.
 * <p>
 * Every record uses the semicolon character as the field delimiter, because
 * names may legitimately contain commas. The format of a customer record is
 * {@code id;name;phone;email} and the format of a seller record is
 * {@code id;name;phone;employeeCode;shift}. Both formats are plain text, so the
 * files can be inspected and edited without running the application.
 * <p>
 * The values are stored exactly as the application produced them: the trimming
 * and the lower casing of the e-mail address are done by the domain classes, so
 * this repository never changes the content of a record.
 * <p>
 * This class belongs to the persistence layer and therefore is one of the few
 * classes allowed to perform file access. It depends only on the model layer,
 * never on the services nor on the user interface.
 *
 * @author Jose Cervantes
 * @since 1.0.0
 */
public class PersonRepository {

    /** Character used to separate the fields of a record. */
    private static final String DELIMITER = ";";

    /** Number of fields of a customer record. */
    private static final int CUSTOMER_FIELD_COUNT = 4;

    /** Number of fields of a seller record. */
    private static final int SELLER_FIELD_COUNT = 5;

    /** Location of the file where the customers are stored. */
    private final Path customersFile;

    /** Location of the file where the sellers are stored. */
    private final Path sellersFile;

    /**
     * Builds the repository pointing to the two files it uses.
     *
     * @param customersFile path of the customers file
     * @param sellersFile   path of the sellers file
     */
    public PersonRepository(String customersFile, String sellersFile) {
        this.customersFile = Paths.get(customersFile);
        this.sellersFile = Paths.get(sellersFile);
    }

    /**
     * Saves every customer, replacing the previous content of the file.
     *
     * @param customers customers to persist
     * @throws PersistenceException if the file cannot be written
     */
    public void saveCustomers(List<Customer> customers) {
        List<String> lines = new ArrayList<>();
        for (Customer customer : customers) {
            lines.add(String.join(DELIMITER,
                    customer.getId(),
                    customer.getName(),
                    customer.getPhone(),
                    customer.getEmail()));
        }
        try {
            write(customersFile, lines);
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error de escritura en el archivo " + customersFile, e);
        }
    }

    /**
     * Loads every customer stored in the customers file.
     * <p>
     * A damaged record is ignored instead of interrupting the load, so a single
     * corrupted line never prevents the application from starting.
     *
     * @return list of the stored customers, empty when the file does not exist
     * @throws PersistenceException if the file cannot be read
     */
    public List<Customer> loadCustomers() {
        List<Customer> customers = new ArrayList<>();
        for (String record : readQuietly(customersFile)) {
            String[] fields = record.split(DELIMITER, -1);
            if (fields.length == CUSTOMER_FIELD_COUNT && hasContent(fields)) {
                customers.add(new Customer(
                        fields[0].trim(),
                        fields[1].trim(),
                        fields[2].trim(),
                        fields[3].trim()));
            }
        }
        return customers;
    }

    /**
     * Saves every seller, replacing the previous content of the file.
     *
     * @param sellers sellers to persist
     * @throws PersistenceException if the file cannot be written
     */
    public void saveSellers(List<Seller> sellers) {
        List<String> lines = new ArrayList<>();
        for (Seller seller : sellers) {
            lines.add(String.join(DELIMITER,
                    seller.getId(),
                    seller.getName(),
                    seller.getPhone(),
                    seller.getEmployeeCode(),
                    seller.getShift()));
        }
        try {
            write(sellersFile, lines);
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error de escritura en el archivo " + sellersFile, e);
        }
    }

    /**
     * Loads every seller stored in the sellers file.
     * <p>
     * A damaged record is ignored instead of interrupting the load, so a single
     * corrupted line never prevents the application from starting.
     *
     * @return list of the stored sellers, empty when the file does not exist
     * @throws PersistenceException if the file cannot be read
     */
    public List<Seller> loadSellers() {
        List<Seller> sellers = new ArrayList<>();
        for (String record : readQuietly(sellersFile)) {
            String[] fields = record.split(DELIMITER, -1);
            if (fields.length == SELLER_FIELD_COUNT && hasContent(fields)) {
                sellers.add(new Seller(
                        fields[0].trim(),
                        fields[1].trim(),
                        fields[2].trim(),
                        fields[3].trim(),
                        fields[4].trim()));
            }
        }
        return sellers;
    }

    /**
     * Checks that every field of a record holds actual content.
     * <p>
     * A record with a blank field is considered damaged and is ignored by the
     * load methods, because it would produce a person with missing information.
     * The domain constructors do not validate, so skipping these records here
     * is what keeps a damaged file from reaching the rest of the system.
     *
     * @param fields fields of the record
     * @return true when every field has content
     */
    private static boolean hasContent(String[] fields) {
        for (String field : fields) {
            if (field.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Writes the given records into a file, creating the parent folders when
     * they do not exist yet.
     *
     * @param file   destination file
     * @param records records to write, one per line
     * @throws IOException if the file cannot be written
     */
    private void write(Path file, List<String> records) throws IOException {
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, records, StandardCharsets.UTF_8);
    }

    /**
     * Reads every record of a file. An empty list is returned when the file
     * does not exist yet, so the first execution of the system works without
     * pre-existing data. Blank lines are ignored here, and the load methods
     * ignore the records whose fields are incomplete.
     *
     * @param file file to read
     * @return the records contained in the file
     * @throws IOException if the file cannot be read
     */
    /**
     * Reads every record of a file and turns a failure of the disk into the
     * typed exception of the project, naming the file that could not be read.
     *
     * @param file file to read
     * @return the records contained in the file
     * @throws PersistenceException if the file cannot be read
 */
    private List<String> readQuietly(Path file) {
        try {
            return readRecords(file);
        } catch (IOException e) {
            throw new PersistenceException("Error de lectura en el archivo " + file, e);
        }
    }

    private List<String> readRecords(Path file) throws IOException {
        List<String> records = new ArrayList<>();
        if (!Files.exists(file)) {
            return records;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
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