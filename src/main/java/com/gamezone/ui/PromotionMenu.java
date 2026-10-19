package com.gamezone.ui;

import com.gamezone.model.Promotion;
import com.gamezone.service.PromotionService;

import java.time.LocalDate;
import java.util.List;

/**
 * Submenu of the promotions: registers the three kinds of promotion and lists all of them
 * or only the active ones.
 */
public class PromotionMenu extends Submenu {

    private final PromotionService promotionService;

    /**
     * Creates the submenu.
     *
     * @param input            reader of the console
     * @param promotionService service for promotions
     */
    public PromotionMenu(ConsoleInput input, PromotionService promotionService) {
        super(input);
        this.promotionService = promotionService;
    }

    @Override
    public void show() {
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
}
