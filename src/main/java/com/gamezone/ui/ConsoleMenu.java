package com.gamezone.ui;

import com.gamezone.model.Accessory;
import com.gamezone.model.Console;
import com.gamezone.model.Customer;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.SaleService;

import java.io.IOException;
import java.util.ArrayList;
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
    private final AccessoryService accessoryService;
    private final Scanner scanner;

    /**
     * Creates the menu with the services it works with.
     *
     * @param productService service for products
     * @param personService  service for customers and sellers
     * @param saleService      service for sales
     * @param accessoryService service for accessories
     */
    public ConsoleMenu(ProductService productService, PersonService personService, SaleService saleService,
                       AccessoryService accessoryService) {
        this.productService = productService;
        this.personService = personService;
        this.saleService = saleService;
        this.accessoryService = accessoryService;
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
            System.out.println("2. Gestión de personas");
            System.out.println("3. Gestión de ventas");
            System.out.println("4. Gestión de accesorios");
            System.out.println("0. Salir");
            option = readInt("Seleccione una opción: ");
            switch (option) {
                case 1 -> productMenu();
                case 2 -> personMenu();
                case 3 -> saleMenu();
                case 4 -> accessoryMenu();
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

    // ---------------------------------------------------------------- people

    private void personMenu() {
        System.out.println();
        System.out.println("--- Gestión de personas ---");
        System.out.println("1. Registrar cliente");
        System.out.println("2. Listar clientes");
        System.out.println("3. Listar vendedores");
        System.out.println("0. Volver");
        int option = readInt("Seleccione una opción: ");
        try {
            switch (option) {
                case 1 -> registerCustomer();
                case 2 -> listCustomers();
                case 3 -> listSellers();
                case 0 -> { }
                default -> System.out.println("Opción no válida.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error: no se pudo guardar el cliente en el archivo de datos.");
        }
    }

    private void registerCustomer() throws IOException {
        String id = readText("Identificación: ");
        String name = readText("Nombre: ");
        String phone = readText("Teléfono: ");
        String email = readText("Correo electrónico: ");
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

    private void listSellers() {
        List<Seller> sellers = personService.listSellers();
        if (sellers.isEmpty()) {
            System.out.println("No hay vendedores registrados.");
            return;
        }
        for (Seller seller : sellers) {
            System.out.println(seller.getSummary());
        }
    }

    // ---------------------------------------------------------------- sales

    private void saleMenu() {
        System.out.println();
        System.out.println("--- Gestión de ventas ---");
        System.out.println("1. Registrar venta");
        System.out.println("2. Historial de ventas");
        System.out.println("3. Historial de compras de un cliente");
        System.out.println("4. Ventas atendidas por un vendedor");
        System.out.println("0. Volver");
        int option = readInt("Seleccione una opción: ");
        try {
            switch (option) {
                case 1 -> registerSale();
                case 2 -> printSales(saleService.listAllSales());
                case 3 -> printSales(saleService.findSalesByCustomer(readText("Identificación del cliente: ")));
                case 4 -> printSales(saleService.findSalesBySeller(readText("Identificación o código del vendedor: ")));
                case 0 -> { }
                default -> System.out.println("Opción no válida.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void registerSale() {
        String customerId = readText("Identificación del cliente: ");
        listSellers();
        String sellerId = readText("Identificación o código del vendedor: ");
        listProducts();
        List<String> productIds = new ArrayList<>();
        while (true) {
            String productId = readOptionalText("Identificador del producto (Enter para terminar): ");
            if (productId.isEmpty()) {
                break;
            }
            int quantity = readInt("Cantidad: ");
            for (int i = 0; i < quantity; i++) {
                productIds.add(productId);
            }
        }
        Sale sale = saleService.registerSale(customerId, sellerId, productIds);
        System.out.println("Venta registrada con éxito.");
        System.out.println(sale.generateReceipt());
    }

    private void printSales(List<Sale> sales) {
        if (sales.isEmpty()) {
            System.out.println("No hay ventas para mostrar.");
            return;
        }
        for (Sale sale : sales) {
            System.out.println(sale.generateReceipt());
            System.out.println();
        }
    }

    // ---------------------------------------------------------------- accessories

    private void accessoryMenu() {
        System.out.println();
        System.out.println("--- Gestión de accesorios ---");
        System.out.println("1. Registrar control");
        System.out.println("2. Registrar cable");
        System.out.println("3. Registrar memoria");
        System.out.println("4. Listar todos los accesorios");
        System.out.println("0. Volver");
        int option = readInt("Seleccione una opción: ");
        try {
            switch (option) {
                case 1 -> registerController();
                case 2 -> registerCable();
                case 3 -> registerMemory();
                case 4 -> listAccessories(accessoryService.listAllAccessories());
                case 0 -> { }
                default -> System.out.println("Opción no válida.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void registerController() {
        String id = readText("Identificador (por ejemplo A-04): ");
        String title = readText("Título: ");
        double price = readDouble("Precio: ");
        int stock = readInt("Cantidad en inventario: ");
        List<String> consoles = readCompatibleConsoles();
        String connection = readText("Tipo de conexión (Inalámbrico/Alámbrico): ");
        Accessory controller = accessoryService.registerController(id, title, price, stock, consoles, connection);
        System.out.println("Control registrado: " + controller.getDescription());
    }

    private void registerCable() {
        String id = readText("Identificador (por ejemplo A-04): ");
        String title = readText("Título: ");
        double price = readDouble("Precio: ");
        int stock = readInt("Cantidad en inventario: ");
        List<String> consoles = readCompatibleConsoles();
        double length = readDouble("Longitud en metros: ");
        String connector = readText("Tipo de conector (HDMI, USB, óptico...): ");
        Accessory cable = accessoryService.registerCable(id, title, price, stock, consoles, length, connector);
        System.out.println("Cable registrado: " + cable.getDescription());
    }

    private void registerMemory() {
        String id = readText("Identificador (por ejemplo A-04): ");
        String title = readText("Título: ");
        double price = readDouble("Precio: ");
        int stock = readInt("Cantidad en inventario: ");
        List<String> consoles = readCompatibleConsoles();
        int capacity = readInt("Capacidad en GB: ");
        String type = readText("Tipo de memoria (SD, microSD, tarjeta interna): ");
        Accessory memory = accessoryService.registerMemory(id, title, price, stock, consoles, capacity, type);
        System.out.println("Memoria registrada: " + memory.getDescription());
    }

    private List<String> readCompatibleConsoles() {
        System.out.println("Consolas del inventario:");
        for (Product product : productService.listAllProducts()) {
            if (product instanceof Console) {
                System.out.println("  " + product.getId() + " - " + product.getTitle());
            }
        }
        String value = readOptionalText("Consolas compatibles separadas por coma (Enter si ninguna): ");
        List<String> ids = new ArrayList<>();
        for (String id : value.split(",")) {
            if (!id.isBlank()) {
                ids.add(id.trim());
            }
        }
        return ids;
    }

    private void listAccessories(List<Accessory> accessories) {
        if (accessories.isEmpty()) {
            System.out.println("No hay accesorios para mostrar.");
            return;
        }
        for (Accessory accessory : accessories) {
            System.out.println(accessory.getDescription());
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
