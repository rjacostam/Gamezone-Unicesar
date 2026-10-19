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
    private final WarrantyMenu warrantyMenu;
    private final PromotionMenu promotionMenu;
    private final AccessoryMenu accessoryMenu;
    private final SaleMenu saleMenu;
    private final PersonMenu personMenu;
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
        this.personMenu = new PersonMenu(input, personService);
        this.saleMenu = new SaleMenu(input, saleService, productService, personMenu);
        this.accessoryMenu = new AccessoryMenu(input, accessoryService, productService);
        this.promotionMenu = new PromotionMenu(input, promotionService);
        this.warrantyMenu = new WarrantyMenu(input, warrantyService);
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
                case 2 -> personMenu.show();
                case 3 -> saleMenu.show();
                case 4 -> accessoryMenu.show();
                case 5 -> promotionMenu.show();
                case 6 -> warrantyMenu.show();
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
