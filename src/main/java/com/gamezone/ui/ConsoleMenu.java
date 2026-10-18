package com.gamezone.ui;

import com.gamezone.model.Product;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.SaleService;

import java.util.List;
import java.util.Scanner;

/**
 * Console menu of the application.
 * <p>
 * It only reads the user input, calls the services and prints the results. It never uses
 * a repository. Every message shown to the user is in Spanish, and an error in one
 * operation is shown to the user without closing the application.
 */
public class ConsoleMenu {

    private final ProductService productService;
    private final PersonService personService;
    private final SaleService saleService;
    private final Scanner scanner;

    /**
     * Creates the menu with the services it works with.
     *
     * @param productService service for products
     * @param personService  service for customers and sellers
     * @param saleService    service for sales
     */
    public ConsoleMenu(ProductService productService, PersonService personService, SaleService saleService) {
        this.productService = productService;
        this.personService = personService;
        this.saleService = saleService;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Shows the main menu until the user chooses to exit.
     */
    public void start() {
        int option;
        do {
            System.out.println();
            System.out.println("===== GAMEZONE UNICESAR =====");
            System.out.println("1. Gestión de productos");
            System.out.println("0. Salir");
            option = readInt("Seleccione una opción: ");
            switch (option) {
                case 1 -> productMenu();
                case 0 -> System.out.println("Hasta pronto.");
                default -> System.out.println("Opción no válida.");
            }
        } while (option != 0);
    }

    // ---------------------------------------------------------------- products

    private void productMenu() {
        System.out.println();
        System.out.println("--- Gestión de productos ---");
        System.out.println("1. Registrar videojuego");
        System.out.println("2. Registrar consola");
        System.out.println("3. Listar inventario");
        System.out.println("0. Volver");
        int option = readInt("Seleccione una opción: ");
        try {
            switch (option) {
                case 1 -> registerVideoGame();
                case 2 -> registerConsole();
                case 3 -> listProducts();
                case 0 -> { }
                default -> System.out.println("Opción no válida.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void registerVideoGame() {
        String id = readText("Identificador: ");
        String title = readText("Título: ");
        double price = readDouble("Precio: ");
        int stock = readInt("Cantidad en inventario: ");
        String platform = readText("Plataforma: ");
        String genre = readText("Género: ");
        String ageRating = readText("Clasificación de edad: ");
        Product game = productService.registerVideoGame(id, title, price, stock, platform, genre, ageRating);
        System.out.println("Videojuego registrado: " + game.getDescription());
    }

    private void registerConsole() {
        String id = readText("Identificador: ");
        String title = readText("Título: ");
        double price = readDouble("Precio: ");
        int stock = readInt("Cantidad en inventario: ");
        String brand = readText("Marca: ");
        String model = readText("Modelo: ");
        String generation = readText("Generación: ");
        Product console = productService.registerConsole(id, title, price, stock, brand, model, generation);
        System.out.println("Consola registrada: " + console.getDescription());
    }

    private void listProducts() {
        List<Product> products = productService.listAllProducts();
        if (products.isEmpty()) {
            System.out.println("No hay productos registrados.");
            return;
        }
        for (Product product : products) {
            System.out.println(product.getDescription());
        }
    }

    // ---------------------------------------------------------------- input helpers

    private String readText(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (value == null) {
                return "";
            }
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Este campo es obligatorio.");
        }
    }

    private String readOptionalText(String prompt) {
        String value = readLine(prompt);
        return value == null ? "" : value;
    }

    private int readInt(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (value == null) {
                return 0;
            }
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("Debe ingresar un número entero.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            String value = readText(prompt);
            if (value.isEmpty()) {
                return 0;
            }
            try {
                return Double.parseDouble(value.replace(",", "."));
            } catch (NumberFormatException e) {
                System.out.println("Debe ingresar un número válido.");
            }
        }
    }

    /**
     * Shows the prompt and reads one line.
     *
     * @param prompt text shown before reading
     * @return the trimmed line, or {@code null} when there is no more input
     */
    private String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            return null;
        }
        return scanner.nextLine().trim();
    }
}
