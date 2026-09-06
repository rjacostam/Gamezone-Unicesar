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

// Menú con ventanitas (JOptionPane) para la prueba.
// Ojo: el taller pide menú de consola y JavaDoc en inglés,
// esto es solo versión de prueba, no para entregar así.
public class ConsoleMenu {
    // Servicios inyectados, la UI nunca toca los repositorios directo
    private ProductService productService;
    private PersonService personService;
    private SaleService saleService;

    // Armamos el menú con lo que ya viene cargado del Main
    public ConsoleMenu(ProductService productService, PersonService personService, SaleService saleService) {
        this.productService = productService;
        this.personService = personService;
        this.saleService = saleService;
    }

    // Bucle principal, muestra las 10 opciones que pide el taller
    public void start() {
        String menu = "GameZone Unicesar\n"
            + "1. Registrar videojuego\n2. Registrar consola\n3. Listar productos\n"
            + "4. Registrar cliente\n5. Listar clientes\n6. Listar vendedores\n"
            + "7. Registrar venta\n8. Ver todas las ventas\n"
            + "9. Compras por cliente\n10. Ventas por vendedor\n0. Salir";
        int op = -1;
        while (op != 0) {
            try {
                String input = JOptionPane.showInputDialog(null, menu + "\nElige opción:");
                if (input == null) {
                    break; // cerró la ventana
                }
                op = Integer.parseInt(input.trim());
                switch (op) {
                    case 1 -> registrarJuego();
                    case 2 -> registrarConsola();
                    case 3 -> listarProductos();
                    case 4 -> registrarCliente();
                    case 5 -> listarClientes();
                    case 6 -> listarVendedores();
                    case 7 -> registrarVenta();
                    case 8 -> listarVentas(saleService.findAll());
                    case 9 -> ventasPorCliente();
                    case 10 -> ventasPorVendedor();
                    case 0 -> JOptionPane.showMessageDialog(null, "Nos vemos, gracias por usar GameZone");
                    default -> JOptionPane.showMessageDialog(null, "Esa opción no existe");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Mete un número válido, ej: 3");
            } catch (Exception e) {
                // Mostramos el error en cristiano para no asustar al usuario
                JOptionPane.showMessageDialog(null, "Ups, pasó algo: " + e.getMessage());
            }
        }
    }

    // Pide los datos del juego por ventanitas y lo guarda
    private void registrarJuego() {
        String id = pedir("ID del juego (ej: G001):");
        String titulo = pedir("Título:");
        double precio = Double.parseDouble(pedir("Precio:"));
        int stock = Integer.parseInt(pedir("Stock:"));
        String plat = pedir("Plataforma (PS5/Xbox/Switch/PC):");
        String genero = pedir("Género:");
        String edad = pedir("Clasificación edad (E/T/M):");
        Product p = productService.registerVideoGame(id, titulo, precio, stock, plat, genero, edad);
        mostrar(p.getFullDescription());
    }

    // Lo mismo pero para consolas
    private void registrarConsola() {
        String id = pedir("ID consola (ej: C001):");
        String titulo = pedir("Título (ej: PlayStation 5):");
        double precio = Double.parseDouble(pedir("Precio:"));
        int stock = Integer.parseInt(pedir("Stock:"));
        String marca = pedir("Marca:");
        String modelo = pedir("Modelo:");
        String gen = pedir("Generación:");
        Product p = productService.registerConsole(id, titulo, precio, stock, marca, modelo, gen);
        mostrar(p.getFullDescription());
    }

    // Muestra todo el inventario junto
    private void listarProductos() {
        StringBuilder sb = new StringBuilder("Inventario:\n");
        for (Product p : productService.findAll()) {
            sb.append(p.getFullDescription()).append("\n");
        }
        mostrar(sb.toString());
    }

    // Registra un cliente nuevo
    private void registrarCliente() {
        String nombre = pedir("Nombre completo:");
        String cedula = pedir("Identificación:");
        String tel = pedir("Teléfono:");
        String correo = pedir("Correo:");
        Customer c = personService.registerCustomer(nombre, cedula, tel, correo);
        mostrar("Listo: " + c.getRoleLabel());
    }

    private void listarClientes() {
        StringBuilder sb = new StringBuilder("Clientes:\n");
        for (Customer c : personService.findAllCustomers()) {
            sb.append(c.getRoleLabel()).append(" id:").append(c.getNationalId()).append("\n");
        }
        mostrar(sb.toString());
    }

    private void listarVendedores() {
        StringBuilder sb = new StringBuilder("Vendedores:\n");
        for (Seller s : personService.findAllSellers()) {
            sb.append(s.getRoleLabel()).append("\n");
        }
        mostrar(sb.toString());
    }

    // La parte más delicada: armar la venta y descontar stock
    private void registrarVenta() {
        Customer cli = personService.findCustomerById(pedir("ID del cliente:"));
        if (cli == null) {
            mostrar("Ese cliente no existe");
            return;
        }
        Seller ven = personService.findSellerByCode(pedir("Código vendedor (ej: EMP-001):"));
        if (ven == null) {
            mostrar("Ese vendedor no existe");
            return;
        }
        List<Product> items = new ArrayList<>();
        while (true) {
            String pid = JOptionPane.showInputDialog(null, "ID producto (vacío para terminar):");
            if (pid == null || pid.trim().isEmpty()) {
                break;
            }
            Product p = productService.findById(pid.trim());
            if (p == null) {
                mostrar("No encontré ese producto");
            } else {
                items.add(p);
            }
        }
        Sale s = saleService.registerSale(cli, ven, items);
        mostrar("Venta " + s.getId() + " total $" + s.calculateTotal());
    }

    private void listarVentas(List<Sale> ventas) {
        StringBuilder sb = new StringBuilder("Ventas:\n");
        for (Sale s : ventas) {
            sb.append(s.getId()).append(" ").append(s.getDate())
              .append(" cli:").append(s.getCustomer().getNationalId())
              .append(" vend:").append(s.getSeller().getEmployeeCode())
              .append(" total:$").append(s.calculateTotal()).append("\n");
        }
        mostrar(sb.toString());
    }

    private void ventasPorCliente() {
        listarVentas(saleService.findByCustomer(pedir("ID del cliente:")));
    }

    private void ventasPorVendedor() {
        listarVentas(saleService.findBySeller(pedir("Código vendedor:")));
    }

    // Atajos para no repetir JOptionPane a cada rato
    private String pedir(String msg) {
        String v = JOptionPane.showInputDialog(null, msg);
        return v == null ? "" : v.trim();
    }

    private void mostrar(String msg) {
        JOptionPane.showMessageDialog(null, msg);
    }
}
