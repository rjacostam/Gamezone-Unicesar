package com.gamezone.ui;

/**
 * Main menu of the application.
 * <p>
 * It shows the seven modules of the store and opens the submenu the user chooses. Each
 * submenu reads the input, calls the services of its module and prints the results. The
 * submenus are built by the composition root, {@code Main}, and received here as
 * {@link Submenu}, so this class only moves between them. Every message shown to the user is in
 * Spanish, and an error in one operation never closes the application or shows a stack
 * trace.
 */
public class ConsoleMenu {

    private final ConsoleInput input;
    private final Submenu productMenu;
    private final Submenu personMenu;
    private final Submenu saleMenu;
    private final Submenu accessoryMenu;
    private final Submenu promotionMenu;
    private final Submenu warrantyMenu;
    private final Submenu returnMenu;

    /**
     * Creates the main menu with the reader of the console and the seven submenus, in the
     * order of the options of the main menu.
     *
     * @param input         reader of the console, shared with the submenus
     * @param productMenu   option 1, products
     * @param personMenu    option 2, people
     * @param saleMenu      option 3, sales
     * @param accessoryMenu option 4, accessories
     * @param promotionMenu option 5, promotions
     * @param warrantyMenu  option 6, warranties
     * @param returnMenu    option 7, returns
     */
    public ConsoleMenu(ConsoleInput input, Submenu productMenu, Submenu personMenu, Submenu saleMenu,
                       Submenu accessoryMenu, Submenu promotionMenu, Submenu warrantyMenu,
                       Submenu returnMenu) {
        this.input = input;
        this.productMenu = productMenu;
        this.personMenu = personMenu;
        this.saleMenu = saleMenu;
        this.accessoryMenu = accessoryMenu;
        this.promotionMenu = promotionMenu;
        this.warrantyMenu = warrantyMenu;
        this.returnMenu = returnMenu;
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
