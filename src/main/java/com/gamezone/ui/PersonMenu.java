package com.gamezone.ui;

import com.gamezone.model.Customer;
import com.gamezone.model.Seller;
import com.gamezone.service.PersonService;

import java.util.List;

/**
 * Submenu of the people of the store: registers customers and lists customers and sellers.
 */
public class PersonMenu extends Submenu {

    private final PersonService personService;

    /**
     * Creates the submenu.
     *
     * @param input         reader of the console
     * @param personService service for customers and sellers
     */
    public PersonMenu(ConsoleInput input, PersonService personService) {
        super(input);
        this.personService = personService;
    }

    @Override
    public void show() {
        System.out.println();
        System.out.println("--- Gestión de personas ---");
        System.out.println("1. Registrar cliente");
        System.out.println("2. Listar clientes");
        System.out.println("3. Listar vendedores");
        System.out.println("0. Volver");
        int option = input.readInt("Seleccione una opción: ");
        runSafely(() -> {
            switch (option) {
                case 1 -> registerCustomer();
                case 2 -> listCustomers();
                case 3 -> listSellers();
                case 0 -> { }
                default -> System.out.println("Opción no válida.");
            }
        });
    }

    private void registerCustomer() {
        String id = input.readText("Identificación: ");
        String name = input.readText("Nombre: ");
        String phone = input.readText("Teléfono: ");
        String email = input.readText("Correo electrónico: ");
        Customer customer = personService.registerCustomer(id, name, phone, email);
        System.out.println("Cliente registrado: " + customer.getSummary());
    }

    private void listCustomers() {
        List<Customer> customers = personService.listCustomers();
        if (customers.isEmpty()) {
            System.out.println("No hay clientes registrados.");
            return;
        }
        for (Customer customer : customers) {
            System.out.println(customer.getSummary());
        }
    }

    /**
     * Prints the sellers of the store. The sale submenu also uses it, so the seller can be
     * chosen from the list.
     */
    void listSellers() {
        List<Seller> sellers = personService.listSellers();
        if (sellers.isEmpty()) {
            System.out.println("No hay vendedores registrados.");
            return;
        }
        for (Seller seller : sellers) {
            System.out.println(seller.getSummary());
        }
    }
}
