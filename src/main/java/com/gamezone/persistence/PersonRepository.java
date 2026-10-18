package com.gamezone.persistence;

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
 * {@code id;name;phone;employeeCode;shift}.
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
     * @throws IOException if the file cannot be written
     */
    public void saveCustomers(List<Customer> customers) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Customer customer : customers) {
            lines.add(String.join(DELIMITER,
                    customer.getId(),
                    customer.getName(),
                    customer.getPhone(),
                    customer.getEmail()));
        }
        write(customersFile, lines);
    }

    /**
     * Loads every customer stored in the customers file.
     *
     * @return list of the stored customers, empty when the file does not exist
     * @throws IOException if the file cannot be read
     */
    public List<Customer> loadCustomers() throws IOException {
        List<Customer> customers = new ArrayList<>();
        for (String record : read(customersFile)) {
            String[] fields = record.split(DELIMITER, -1);
            if (fields.length == CUSTOMER_FIELD_COUNT) {
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
     * @throws IOException if the file cannot be written
     */
    public void saveSellers(List<Seller> sellers) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Seller seller : sellers) {
            lines.add(String.join(DELIMITER,
                    seller.getId(),
                    seller.getName(),
                    seller.getPhone(),
                    seller.getEmployeeCode(),
                    seller.getShift()));
        }
        write(sellersFile, lines);
    }

    /**
     * Loads every seller stored in the sellers file.
     *
     * @return list of the stored sellers, empty when the file does not exist
     * @throws IOException if the file cannot be read
     */
    public List<Seller> loadSellers() throws IOException {
        List<Seller> sellers = new ArrayList<>();
        for (String record : read(sellersFile)) {
            String[] fields = record.split(DELIMITER, -1);
            if (fields.length == SELLER_FIELD_COUNT) {
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
     * pre-existing data. Blank lines and records with an unexpected number of
     * fields are ignored, so a single corrupted line does not prevent the
     * application from starting.
     *
     * @param file file to read
     * @return the records contained in the file
     * @throws IOException if the file cannot be read
     */
    private List<String> read(Path file) throws IOException {
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