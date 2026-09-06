package com.gamezone;

import com.gamezone.model.Seller;
import com.gamezone.persistence.PersonRepository;
import com.gamezone.persistence.ProductRepository;
import com.gamezone.persistence.SaleRepository;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.SaleService;
import com.gamezone.ui.ConsoleMenu;

/**
 * Arranque que conecta todas las capas.
 */
public class Main {
    /**
     * Arranca cargando datos y abriendo el menu.
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        ProductRepository productRepo = new ProductRepository("data/products.csv");
        PersonRepository personRepo = new PersonRepository("data/persons.csv");
        SaleRepository saleRepo = new SaleRepository("data/sales.csv");

        ProductService productService = new ProductService(productRepo);
        PersonService personService = new PersonService(personRepo);

        // Seed sellers if empty (store staff already hired)
        if (personService.findAllSellers().isEmpty()) {
            personService.ensureSeller(new Seller("Carlos Perez", "1065000001", "3001112222", "EMP-001", "MORNING"));
            personService.ensureSeller(new Seller("Laura Gomez", "1065000002", "3003334444", "EMP-002", "AFTERNOON"));
            personService.ensureSeller(new Seller("Diego Martinez", "1065000003", "3005556666", "EMP-003", "NIGHT"));
        }

        SaleService saleService = new SaleService(saleRepo, productService, personService);
        new ConsoleMenu(productService, personService, saleService).start();
    }
}
