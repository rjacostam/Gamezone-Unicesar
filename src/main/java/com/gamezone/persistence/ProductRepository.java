package com.gamezone.persistence;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.model.ProductCatalogRow;
import com.gamezone.model.VideoGame;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes the store catalogue in a CSV file. Each line stores a type
 * marker in the first column so the concrete product class can be rebuilt when
 * the file is loaded, followed by the common attributes and the specific ones
 * of that type. This class is the only place in the system that knows how the
 * product data is stored on disk.
 *
 * <p>Column layout of the file, separated by semicolons:</p>
 * <pre>
 * type;id;title;price;stock;extra1;extra2;extra3
 * VIDEOGAME rows use platform, genre and age rating in the extra columns.
 * CONSOLE rows use brand, model and generation in the extra columns.
 * </pre>
 */
public class ProductRepository {
    private Path filePath;

    /**
     * Creates the repository for a given catalogue file.
     * @param filePath path of the CSV file that stores the products
     */
    public ProductRepository(String filePath) {
        this.filePath = Path.of(filePath);
    }

    /**
     * Writes the complete catalogue to the file, replacing its previous
     * content. Rows are written using the VIDEOGAME or CONSOLE marker in the
     * first column, which is the category the product itself publishes, and the
     * three extra columns are the ones each kind of product publishes. Missing
     * parent folders are created when needed.
     * @param products products to write
     * @throws PersistenceException when the file cannot be written
     */
    public void saveAll(List<Product> products) {
        StringBuilder content = new StringBuilder();
        content.append("type;id;title;price;stock;extra1;extra2;extra3\n");
        for (Product product : products) {
            content.append(product.getCategory()).append(";").append(product.getId())
                    .append(";").append(product.getTitle())
                    .append(";").append(product.getPrice())
                    .append(";").append(product.getStock());
            // Every product of this catalogue is a ProductCatalogRow, which is the
            // contract of products.csv only. Accessory and Promotion declare their
            // own contracts for their own files, so this repository can no longer
            // be pointed at them by accident.
            ProductCatalogRow row = (ProductCatalogRow) product;
            for (String extraField : row.getExtraFields()) {
                content.append(";").append(extraField);
            }
            content.append("\n");
        }
        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(filePath, content.toString());
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error de escritura en el archivo " + filePath, e);
        }
    }

    /**
     * Reads the complete catalogue from the file. The type marker of each line
     * decides which concrete class is instantiated. Lines with the header or
     * with an unknown marker are skipped, and an empty list is returned when
     * the file does not exist yet.
     * @return products stored in the file
     * @throws PersistenceException when the file cannot be read
     */
    public List<Product> loadAll() {
        List<Product> products = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return products;
        }
        try {
            for (String line : Files.readAllLines(filePath)) {
                if (line.isBlank() || line.startsWith("type;")) {
                    continue;
                }
                String[] fields = line.split(";", -1);
                if (fields.length < 8) {
                    continue;
                }
                String id = fields[1];
                String title = fields[2];
                double price;
                int stock;
                try {
                    price = Double.parseDouble(fields[3].trim());
                    stock = Integer.parseInt(fields[4].trim());
                } catch (NumberFormatException e) {
                    continue;
                }
                if ("VIDEOGAME".equalsIgnoreCase(fields[0].trim())) {
                    products.add(new VideoGame(id, title, price, stock,
                            fields[5], fields[6], fields[7]));
                } else if ("CONSOLE".equalsIgnoreCase(fields[0].trim())) {
                    products.add(new Console(id, title, price, stock,
                            fields[5], fields[6], fields[7]));
                }
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error de lectura en el archivo " + filePath, e);
        }
        return products;
    }
}

