package com.gamezone.persistence;

import com.gamezone.model.Customer;
import com.gamezone.model.Person;
import com.gamezone.model.Seller;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * File-based storage for persons.
 */
public class PersonRepository {
    private String filePath;

    /**
     * Creates repository.
     * @param filePath csv path
     */
    public PersonRepository(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Loads all persons.
     * @return person list
     */
    public List<Person> loadAll() {
        List<Person> out = new ArrayList<>();
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
                if (c[0].equals("CUSTOMER") && c.length >= 5) {
                    out.add(new Customer(c[1], c[2], c[3], c[4]));
                } else if (c[0].equals("SELLER") && c.length >= 6) {
                    out.add(new Seller(c[1], c[2], c[3], c[4], c[5]));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot load persons", e);
        }
        return out;
    }

    /**
     * Saves all persons.
     * @param persons persons to save
     */
    public void saveAll(List<Person> persons) {
        StringBuilder sb = new StringBuilder("type;name;id;phone;extra1;extra2\n");
        for (Person pe : persons) {
            if (pe instanceof Customer cu) {
                sb.append("CUSTOMER;").append(pe.getName()).append(";").append(pe.getNationalId())
                  .append(";").append(pe.getPhone()).append(";").append(cu.getEmail()).append(";\n");
            } else if (pe instanceof Seller se) {
                sb.append("SELLER;").append(pe.getName()).append(";").append(pe.getNationalId())
                  .append(";").append(pe.getPhone()).append(";").append(se.getEmployeeCode())
                  .append(";").append(se.getShift()).append("\n");
            }
        }
        try {
            Path p = Path.of(filePath);
            Files.createDirectories(p.getParent() == null ? Path.of(".") : p.getParent());
            Files.writeString(p, sb.toString());
        } catch (IOException e) {
            throw new RuntimeException("Cannot save persons", e);
        }
    }
}
