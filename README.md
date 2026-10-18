# GameZone Unicesar

Information system for GameZone Unicesar, a video game and console store in Valledupar.

Course: Programación III — Universidad Popular del Cesar.

## Architecture

Java 17 console application built with Maven and organized in four layers:

| Layer | Package | Responsibility |
|---|---|---|
| Model | `com.gamezone.model` | Domain classes: `Person`, `Customer`, `Seller`, `Product`, `VideoGame`, `Console`, `Accessory`, `Controller`, `Cable`, `Memory`, `Promotion`, `PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount`, `Warranty`, `BasicWarranty`, `ExtendedWarranty`, `Return`, `Sale` |
| Persistence | `com.gamezone.persistence` | Read and write the files in `data/` |
| Service | `com.gamezone.service` | Business rules; the only layer that calls the repositories |
| UI | `com.gamezone.ui` | Console menu in Spanish |

Dependencies go `ui → service → persistence → model`. `Main` creates every object and
connects the layers. The design is documented in `docs/`:

- `docs/analysis.md` — answers to the guiding questions
- `docs/hierarchy-diagram.md`, `docs/class-diagram.md`, `docs/layers-diagram.md` — Mermaid diagrams
- `docs/accessory-analysis.md`, `docs/accessory-class-diagram.md` — accessory module (Requirement 1)
- `docs/promotion-analysis.md`, `docs/promotion-class-diagram.md` — promotion module (Requirement 2)
- `docs/warranty-analysis.md`, `docs/warranty-class-diagram.md` — warranty module (Requirement 4)
- `docs/return-analysis.md`, `docs/return-class-diagram.md` — return module (Requirement 3)

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
| `data/warranties.csv` | Created when the first console is sold; no preloaded data |
| `data/returns.csv` | Created when the first return is registered; no preloaded data |

The data is loaded when the application starts and saved after every operation.

## Menu operations

| Menu | Operations |
|---|---|
| 1. Gestión de productos | Register a video game, register a console, list the inventory |
| 2. Gestión de personas | Register a customer, list customers, list sellers |
| 3. Gestión de ventas | Register a sale, full sales history, purchase history of a customer, sales served by a seller, detail of one sale |
| 4. Gestión de accesorios | Register a controller, a cable or a memory card; list all accessories; list by type; list the accessories compatible with a console |
| 5. Gestión de promociones | Register a percentage, category or bulk purchase promotion; list all promotions; list the active promotions |
| 6. Gestión de garantías | Show the warranty of a product in a sale; list all warranties; list the active warranties; list the warranties expiring in the next days |
| 7. Gestión de devoluciones | Register a return; list all returns; list the returns of a customer; list the returns of a sale; monthly balance |

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

## Warranties (Requirement 4)

`Warranty` is abstract and computes its end date from its duration:

- `BasicWarranty`: 6 months, factory defects only, no cost. Every console sold gets one
  automatically.
- `ExtendedWarranty`: 12 months, factory defects and accidental damage. It costs 10% of
  the console price. When a sale is registered, the menu asks whether each console gets
  one, and its cost is added to the sale total.

The total of a sale is the subtotal minus the promotion discount plus the extended warranty
cost. Video games and accessories have no warranty. Warranty ids look like `W0001`.

## Returns (Requirement 3)

A customer can return some or all the products of a sale within 30 calendar days of the
sale date. Every returned product must belong to that sale, and a unit cannot be returned
twice. The refund is the sum of the prices of the returned products, and the stock of
returned video games and consoles goes back to the inventory through
`ProductService.restoreStock`.

The monthly balance shows, for a month and year, the total of the sales minus the refunds
of the returns. Return ids look like `R0001`.

The integration adjustments of Requirement 5 complete this module. They cover the stock
of returned accessories, refunds of discounted sales, the full balance report, and
cancelling the warranties of returned consoles.

## Team

See `TEAM.md`.
