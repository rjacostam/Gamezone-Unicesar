package com.gamezone.ui;

import com.gamezone.model.Customer;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.SaleService;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Console menu exposing the ten required operations.
 * Only talks to services, never to repositories.
 */
public class ConsoleMenu {
    private ProductService productService;
    private PersonService personService;
    private SaleService saleService;
    private Scanner scanner;

    /**
     * Creates menu with injected services.
     * @param productService product service
     * @param personService person service
     * @param saleService sale service
     */
    public ConsoleMenu(ProductService productService, PersonService personService, SaleService saleService) {
        this.productService = productService;
        this.personService = personService;
        this.saleService = saleService;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Starts the main loop.
     */
    public void start() {
        int option = -1;
        while (option != 0) {
            System.out.println("\n=== GameZone Unicesar ===");
            System.out.println("1. Register video game");
            System.out.println("2. Register console");
            System.out.println("3. List products");
            System.out.println("4. Register customer");
            System.out.println("5. List customers");
            System.out.println("6. List sellers");
            System.out.println("7. Register sale");
            System.out.println("8. List all sales");
            System.out.println("9. Sales by customer");
            System.out.println("10. Sales by seller");
            System.out.println("0. Exit");
            System.out.print("Choose: ");
            try {
                option = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number");
                continue;
            }
            try {
                switch (option) {
                    case 1 -> registerGame();
                    case 2 -> registerConsole();
                    case 3 -> listProducts();
                    case 4 -> registerCustomer();
                    case 5 -> listCustomers();
                    case 6 -> listSellers();
                    case 7 -> registerSale();
                    case 8 -> listSales(saleService.findAll());
                    case 9 -> salesByCustomer();
                    case 10 -> salesBySeller();
                    case 0 -> System.out.println("Bye");
                    default -> System.out.println("Unknown option");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void registerGame() {
        System.out.print("id: ");
        String id = scanner.nextLine().trim();
        System.out.print("title: ");
        String title = scanner.nextLine().trim();
        System.out.print("price: ");
        double price = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("stock: ");
        int stock = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("platform: ");
        String platform = scanner.nextLine().trim();
        System.out.print("genre: ");
        String genre = scanner.nextLine().trim();
        System.out.print("ageRating: ");
        String age = scanner.nextLine().trim();
        System.out.println(productService.registerVideoGame(id, title, price, stock, platform, genre, age).getFullDescription());
    }

    private void registerConsole() {
        System.out.print("id: ");
        String id = scanner.nextLine().trim();
        System.out.print("title: ");
        String title = scanner.nextLine().trim();
        System.out.print("price: ");
        double price = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("stock: ");
        int stock = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("brand: ");
        String brand = scanner.nextLine().trim();
        System.out.print("model: ");
        String model = scanner.nextLine().trim();
        System.out.print("generation: ");
        String gen = scanner.nextLine().trim();
        System.out.println(productService.registerConsole(id, title, price, stock, brand, model, gen).getFullDescription());
    }

    private void listProducts() {
        for (Product p : productService.findAll()) {
            System.out.println(p.getFullDescription());
        }
    }

    private void registerCustomer() {
        System.out.print("name: ");
        String name = scanner.nextLine().trim();
        System.out.print("nationalId: ");
        String nid = scanner.nextLine().trim();
        System.out.print("phone: ");
        String phone = scanner.nextLine().trim();
        System.out.print("email: ");
        String email = scanner.nextLine().trim();
        System.out.println(personService.registerCustomer(name, nid, phone, email).getRoleLabel());
    }

    private void listCustomers() {
        for (Customer c : personService.findAllCustomers()) {
            System.out.println(c.getRoleLabel() + " id:" + c.getNationalId());
        }
    }

    private void listSellers() {
        for (Seller s : personService.findAllSellers()) {
            System.out.println(s.getRoleLabel());
        }
    }

    private void registerSale() {
        System.out.print("customer nationalId: ");
        Customer cu = personService.findCustomerById(scanner.nextLine().trim());
        if (cu == null) {
            System.out.println("Customer not found");
            return;
        }
        System.out.print("seller code: ");
        Seller se = personService.findSellerByCode(scanner.nextLine().trim());
        if (se == null) {
            System.out.println("Seller not found");
            return;
        }
        List<Product> items = new ArrayList<>();
        while (true) {
            System.out.print("product id (empty to finish): ");
            String pid = scanner.nextLine().trim();
            if (pid.isEmpty()) {
                break;
            }
            Product p = productService.findById(pid);
            if (p == null) {
                System.out.println("Not found");
            } else {
                items.add(p);
            }
        }
        Sale s = saleService.registerSale(cu, se, items);
        System.out.println("Sale " + s.getId() + " total $" + s.calculateTotal());
    }

    private void listSales(List<Sale> sales) {
        for (Sale s : sales) {
            System.out.println(s.getId() + " " + s.getDate()
                + " cust:" + s.getCustomer().getNationalId()
                + " seller:" + s.getSeller().getEmployeeCode()
                + " total:$" + s.calculateTotal() + " items:" + s.getProducts().size());
        }
    }

    private void salesByCustomer() {
        System.out.print("customer nationalId: ");
        listSales(saleService.findByCustomer(scanner.nextLine().trim()));
    }

    private void salesBySeller() {
        System.out.print("seller code: ");
        listSales(saleService.findBySeller(scanner.nextLine().trim()));
    }
}
