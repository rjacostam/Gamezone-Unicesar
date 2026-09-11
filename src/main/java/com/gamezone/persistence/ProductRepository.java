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
 * File-based storage for products using CSV.
 */
public class ProductRepository {
    private String filePath;

    /**
     * Creates repository.
     * @param filePath csv path
     */
    public ProductRepository(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Loads all products.
     * @return product list
     */
    public List<Product> loadAll() {
        List<Product> out = new ArrayList<>();
        Path p = Path.of(filePath);
        if (!Files.exists(p)) {
            return out;
        }
        try {
            for (String line : Files.readAllLines(p)) {
                if (line.isBlank() || line.startsWith("type;")) {
                    continue;
                }
                String[] c = line.split(";", -1);
                if (c[0].equals("GAME") && c.length >= 8) {
                    out.add(new VideoGame(c[1], c[2], Double.parseDouble(c[3]),
                        Integer.parseInt(c[4]), c[5], c[6], c[7]));
                } else if (c[0].equals("CONSOLE") && c.length >= 8) {
                    out.add(new Console(c[1], c[2], Double.parseDouble(c[3]),
                        Integer.parseInt(c[4]), c[5], c[6], c[7]));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot load products", e);
        }
        return out;
    }

    /**
     * Saves all products.
     * @param products products to save
     */
    public void saveAll(List<Product> products) {
        StringBuilder sb = new StringBuilder("type;id;title;price;stock;f1;f2;f3\n");
        for (Product pr : products) {
            if (pr instanceof VideoGame g) {
                sb.append("GAME;").append(pr.getId()).append(";").append(pr.getTitle())
                  .append(";").append(pr.getPrice()).append(";").append(pr.getStock())
                  .append(";").append(g.getPlatform()).append(";").append(g.getGenre())
                  .append(";").append(g.getAgeRating()).append("\n");
            } else if (pr instanceof Console c) {
                sb.append("CONSOLE;").append(pr.getId()).append(";").append(pr.getTitle())
                  .append(";").append(pr.getPrice()).append(";").append(pr.getStock())
                  .append(";").append(c.getBrand()).append(";").append(c.getModel())
                  .append(";").append(c.getGeneration()).append("\n");
            }
        }
        try {
            Path p = Path.of(filePath);
            Files.createDirectories(p.getParent() == null ? Path.of(".") : p.getParent());
            Files.writeString(p, sb.toString());
        } catch (IOException e) {
            throw new RuntimeException("Cannot save products", e);
        }
    }
}
