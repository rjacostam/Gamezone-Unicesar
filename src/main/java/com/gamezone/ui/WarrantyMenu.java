package com.gamezone.ui;

import com.gamezone.model.Warranty;
import com.gamezone.service.WarrantyService;

import java.util.List;

/**
 * Submenu of the warranties: shows the warranty of a product in a sale and lists all,
 * active and soon to expire warranties.
 */
public class WarrantyMenu extends Submenu {

    private final WarrantyService warrantyService;

    /**
     * Creates the submenu.
     *
     * @param input           reader of the console
     * @param warrantyService service for warranties
     */
    public WarrantyMenu(ConsoleInput input, WarrantyService warrantyService) {
        super(input);
        this.warrantyService = warrantyService;
    }

    @Override
    public void show() {
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
}
