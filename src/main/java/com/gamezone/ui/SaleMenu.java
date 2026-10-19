package com.gamezone.ui;

import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.service.ItemFinder;
import com.gamezone.service.SaleService;
import com.gamezone.service.StoreCatalog;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Submenu of the sales: registers a sale and shows the sale history, the purchases of a
 * customer, the sales of a seller and the detail of one sale.
 */
public class SaleMenu extends Submenu {

    private final SaleService saleService;
    private final StoreCatalog catalog;
    private final ItemFinder productFinder;
    private final SellerLister sellerLister;

    /**
     * Creates the submenu.
     *
     * @param input          reader of the console
     * @param saleService    service for sales
     * @param catalog        catalogue of everything the store sells
     * @param productFinder  search of the product catalogue, used to offer the extended warranty
     * @param sellerLister   prints the sellers before asking for one
     */
    public SaleMenu(ConsoleInput input, SaleService saleService, StoreCatalog catalog, ItemFinder productFinder,
                    SellerLister sellerLister) {
        super(input);
        this.saleService = saleService;
        this.catalog = catalog;
        this.productFinder = productFinder;
        this.sellerLister = sellerLister;
    }

    @Override
    public void show() {
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
        sellerLister.listSellers();
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
            Product product = productFinder.findById(productId);
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
        List<Product> items = catalog.listAll();
        if (items.isEmpty()) {
            System.out.println("No hay productos ni accesorios registrados.");
            return;
        }
        for (Product item : items) {
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
}
