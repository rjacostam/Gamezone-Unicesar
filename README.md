# GameZone Unicesar

Information system for GameZone Unicesar, a video game and console store in Valledupar.

Course: Programación III — Universidad Popular del Cesar.

## Architecture

Java 17 console application built with Maven and organized in four layers:

| Layer | Package | Responsibility |
|---|---|---|
| Model | `com.gamezone.model` | Domain classes: `Person`, `Customer`, `Seller`, `Product`, `VideoGame`, `Console`, `Accessory`, `Controller`, `Cable`, `Memory`, `Promotion`, `PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount`, `Sale` |
| Persistence | `com.gamezone.persistence` | Read and write the files in `data/` |
| Service | `com.gamezone.service` | Business rules; the only layer that calls the repositories |
| UI | `com.gamezone.ui` | Console menu in Spanish |

Dependencies go `ui → service → persistence → model`. `Main` creates every object and
connects the layers. The design is documented in `docs/`:

- `docs/analysis.md` — answers to the guiding questions
- `docs/hierarchy-diagram.md`, `docs/class-diagram.md`, `docs/layers-diagram.md` — Mermaid diagrams
- `docs/accessory-analysis.md`, `docs/accessory-class-diagram.md` — accessory module (Requirement 1)
- `docs/promotion-analysis.md`, `docs/promotion-class-diagram.md` — promotion module (Requirement 2)

## Requirements

- Java 17 or later
- Maven 3.8 or later (optional, see below)

## Build and run

Run the commands from the repository root, because the program reads the `data/` folder.

With Maven:

```bash
mvn clean compile
mvn exec:java
```

Without Maven:

```bash
javac -encoding UTF-8 -d target/classes $(find src -name "*.java")
java -cp target/classes com.gamezone.Main
```

## Data files

| File | Content |
|---|---|
| `data/sellers.csv` | The three preloaded sellers of the store |
| `data/products.csv` | Preloaded video games and consoles; the stock changes with each sale |
| `data/accessories.csv` | Preloaded controller, cable and memory card, with their compatible consoles |
| `data/promotions.csv` | One preloaded promotion of each type, valid from 2026-10-01 to 2026-12-31 |
| `data/customers.csv` | Created when the first customer is registered |
| `data/sales.csv` | Created when the first sale is registered |

The data is loaded when the application starts and saved after every operation.

## Menu operations

| Menu | Operations |
|---|---|
| 1. Gestión de productos | Register a video game, register a console, list the inventory |
| 2. Gestión de personas | Register a customer, list customers, list sellers |
| 3. Gestión de ventas | Register a sale, full sales history, purchase history of a customer, sales served by a seller, detail of one sale |
| 4. Gestión de accesorios | Register a controller, a cable or a memory card; list all accessories; list by type; list the accessories compatible with a console |
| 5. Gestión de promociones | Register a percentage, category or bulk purchase promotion; list all promotions; list the active promotions |

A sale needs an existing customer, an existing seller (by identification or employee code)
and at least one item with enough stock. A sale can mix video games, consoles and
accessories, and the inventory of each item is updated automatically by its own service.

## Accessories (Requirement 1)

`Accessory` extends `Product`, so accessories are sold exactly like video games and
consoles. `Controller`, `Cable` and `Memory` are its concrete types, and each accessory
keeps the ids of the consoles it is compatible with. Accessory ids start with `A-`.

## Promotions (Requirement 2)

`Promotion` is abstract and each type computes its own discount:

- `PercentageDiscount`: a percentage of the subtotal.
- `CategoryDiscount`: a percentage of the price of the video games or the consoles of the
  sale.
- `BulkPurchaseDiscount`: a percentage of the subtotal when the sale has a minimum number of
  units.

A promotion only applies between its start and end dates. When a sale is registered,
`PromotionService` picks the active promotion that gives the highest discount, and only
that one is applied. The receipt shows the subtotal, the discount with the promotion name,
and the total. Promotion ids start with `P-`.

## Team

See `TEAM.md`.
