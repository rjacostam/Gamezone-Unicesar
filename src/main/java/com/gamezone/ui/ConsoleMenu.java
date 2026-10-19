package com.gamezone.ui;

import com.gamezone.service.AccessoryService;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.PromotionService;
import com.gamezone.service.ReturnService;
import com.gamezone.service.SaleService;
import com.gamezone.service.WarrantyService;

/**
 * Main menu of the application.
 * <p>
 * It shows the seven modules of the store and opens the submenu the user chooses. Each
 * submenu reads the input, calls the services of its module and prints the results, so this
 * class only builds them and moves between them. Every message shown to the user is in
 * Spanish, and an error in one operation never closes the application or shows a stack
 * trace.
 */
public class ConsoleMenu {

    private final ConsoleInput input;
    private final ProductMenu productMenu;
    private final PersonMenu personMenu;
    private final SaleMenu saleMenu;
    private final AccessoryMenu accessoryMenu;
    private final PromotionMenu promotionMenu;
    private final WarrantyMenu warrantyMenu;
    private final ReturnMenu returnMenu;

    /**
     * Creates the main menu and one submenu per module, all of them sharing the same reader
     * of the console.
     *
     * @param productService   service for products
     * @param personService    service for customers and sellers
     * @param saleService      service for sales
     * @param accessoryService service for accessories
     * @param promotionService service for promotions
     * @param warrantyService  service for warranties
     * @param returnService    service for returns and the monthly balance
     */
    public ConsoleMenu(ProductService productService, PersonService personService, SaleService saleService,
                       AccessoryService accessoryService, PromotionService promotionService,
                       WarrantyService warrantyService, ReturnService returnService) {
        this.input = new ConsoleInput();
        this.productMenu = new ProductMenu(input, productService);
        this.personMenu = new PersonMenu(input, personService);
        this.saleMenu = new SaleMenu(input, saleService, productService, personMenu);
        this.accessoryMenu = new AccessoryMenu(input, accessoryService, productService);
        this.promotionMenu = new PromotionMenu(input, promotionService);
        this.warrantyMenu = new WarrantyMenu(input, warrantyService);
        this.returnMenu = new ReturnMenu(input, returnService, saleService);
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
                case 7 -> returnMenu.show();
                case 0 -> System.out.println("Hasta pronto.");
                default -> System.out.println("Opción no válida.");
            }
        } while (option != 0);
    }
}
