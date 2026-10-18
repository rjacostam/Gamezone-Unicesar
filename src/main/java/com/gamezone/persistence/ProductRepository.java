package com.gamezone.persistence;

import com.gamezone.model.Console;
import com.gamezone.model.Product;
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
     * first column. Missing parent folders are created when needed.
     * @param products products to write
     * @throws IOException when the file cannot be written
     */
    public void saveAll(List<Product> products) throws IOException {
        StringBuilder content = new StringBuilder();
        content.append("type;id;title;price;stock;extra1;extra2;extra3\n");
        for (Product product : products) {
            if (product instanceof VideoGame) {
                VideoGame game = (VideoGame) product;
                content.append("VIDEOGAME").append(";").append(game.getId())
                        .append(";").append(game.getTitle())
                        .append(";").append(game.getPrice())
                        .append(";").append(game.getStock())
                        .append(";").append(game.getPlatform())
                        .append(";").append(game.getGenre())
                        .append(";").append(game.getAgeRating())
                        .append("\n");
            } else if (product instanceof Console) {
                Console console = (Console) product;
                content.append("CONSOLE").append(";").append(console.getId())
                        .append(";").append(console.getTitle())
                        .append(";").append(console.getPrice())
                        .append(";").append(console.getStock())
                        .append(";").append(console.getBrand())
                        .append(";").append(console.getModel())
                        .append(";").append(console.getGeneration())
                        .append("\n");
            }
        }
        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(filePath, content.toString());
    }

    /**
     * Reads the complete catalogue from the file. The type marker of each line
     * decides which concrete class is instantiated. Lines with the header or
     * with an unknown marker are skipped, and an empty list is returned when
     * the file does not exist yet.
     * @return products stored in the file
     * @throws IOException when the file cannot be read
     */
    public List<Product> loadAll() throws IOException {
        List<Product> products = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return products;
        }
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
        return products;
    }
}
