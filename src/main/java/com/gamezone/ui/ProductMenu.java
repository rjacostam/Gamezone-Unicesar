package com.gamezone.ui;

import com.gamezone.model.Product;
import com.gamezone.service.ProductService;

import java.util.List;

/**
 * Submenu of the product catalogue: registers video games and consoles and lists the
 * inventory.
 */
public class ProductMenu extends Submenu {

    private final ProductService productService;

    /**
     * Creates the submenu.
     *
     * @param input          reader of the console
     * @param productService service for products
     */
    public ProductMenu(ConsoleInput input, ProductService productService) {
        super(input);
        this.productService = productService;
    }

    @Override
    public void show() {
        System.out.println();
        System.out.println("--- Gestión de productos ---");
        System.out.println("1. Registrar videojuego");
        System.out.println("2. Registrar consola");
        System.out.println("3. Listar inventario");
        System.out.println("0. Volver");
        int option = input.readInt("Seleccione una opción: ");
        runSafely(() -> {
            switch (option) {
                case 1 -> registerVideoGame();
                case 2 -> registerConsole();
                case 3 -> listProducts();
                case 0 -> { }
                default -> System.out.println("Opción no válida.");
            }
        });
    }

    private void registerVideoGame() {
        String id = input.readText("Identificador: ");
        String title = input.readText("Título: ");
        double price = input.readDouble("Precio: ");
        int stock = input.readInt("Cantidad en inventario: ");
        String platform = input.readText("Plataforma: ");
        String genre = input.readText("Género: ");
        String ageRating = input.readText("Clasificación de edad: ");
        Product game = productService.registerVideoGame(id, title, price, stock, platform, genre, ageRating);
        System.out.println("Videojuego registrado: " + game.getDescription());
    }

    private void registerConsole() {
        String id = input.readText("Identificador: ");
        String title = input.readText("Título: ");
        double price = input.readDouble("Precio: ");
        int stock = input.readInt("Cantidad en inventario: ");
        String brand = input.readText("Marca: ");
        String model = input.readText("Modelo: ");
        String generation = input.readText("Generación: ");
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
}
