package com.gamezone.ui;

import com.gamezone.model.Return;
import com.gamezone.model.Sale;
import com.gamezone.service.MonthlyBalanceService;
import com.gamezone.service.ReturnService;
import com.gamezone.service.SaleService;

import java.util.ArrayList;
import java.util.List;

/**
 * Submenu of the returns: registers a return, lists the returns and shows the monthly
 * balance of sales and returns.
 */
public class ReturnMenu extends Submenu {

    private final ReturnService returnService;
    private final MonthlyBalanceService balanceService;
    private final SaleService saleService;

    /**
     * Creates the submenu.
     *
     * @param input         reader of the console
     * @param returnService  service for returns
     * @param balanceService service for the monthly balance of sales and returns
     * @param saleService   service for sales, used to show the original sale
     */
    public ReturnMenu(ConsoleInput input, ReturnService returnService, MonthlyBalanceService balanceService,
                      SaleService saleService) {
        super(input);
        this.returnService = returnService;
        this.balanceService = balanceService;
        this.saleService = saleService;
    }

    @Override
    public void show() {
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
        double sales = balanceService.calculateMonthlySales(month, year);
        double returns = balanceService.calculateMonthlyReturns(month, year);
        double balance = balanceService.generateMonthlyBalance(month, year);
        System.out.printf("Balance de %02d/%d%n", month, year);
        System.out.printf("  Total de ventas: $%,.2f%n", sales);
        System.out.printf("  Total de devoluciones: $%,.2f%n", returns);
        System.out.printf("  Balance neto: $%,.2f%n", balance);
    }
}
