package com.gamezone;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.persistence.AccessoryRepository;
import com.gamezone.persistence.PersonRepository;
import com.gamezone.persistence.ProductRepository;
import com.gamezone.persistence.PromotionRepository;
import com.gamezone.persistence.ReturnRepository;
import com.gamezone.persistence.SaleRepository;
import com.gamezone.persistence.WarrantyRepository;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.MonthlyBalanceService;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.PromotionService;
import com.gamezone.service.ReturnService;
import com.gamezone.service.SaleService;
import com.gamezone.service.StoreCatalog;
import com.gamezone.service.WarrantyService;
import com.gamezone.ui.AccessoryMenu;
import com.gamezone.ui.ConsoleInput;
import com.gamezone.ui.ConsoleMenu;
import com.gamezone.ui.PersonMenu;
import com.gamezone.ui.ProductMenu;
import com.gamezone.ui.PromotionMenu;
import com.gamezone.ui.ReturnMenu;
import com.gamezone.ui.SaleMenu;
import com.gamezone.ui.WarrantyMenu;

import java.util.List;


/**
 * Entry point of the GameZone Unicesar application.
 * <p>
 * It builds every layer from the bottom up and injects each dependency through the
 * constructors, so no class creates its own collaborators. Each service loads its stored
 * data when it is created, except the warranties: they point to sales, so they are loaded
 * right after the sales exist.
 */
public class Main {

    private static final String DATA_FOLDER = "data/";

    /**
     * Starts the application. If the stored data cannot be loaded, it shows a message and
     * ends instead of starting the menu with incomplete information.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            ProductRepository productRepository = new ProductRepository(DATA_FOLDER + "products.csv");
            PersonRepository personRepository = new PersonRepository(
                    DATA_FOLDER + "customers.csv", DATA_FOLDER + "sellers.csv");
            SaleRepository saleRepository = new SaleRepository(DATA_FOLDER + "sales.csv");
            AccessoryRepository accessoryRepository = new AccessoryRepository(DATA_FOLDER + "accessories.csv");
            PromotionRepository promotionRepository = new PromotionRepository(DATA_FOLDER + "promotions.csv");
            WarrantyRepository warrantyRepository = new WarrantyRepository(DATA_FOLDER + "warranties.csv");
            ReturnRepository returnRepository = new ReturnRepository(DATA_FOLDER + "returns.csv");

            ProductService productService = new ProductService(productRepository);
            PersonService personService = new PersonService(personRepository);
            AccessoryService accessoryService = new AccessoryService(accessoryRepository, productService);
            PromotionService promotionService = new PromotionService(promotionRepository);
            WarrantyService warrantyService = new WarrantyService(warrantyRepository);
            StoreCatalog catalog = new StoreCatalog(
                    List.of(productService::listAllProducts, accessoryService::listAllAccessories),
                    List.of(productService, accessoryService));
            SaleService saleService = new SaleService(saleRepository, catalog,
                    List.of(productService, accessoryService), personService,
                    promotionService, warrantyService);
            warrantyService.loadWarranties(saleService.listAllSales());
            ReturnService returnService = new ReturnService(returnRepository, saleService, productService,
                    accessoryService, warrantyService);

            ConsoleInput input = new ConsoleInput();
            PersonMenu personMenu = new PersonMenu(input, personService);
            ConsoleMenu menu = new ConsoleMenu(input,
                    new ProductMenu(input, productService),
                    personMenu,
                    new SaleMenu(input, saleService, catalog, productService, personMenu),
                    new AccessoryMenu(input, accessoryService, productService),
                    new PromotionMenu(input, promotionService),
                    new WarrantyMenu(input, warrantyService),
                    new ReturnMenu(input, returnService,
                            new MonthlyBalanceService(saleService, returnService), saleService));
            menu.start();
        } catch (PersistenceException e) {
            System.out.println("No fue posible cargar los datos del sistema. Contacte al administrador.");
        }
    }
}
