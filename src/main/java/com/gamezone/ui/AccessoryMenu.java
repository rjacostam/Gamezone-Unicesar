package com.gamezone.ui;

import com.gamezone.model.Accessory;
import com.gamezone.model.Product;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.ProductService;

import java.util.ArrayList;
import java.util.List;

/**
 * Submenu of the accessories: registers controllers, cables and memories and lists them,
 * all together, by type or by compatible console.
 */
public class AccessoryMenu extends Submenu {

    private final AccessoryService accessoryService;
    private final ProductService productService;

    /**
     * Creates the submenu.
     *
     * @param input            reader of the console
     * @param accessoryService service for accessories
     * @param productService   service for products, used to show the consoles
     */
    public AccessoryMenu(ConsoleInput input, AccessoryService accessoryService, ProductService productService) {
        super(input);
        this.accessoryService = accessoryService;
        this.productService = productService;
    }

    @Override
    public void show() {
        System.out.println();
        System.out.println("--- Gestión de accesorios ---");
        System.out.println("1. Registrar control");
        System.out.println("2. Registrar cable");
        System.out.println("3. Registrar memoria");
        System.out.println("4. Listar todos los accesorios");
        System.out.println("5. Listar accesorios por tipo");
        System.out.println("6. Consultar accesorios compatibles con una consola");
        System.out.println("0. Volver");
        int option = input.readInt("Seleccione una opción: ");
        runSafely(() -> {
            switch (option) {
                case 1 -> registerController();
                case 2 -> registerCable();
                case 3 -> registerMemory();
                case 4 -> listAccessories(accessoryService.listAllAccessories());
                case 5 -> listAccessories(accessoryService.listAccessoriesByType(readAccessoryType()));
                case 6 -> listAccessories(accessoryService.findAccessoriesCompatibleWith(
                        input.readText("Identificador de la consola: ")));
                case 0 -> { }
                default -> System.out.println("Opción no válida.");
            }
        });
    }

    private void registerController() {
        String id = input.readText("Identificador (por ejemplo A-04): ");
        String title = input.readText("Título: ");
        double price = input.readDouble("Precio: ");
        int stock = input.readInt("Cantidad en inventario: ");
        List<String> consoles = readCompatibleConsoles();
        String connection = input.readText("Tipo de conexión (Inalámbrico/Alámbrico): ");
        Accessory controller = accessoryService.registerController(id, title, price, stock, consoles, connection);
        System.out.println("Control registrado: " + controller.getDescription());
    }

    private void registerCable() {
        String id = input.readText("Identificador (por ejemplo A-04): ");
        String title = input.readText("Título: ");
        double price = input.readDouble("Precio: ");
        int stock = input.readInt("Cantidad en inventario: ");
        List<String> consoles = readCompatibleConsoles();
        double length = input.readDouble("Longitud en metros: ");
        String connector = input.readText("Tipo de conector (HDMI, USB, óptico...): ");
        Accessory cable = accessoryService.registerCable(id, title, price, stock, consoles, length, connector);
        System.out.println("Cable registrado: " + cable.getDescription());
    }

    private void registerMemory() {
        String id = input.readText("Identificador (por ejemplo A-04): ");
        String title = input.readText("Título: ");
        double price = input.readDouble("Precio: ");
        int stock = input.readInt("Cantidad en inventario: ");
        List<String> consoles = readCompatibleConsoles();
        int capacity = input.readInt("Capacidad en GB: ");
        String type = input.readText("Tipo de memoria (SD, microSD, tarjeta interna): ");
        Accessory memory = accessoryService.registerMemory(id, title, price, stock, consoles, capacity, type);
        System.out.println("Memoria registrada: " + memory.getDescription());
    }

    private List<String> readCompatibleConsoles() {
        System.out.println("Consolas del inventario:");
        for (Product product : productService.listAllProducts()) {
            if (product.isConsole()) {
                System.out.println("  " + product.getId() + " - " + product.getTitle());
            }
        }
        String value = input.readOptionalText("Consolas compatibles separadas por coma (Enter si ninguna): ");
        List<String> ids = new ArrayList<>();
        for (String id : value.split(",")) {
            if (!id.isBlank()) {
                ids.add(id.trim());
            }
        }
        return ids;
    }

    private String readAccessoryType() {
        System.out.println("1. Controles  2. Cables  3. Memorias");
        return switch (input.readInt("Tipo: ")) {
            case 1 -> "CONTROLLER";
            case 2 -> "CABLE";
            case 3 -> "MEMORY";
            default -> "";
        };
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
}
