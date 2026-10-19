package com.gamezone.persistence;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.Customer;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves and loads sales from a CSV file.
 * <p>
 * Each line stores the sale id, its date, the ids of its customer and seller, the ids of
 * the products sold separated by commas, the applied promotion and discount, and the cost
 * of the extended warranties:
 * {@code S0001;2026-10-18;123;1098765432;VG-01,VG-01,C-01;Lleva 3 o mÃ¡s;300000.0;250000.0}.
 * Older lines without the last columns load without discount or warranty cost.
 * When loading, those ids are matched against the objects already loaded by the other
 * modules, so this repository only depends on the model layer.
 */
public class SaleRepository {

    private static final String SEPARATOR = ";";
    private static final String ID_SEPARATOR = ",";

    private final Path filePath;

    /**
     * Creates a repository that works with the given file.
     *
     * @param filePath path of the sales file
     */
    public SaleRepository(String filePath) {
        this.filePath = Paths.get(filePath);
    }

    /**
     * Writes all the sales to the file, replacing its previous content. The folder is
     * created if it does not exist yet.
     *
     * @param sales sales to save
     * @throws IOException if the file cannot be written
     */
    public void saveAll(List<Sale> sales) {
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            try (BufferedWriter writer =
                         Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                for (Sale sale : sales) {
                    writer.write(toLine(sale));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error de escritura en el archivo " + filePath, e);
        }
    }

    /**
     * Reads every stored sale and links it to its customer, seller and products.
     *
     * @param products  products already loaded
     * @param customers customers already loaded
     * @param sellers   sellers already loaded
     * @return stored sales, or an empty list if the file does not exist
     * @throws IOException if the file cannot be read
     */
    public List<Sale> loadAll(List<Product> products, List<Customer> customers, List<Seller> sellers) {
        List<Sale> sales = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return sales;
        }
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    sales.add(fromLine(line, products, customers, sellers));
                }
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error de lectura en el archivo " + filePath, e);
        }
        return sales;
    }

    private String toLine(Sale sale) {
        List<String> productIds = new ArrayList<>();
        for (Product product : sale.getProducts()) {
            productIds.add(product.getId());
        }
        String promotionName = sale.getAppliedPromotionName() == null
                ? "" : sale.getAppliedPromotionName().replace(SEPARATOR, ID_SEPARATOR);
        return String.join(SEPARATOR, sale.getId(), sale.getDate().toString(),
                sale.getCustomer().getId(), sale.getSeller().getId(),
                String.join(ID_SEPARATOR, productIds), promotionName,
                String.valueOf(sale.getDiscountAmount()), String.valueOf(sale.getExtendedWarrantyCost()));
    }

    private Sale fromLine(String line, List<Product> products, List<Customer> customers, List<Seller> sellers) {
        String[] parts = line.split(SEPARATOR, -1);
        Customer customer = findCustomer(customers, parts[2]);
        Seller seller = findSeller(sellers, parts[3]);
        List<Product> soldProducts = new ArrayList<>();
        for (String productId : parts[4].split(ID_SEPARATOR)) {
            soldProducts.add(findProduct(products, productId));
        }
        Sale sale = new Sale(parts[0], LocalDate.parse(parts[1]), customer, seller, soldProducts);
        if (parts.length > 6 && !parts[5].isBlank()) {
            sale.applyDiscount(parts[5], Double.parseDouble(parts[6]));
        }
        if (parts.length > 7 && !parts[7].isBlank()) {
            sale.addExtendedWarrantyCost(Double.parseDouble(parts[7]));
        }
        return sale;
    }

    private Customer findCustomer(List<Customer> customers, String id) {
        for (Customer customer : customers) {
            if (customer.getId().equals(id)) {
                return customer;
            }
        }
        throw invalidFormat();
    }

    private Seller findSeller(List<Seller> sellers, String id) {
        for (Seller seller : sellers) {
            if (seller.getId().equals(id)) {
                return seller;
            }
        }
        throw invalidFormat();
    }

    private Product findProduct(List<Product> products, String id) {
        for (Product product : products) {
            if (product.getId().equals(id)) {
                return product;
            }
        }
        throw invalidFormat();
    }

    /**
     * Builds the failure used when a stored sale points to a customer, a seller
     * or a product that no longer exists. The line cannot be interpreted, so the
     * file is reported as malformed rather than as a missing object.
     *
     * @return the exception to throw
     */
    private PersistenceException invalidFormat() {
        return new PersistenceException(
                "El archivo " + filePath + " tiene un formato invÃ¡lido", null);
    }
}
