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
import javax.swing.JOptionPane;

// Menú con ventanas para la prueba. La UI solo habla con servicios, nunca con repositorios.
public class ConsoleMenu {
    // Servicios que nos pasa el Main ya cargados
    private ProductService productService;
    private PersonService personService;
    private SaleService saleService;

    // Guardamos los servicios para usarlos en todo el menú
    public ConsoleMenu(ProductService productService, PersonService personService, SaleService saleService) {
        this.productService = productService;
        this.personService = personService;
        this.saleService = saleService;
    }

    // Bucle principal con las 10 operaciones que pide el taller
    public void start() {
        String menu = "GameZone Unicesar\n"
            + "1. Register video game\n2. Register console\n3. List products\n"
            + "4. Register customer\n5. List customers\n6. List sellers\n"
            + "7. Register sale\n8. List all sales\n"
            + "9. Sales by customer\n10. Sales by seller\n0. Exit";
        int option = -1;
        while (option != 0) {
            try {
                String input = JOptionPane.showInputDialog(null, menu + "\nChoose option:");
                if (input == null) {
                    break; // cerró la ventana, salimos sin pelear
                }
                option = Integer.parseInt(input.trim());
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
                    case 0 -> JOptionPane.showMessageDialog(null, "Bye, thanks for using GameZone");
                    default -> JOptionPane.showMessageDialog(null, "Unknown option");
                }
            } catch (NumberFormatException e) {
                // Si mete letras en vez de número, le avisamos en buena onda
                JOptionPane.showMessageDialog(null, "Please enter a valid number, e.g. 3");
            } catch (Exception e) {
                // Cualquier otro error lo mostramos sin tecnicismos raros
                JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
            }
        }
    }

    // Pide los datos del juego por ventanitas y lo guarda
    private void registerGame() {
        String id = ask("Game id (e.g. G001):");
        String title = ask("Title:");
        double price = Double.parseDouble(ask("Price:"));
        int stock = Integer.parseInt(ask("Stock:"));
        String platform = ask("Platform (PS5/Xbox/Switch/PC):");
        String genre = ask("Genre:");
        String ageRating = ask("Age rating (E/T/M):");
        Product created = productService.registerVideoGame(id, title, price, stock, platform, genre, ageRating);
        show(created.getFullDescription());
    }

    // Lo mismo pero para consolas
    private void registerConsole() {
        String id = ask("Console id (e.g. C001):");
        String title = ask("Title (e.g. PlayStation 5):");
        double price = Double.parseDouble(ask("Price:"));
        int stock = Integer.parseInt(ask("Stock:"));
        String brand = ask("Brand:");
        String model = ask("Model:");
        String generation = ask("Generation:");
        Product created = productService.registerConsole(id, title, price, stock, brand, model, generation);
        show(created.getFullDescription());
    }

    // Junta todo el inventario en un solo mensaje
    private void listProducts() {
        StringBuilder builder = new StringBuilder("Inventory:\n");
        for (Product current : productService.findAll()) {
            builder.append(current.getFullDescription()).append("\n");
        }
        show(builder.toString());
    }

    // Registra un cliente nuevo
    private void registerCustomer() {
        String name = ask("Full name:");
        String nationalId = ask("National id:");
        String phone = ask("Phone:");
        String email = ask("Email:");
        Customer created = personService.registerCustomer(name, nationalId, phone, email);
        show("Done: " + created.getRoleLabel());
    }

    // Lista solo los clientes
    private void listCustomers() {
        StringBuilder builder = new StringBuilder("Customers:\n");
        for (Customer current : personService.findAllCustomers()) {
            builder.append(current.getRoleLabel()).append(" id:").append(current.getNationalId()).append("\n");
        }
        show(builder.toString());
    }

    // Lista solo los vendedores que ya estaban contratados
    private void listSellers() {
        StringBuilder builder = new StringBuilder("Sellers:\n");
        for (Seller current : personService.findAllSellers()) {
            builder.append(current.getRoleLabel()).append("\n");
        }
        show(builder.toString());
    }

    // Arma la venta: busca cliente y vendedor y va sumando productos
    private void registerSale() {
        Customer customer = personService.findCustomerById(ask("Customer national id:"));
        if (customer == null) {
            show("Customer not found");
            return;
        }
        Seller seller = personService.findSellerByCode(ask("Seller code (e.g. EMP-001):"));
        if (seller == null) {
            show("Seller not found");
            return;
        }
        List<Product> items = new ArrayList<>();
        while (true) {
            String productId = JOptionPane.showInputDialog(null, "Product id (empty to finish):");
            if (productId == null || productId.trim().isEmpty()) {
                break; // ya terminó de agregar
            }
            Product found = productService.findById(productId.trim());
            if (found == null) {
                show("Product not found");
            } else {
                items.add(found);
            }
        }
        Sale created = saleService.registerSale(customer, seller, items);
        show("Sale " + created.getId() + " total $" + created.calculateTotal());
    }

    // Muestra cualquier lista de ventas en el mismo formato
    private void listSales(List<Sale> sales) {
        StringBuilder builder = new StringBuilder("Sales:\n");
        for (Sale current : sales) {
            builder.append(current.getId()).append(" ").append(current.getDate())
              .append(" cust:").append(current.getCustomer().getNationalId())
              .append(" seller:").append(current.getSeller().getEmployeeCode())
              .append(" total:$").append(current.calculateTotal()).append("\n");
        }
        show(builder.toString());
    }

    // Filtra por cliente
    private void salesByCustomer() {
        listSales(saleService.findByCustomer(ask("Customer national id:")));
    }

    // Filtra por vendedor
    private void salesBySeller() {
        listSales(saleService.findBySeller(ask("Seller code:")));
    }

    // Atajo para pedir un dato sin repetir el JOptionPane
    private String ask(String message) {
        String value = JOptionPane.showInputDialog(null, message);
        return value == null ? "" : value.trim();
    }

    // Atajo para mostrar un mensaje
    private void show(String message) {
        JOptionPane.showMessageDialog(null, message);
    }
}
