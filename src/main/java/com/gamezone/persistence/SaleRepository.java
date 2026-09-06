package com.gamezone.persistence;

import com.gamezone.model.Customer;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * File-based storage for sales. Resolves persons/products via callbacks.
 */
public class SaleRepository {
    private String filePath;

    /**
     * Crea el repositorio con su ruta.
     * @param filePath csv path
     */
    public SaleRepository(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Lookup for customer by id.
     */
    public interface CustomerLookup {
        /**
         * Finds customer.
         * @param nationalId id
         * @return customer or null
         */
        Customer find(String nationalId);
    }

    /**
     * Lookup for seller.
     */
    public interface SellerLookup {
        /**
         * Finds seller.
         * @param employeeCode code
         * @return seller or null
         */
        Seller find(String employeeCode);
    }

    /**
     * Lookup for product.
     */
    public interface ProductLookup {
        /**
         * Finds product.
         * @param id product id
         * @return product or null
         */
        Product find(String id);
    }

    /**
     * Loads sales resolving references.
     * @param customers customer lookup
     * @param sellers seller lookup
     * @param products product lookup
     * @return sale list
     */
    public List<Sale> loadAll(CustomerLookup customers, SellerLookup sellers, ProductLookup products) {
        List<Sale> out = new ArrayList<>();
        Path p = Path.of(filePath);
        if (!Files.exists(p)) {
            return out;
        }
        try {
            for (String line : Files.readAllLines(p)) {
                if (line.isBlank() || line.startsWith("id;")) {
                    continue;
                }
                String[] c = line.split(";", -1);
                if (c.length < 5) {
                    continue;
                }
                Customer cu = customers.find(c[2]);
                Seller se = sellers.find(c[3]);
                if (cu == null || se == null) {
                    continue;
                }
                List<Product> items = new ArrayList<>();
                for (String pid : c[4].split(",")) {
                    Product pr = products.find(pid.trim());
                    if (pr != null) {
                        items.add(pr);
                    }
                }
                if (!items.isEmpty()) {
                    out.add(new Sale(c[0], c[1], cu, se, items));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot load sales", e);
        }
        return out;
    }

    /**
     * Saves sales as ids.
     * @param sales sales to save
     */
    public void saveAll(List<Sale> sales) {
        StringBuilder sb = new StringBuilder("id;date;customerId;sellerCode;productIds\n");
        for (Sale s : sales) {
            List<String> ids = new ArrayList<>();
            for (Product pr : s.getProducts()) {
                ids.add(pr.getId());
            }
            sb.append(s.getId()).append(";").append(s.getDate()).append(";")
              .append(s.getCustomer().getNationalId()).append(";")
              .append(s.getSeller().getEmployeeCode()).append(";")
              .append(String.join(",", ids)).append("\n");
        }
        try {
            Path p = Path.of(filePath);
            Files.createDirectories(p.getParent() == null ? Path.of(".") : p.getParent());
            Files.writeString(p, sb.toString());
        } catch (IOException e) {
            throw new RuntimeException("Cannot save sales", e);
        }
    }
}
