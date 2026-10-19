package com.gamezone.ui;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.PersistenceException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Accessory;
import com.gamezone.model.Console;
import com.gamezone.model.Customer;
import com.gamezone.model.Product;
import com.gamezone.model.Promotion;
import com.gamezone.model.Return;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.model.Warranty;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.PromotionService;
import com.gamezone.service.ReturnService;
import com.gamezone.service.SaleService;
import com.gamezone.service.WarrantyService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Console menu of the application.
 * <p>
 * It only reads the user input, calls the services and prints the results. It never uses
 * a repository. Every message shown to the user is in Spanish. Each kind of error gets its
 * own message, and an error in one operation never closes the application or shows a
 * stack trace.
 */
public class ConsoleMenu {

    private final ProductService productService;
    private final PersonService personService;
    private final SaleService saleService;
    private final AccessoryService accessoryService;
    private final PromotionService promotionService;
    private final WarrantyService warrantyService;
    private final ReturnService returnService;
    private final ConsoleInput input;
    private final ProductMenu productMenu;

    /**
     * Creates the menu with the services it works with.
     *
     * @param productService service for products
     * @param personService  service for customers and sellers
     * @param saleService      service for sales
     * @param accessoryService service for accessories
     * @param promotionService service for promotions
     * @param warrantyService  service for warranties
     * @param returnService    service for returns and the monthly balance
     */
    public ConsoleMenu(ProductService productService, PersonService personService, SaleService saleService,
                       AccessoryService accessoryService, PromotionService promotionService,
                       WarrantyService warrantyService, ReturnService returnService) {
        this.productService = productService;
        this.personService = personService;
        this.saleService = saleService;
        this.accessoryService = accessoryService;
        this.promotionService = promotionService;
        this.warrantyService = warrantyService;
        this.returnService = returnService;
        this.input = new ConsoleInput();
        this.productMenu = new ProductMenu(input, productService);
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
            System.out.println("5. Gestión de promociones");
            System.out.println("6. Gestión de garantías");
            System.out.println("7. Gestión de devoluciones");
            System.out.println("0. Salir");
            option = input.readInt("Seleccione una opción: ");
            switch (option) {
                case 1 -> productMenu.show();
                case 2 -> personMenu();
                case 3 -> saleMenu();
                case 4 -> accessoryMenu();
                case 5 -> promotionMenu();
                case 6 -> warrantyMenu();
                case 7 -> returnMenu();
                case 0 -> System.out.println("Hasta pronto.");
                default -> System.out.println("Opción no válida.");
            }
        } while (option != 0);
    }

    /**
     * Runs one menu operation and turns any error of the system into a clear message for the
     * user, with a different text for each kind of error.
     *
     * @param action operation chosen by the user
     */
    private void runSafely(Runnable action) {
        try {
            action.run();
        } catch (ResourceNotFoundException e) {
            System.out.println("No se encontró el recurso solicitado: " + e.getMessage());
        } catch (BusinessRuleException e) {
            System.out.println("No se puede completar la operación: " + e.getMessage());
        } catch (InvalidDataException e) {
            System.out.println("Los datos ingresados no son válidos: " + e.getMessage());
        } catch (PersistenceException e) {
            System.out.println("Ocurrió un problema al acceder a los datos del sistema. Contacte al administrador.");
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
        System.out.println("5. Ver detalle de una venta");
        System.out.println("0. Volver");
        int option = input.readInt("Seleccione una opción: ");
        runSafely(() -> {
            switch (option) {
                case 1 -> registerSale();
                case 2 -> printSales(saleService.listAllSales());
                case 3 -> printSales(saleService.findSalesByCustomer(input.readText("Identificación del cliente: ")));
                case 4 -> printSales(saleService.findSalesBySeller(input.readText("Identificación o código del vendedor: ")));
                case 5 -> showSaleDetail();
                case 0 -> { }
                default -> System.out.println("Opción no válida.");
            }
        });
    }

    private void registerSale() {
        String customerId = input.readText("Identificación del cliente: ");
        listSellers();
        String sellerId = input.readText("Identificación o código del vendedor: ");
        listCatalog();
        List<String> productIds = new ArrayList<>();
        while (true) {
            String productId = input.readOptionalText("Identificador del producto o accesorio (Enter para terminar): ");
            if (productId.isEmpty()) {
                break;
            }
            int quantity = input.readInt("Cantidad: ");
            for (int i = 0; i < quantity; i++) {
                productIds.add(productId);
            }
        }
        List<String> extendedWarranty = new ArrayList<>();
        for (String productId : new LinkedHashSet<>(productIds)) {
            Product product = productService.findById(productId);
            if (product != null && product.isConsole()
                    && input.readYesNo("¿Desea agregar garantía extendida a " + product.getTitle() + "? (s/n): ")) {
                extendedWarranty.add(product.getId());
            }
        }
        Sale sale = saleService.registerSale(customerId, sellerId, productIds, extendedWarranty);
        System.out.println("Venta registrada con éxito.");
        System.out.println(ReceiptGenerator.generate(sale));
    }

    private void listCatalog() {
        List<Product> catalog = saleService.getCatalog();
        if (catalog.isEmpty()) {
            System.out.println("No hay productos ni accesorios registrados.");
            return;
        }
        for (Product item : catalog) {
            System.out.println(item.getDescription());
        }
    }

    private void showSaleDetail() {
        String saleId = input.readText("Identificador de la venta (por ejemplo S0001): ");
        Sale sale = saleService.findById(saleId);
        if (sale == null) {
            System.out.println("No existe una venta con el identificador " + saleId + ".");
            return;
        }
        System.out.println(ReceiptGenerator.generate(sale));
    }

    private void printSales(List<Sale> sales) {
        if (sales.isEmpty()) {
            System.out.println("No hay ventas para mostrar.");
            return;
        }
        for (Sale sale : sales) {
            System.out.println(ReceiptGenerator.generate(sale));
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
            if (product instanceof Console) {
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

    // ---------------------------------------------------------------- promotions

    private void promotionMenu() {
        System.out.println();
        System.out.println("--- Gestión de promociones ---");
        System.out.println("1. Registrar promoción por porcentaje");
        System.out.println("2. Registrar promoción por categoría");
        System.out.println("3. Registrar promoción por volumen");
        System.out.println("4. Listar todas las promociones");
        System.out.println("5. Listar promociones vigentes");
        System.out.println("0. Volver");
        int option = input.readInt("Seleccione una opción: ");
        runSafely(() -> {
            switch (option) {
                case 1 -> registerPercentagePromotion();
                case 2 -> registerCategoryPromotion();
                case 3 -> registerBulkPromotion();
                case 4 -> listPromotions(promotionService.listAllPromotions());
                case 5 -> listPromotions(promotionService.listActivePromotions());
                case 0 -> { }
                default -> System.out.println("Opción no válida.");
            }
        });
    }

    private void registerPercentagePromotion() {
        String id = input.readText("Identificador (por ejemplo P-04): ");
        String name = input.readText("Nombre: ");
        LocalDate start = input.readDate("Fecha de inicio (AAAA-MM-DD): ");
        LocalDate end = input.readDate("Fecha de fin (AAAA-MM-DD): ");
        double percentage = input.readDouble("Porcentaje de descuento: ");
        Promotion promotion = promotionService.registerPercentageDiscount(id, name, start, end, percentage);
        System.out.println("Promoción registrada: " + promotion.getDescription());
    }

    private void registerCategoryPromotion() {
        String id = input.readText("Identificador (por ejemplo P-04): ");
        String name = input.readText("Nombre: ");
        LocalDate start = input.readDate("Fecha de inicio (AAAA-MM-DD): ");
        LocalDate end = input.readDate("Fecha de fin (AAAA-MM-DD): ");
        double percentage = input.readDouble("Porcentaje de descuento: ");
        System.out.println("1. Videojuegos  2. Consolas  3. Accesorios");
        String category = switch (input.readInt("Categoría: ")) {
            case 1 -> "VIDEOGAME";
            case 2 -> "CONSOLE";
            case 3 -> "ACCESSORY";
            default -> "";
        };
        Promotion promotion = promotionService.registerCategoryDiscount(id, name, start, end, percentage, category);
        System.out.println("Promoción registrada: " + promotion.getDescription());
    }

    private void registerBulkPromotion() {
        String id = input.readText("Identificador (por ejemplo P-04): ");
        String name = input.readText("Nombre: ");
        LocalDate start = input.readDate("Fecha de inicio (AAAA-MM-DD): ");
        LocalDate end = input.readDate("Fecha de fin (AAAA-MM-DD): ");
        int minimum = input.readInt("Cantidad mínima de productos: ");
        double percentage = input.readDouble("Porcentaje de descuento: ");
        Promotion promotion = promotionService.registerBulkPurchaseDiscount(id, name, start, end, minimum, percentage);
        System.out.println("Promoción registrada: " + promotion.getDescription());
    }

    private void listPromotions(List<Promotion> promotions) {
        if (promotions.isEmpty()) {
            System.out.println("No hay promociones para mostrar.");
            return;
        }
        for (Promotion promotion : promotions) {
            System.out.println(promotion.getDescription());
        }
    }

    // ---------------------------------------------------------------- warranties

    private void warrantyMenu() {
        System.out.println();
        System.out.println("--- Gestión de garantías ---");
        System.out.println("1. Consultar la garantía de un producto en una venta");
        System.out.println("2. Listar todas las garantías");
        System.out.println("3. Listar garantías vigentes");
        System.out.println("4. Listar garantías próximas a vencer");
        System.out.println("0. Volver");
        int option = input.readInt("Seleccione una opción: ");
        runSafely(() -> {
            switch (option) {
                case 1 -> showWarranty();
                case 2 -> listWarranties(warrantyService.listAllWarranties());
                case 3 -> listWarranties(warrantyService.listActiveWarranties());
                case 4 -> listWarranties(warrantyService.listWarrantiesExpiringSoon(
                        input.readInt("¿Cuántos días de anticipación? ")));
                case 0 -> { }
                default -> System.out.println("Opción no válida.");
            }
        });
    }

    private void showWarranty() {
        String saleId = input.readText("Identificador de la venta (por ejemplo S0001): ");
        String productId = input.readText("Identificador del producto (por ejemplo C-01): ");
        Warranty warranty = warrantyService.findWarrantyByProduct(productId, saleId);
        if (warranty == null) {
            System.out.println("El producto " + productId + " no tiene garantía en la venta " + saleId + ".");
            return;
        }
        System.out.println(WarrantyCertificateFormatter.format(warranty));
    }

    private void listWarranties(List<Warranty> warranties) {
        if (warranties.isEmpty()) {
            System.out.println("No hay garantías para mostrar.");
            return;
        }
        for (Warranty warranty : warranties) {
            System.out.println(WarrantyCertificateFormatter.format(warranty));
        }
    }

    // ---------------------------------------------------------------- returns

    private void returnMenu() {
        System.out.println();
        System.out.println("--- Gestión de devoluciones ---");
        System.out.println("1. Registrar devolución");
        System.out.println("2. Consultar todas las devoluciones");
        System.out.println("3. Consultar devoluciones por cliente");
        System.out.println("4. Consultar devoluciones por venta");
        System.out.println("5. Consultar balance mensual");
        System.out.println("0. Volver");
        int option = input.readInt("Seleccione una opción: ");
        runSafely(() -> {
            switch (option) {
                case 1 -> registerReturn();
                case 2 -> listReturns(returnService.viewAllReturns());
                case 3 -> listReturns(returnService.viewReturnsByCustomer(input.readText("Identificación del cliente: ")));
                case 4 -> listReturns(returnService.viewReturnsBySale(
                        input.readText("Identificador de la venta (por ejemplo S0001): ")));
                case 5 -> showMonthlyBalance();
                case 0 -> { }
                default -> System.out.println("Opción no válida.");
            }
        });
    }

    private void registerReturn() {
        String saleId = input.readText("Identificador de la venta (por ejemplo S0001): ");
        Sale sale = saleService.findById(saleId);
        if (sale == null) {
            System.out.println("No existe una venta con el identificador " + saleId + ".");
            return;
        }
        System.out.println(ReceiptGenerator.generate(sale));
        if (!sale.canBeReturned()) {
            System.out.println("Aviso: esta venta tiene más de 30 días, no se puede devolver.");
            return;
        }
        List<String> productIds = new ArrayList<>();
        while (true) {
            String productId = input.readOptionalText("Identificador del producto a devolver (Enter para terminar): ");
            if (productId.isEmpty()) {
                break;
            }
            int quantity = input.readInt("Cantidad: ");
            for (int i = 0; i < quantity; i++) {
                productIds.add(productId);
            }
        }
        String reason = input.readText("Motivo de la devolución: ");
        Return productReturn = returnService.registerReturn(sale.getId(), productIds, reason);
        System.out.println("Devolución registrada con éxito.");
        System.out.println(ReturnReceiptFormatter.format(productReturn));
    }

    private void listReturns(List<Return> returns) {
        if (returns.isEmpty()) {
            System.out.println("No hay devoluciones para mostrar.");
            return;
        }
        for (Return productReturn : returns) {
            System.out.println(ReturnReceiptFormatter.format(productReturn));
        }
    }

    private void showMonthlyBalance() {
        int month = input.readInt("Mes (1-12): ");
        int year = input.readInt("Año (por ejemplo 2026): ");
        if (month < 1 || month > 12) {
            System.out.println("El mes debe estar entre 1 y 12.");
            return;
        }
        double sales = returnService.calculateMonthlySales(month, year);
        double returns = returnService.calculateMonthlyReturns(month, year);
        double balance = returnService.generateMonthlyBalance(month, year);
        System.out.printf("Balance de %02d/%d%n", month, year);
        System.out.printf("  Total de ventas: $%,.2f%n", sales);
        System.out.printf("  Total de devoluciones: $%,.2f%n", returns);
        System.out.printf("  Balance neto: $%,.2f%n", balance);
    }
}
