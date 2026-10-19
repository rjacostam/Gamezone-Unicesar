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
- `docs/integration-analysis.md`, `docs/integrated-class-diagram.md` — integration of the four modules (Requirement 5)
- `docs/exception-analysis.md`, `docs/exception-class-diagram.md` — exceptions and validators (Requirement 6)
- `docs/grasp-audit/`, `docs/grasp-analysis.md`, `docs/grasp-class-diagram.md`, `docs/grasp-refactor-log.md`, `docs/regression/` — GRASP audit and correction (Requirement 7)
- `docs/solid-audit/`, `docs/solid-analysis.md`, `docs/solid-class-diagram.md`, `docs/solid-refactor-log.md` — SOLID audit and correction (Requirement 8)

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
| 7. Gestión de devoluciones | Register a return; list all returns; list the returns of a customer; list the returns of a sale; monthly balance (sales, returns and net) |

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
twice. Return ids look like `R0001`.

- The stock of every returned item goes back to the inventory. Video games and consoles go
  through `ProductService.restoreStock`, and accessories through
  `AccessoryService.restoreStock`.
- Each item is refunded in proportion to the discount of the original sale:
  `price × (1 − discount / subtotal)`.
- Returning a console cancels its warranties, and the cost of a cancelled extended
  warranty is added to the refund.

The monthly balance shows, for a month and year, the total of the sales (with discounts and
extended warranties), the total refunded, and the net balance.

## Integration (Requirement 5)

The four modules run together on the same sales. The integration adjustments A1 to A7 are
explained in `docs/integration-analysis.md`. The most visible ones:

- A sale can mix video games, consoles and accessories. It always follows the same eight
  steps:
  1. validate the sale;
  2. resolve the items and check stock;
  3. create the sale and compute the subtotal;
  4. apply the best promotion on the subtotal;
  5. assign the warranties;
  6. compute the total;
  7. update the inventory;
  8. save the sale.
- Promotions by category also apply to accessories (`ACCESSORY`).
- Returns restore accessory stock, refund what the customer actually paid, and cancel the
  warranties of returned consoles.

Example of the integrated flow:

1. Sell a PlayStation 5 Slim, a video game and a controller with an extended warranty for
   the console: subtotal $3,100,000, "Lleva 3 o más" −$310,000, warranty +$250,000, total
   **$3,040,000**.
2. Return the console and the controller. The refund is $2,250,000 + $315,000 + $250,000
   for the cancelled warranty = **$2,815,000**. The stock goes back and the console
   warranties are removed.
3. The balance of the month shows sales $3,040,000, returns $2,815,000 and a net balance of
   **$225,000**.

## Exceptions and validation (Requirement 6)

Two cross-cutting packages separate error handling from the business logic:

- `com.gamezone.exceptions`: `GameZoneException`, an abstract unchecked exception with an
  error code, and its four subclasses:
  - `ResourceNotFoundException`: the product, customer, sale or other resource does not
    exist.
  - `BusinessRuleException`: a rule of the store is broken, such as not enough stock, a
    duplicate id or more than 30 days for a return.
  - `InvalidDataException`: a value has a wrong format or range, such as an empty title or
    a price that is not greater than zero.
  - `PersistenceException`: a data file cannot be read or written. It keeps the original
    `IOException`.
- `com.gamezone.validation`: one validator per module (`ProductValidator`,
  `PersonValidator`, `SaleValidator`, `AccessoryValidator`, `PromotionValidator`,
  `WarrantyValidator`, `ReturnValidator`). They hold static checks, and the services call
  them before changing anything. They never use the persistence layer.

The console menu runs every operation through `runSafely`, which shows a different message
for each kind of error:

| Error | Message |
|---|---|
| Resource not found | "No se encontró el recurso solicitado: …" |
| Business rule | "No se puede completar la operación: …" |
| Invalid data | "Los datos ingresados no son válidos: …" |
| Data files | "Ocurrió un problema al acceder a los datos del sistema. Contacte al administrador." |

The user never sees a stack trace. If the data files cannot be loaded at startup, the
application shows a clear message and stops.

## GRASP audit and correction (Requirement 7)

The requirement did not add any operation to the menu. It was done in two phases.

**Phase 1, audit.** Each member audited the code he wrote against the nine GRASP patterns
(`docs/grasp-audit/leader-audit.md`, `developer1-audit.md`, `developer2-audit.md`). The
Technical Leader merged the three reports into `docs/grasp-audit/consolidated-audit.md`,
audited on `develop` at `6c10ce2`: the 48 classes were audited once each, the 13 violations
of the reports became 9 after merging the ones that described the same defect, and each one
got its responsible members. The answers to the five guiding questions are in
`docs/grasp-analysis.md`.

**Phase 2, correction.** The nine violations were corrected without changing what the user
sees:

| Violation | Pattern | Design change |
|---|---|---|
| V-01 | Information Expert | `Product.increaseStock`; `ProductService.restoreStock` calls it |
| V-02 | Information Expert | Two promotions read `Sale.getSubtotal()` instead of adding the prices again |
| V-03 | Information Expert | `Sale.countUnitsOf` and `Sale.findProductById`, used by the return module |
| V-04 | Controller | `Product.isConsole()` answers the rule "only consoles", used by the menu, the services and the validators |
| V-05 | High Cohesion | The receipt, the return receipt and the warranty certificate are built in the `ui` package (`ReceiptGenerator`, `ReturnReceiptFormatter`, `WarrantyCertificateFormatter`) |
| V-06 | High Cohesion | `ConsoleMenu` is now only the main menu; each module has its own submenu, with `Submenu` as base class and `ConsoleInput` for the input |
| V-07 | Polymorphism | `InventoryCatalog`, implemented by `ProductService` and `AccessoryService`; the services ask the catalogue that owns an item |
| V-08 | Polymorphism | The model publishes its type code and its extra columns; the four repositories write them without `instanceof` |
| V-09 | Polymorphism | Each warranty answers its own coverage |

After the correction there is no `instanceof` left in the source code, and `ConsoleMenu` went
from 698 lines to 86. The before and after diagrams are in `docs/grasp-class-diagram.md`.

**No regression.** The two input scripts of `docs/regression/` go through every operation of
the menu and restart the program on the data left by the first run. They were run on
`develop` before the first correction and on the final `develop`: the screen output and the
eight data files are identical. Each correction, its commit and its no regression check are
recorded in `docs/grasp-refactor-log.md`.

One functional defect found in the audit was left out of scope on purpose (OOS-01: a damaged
line in `data/sales.csv` stops the program at startup), because correcting it would change
what the user sees.

## SOLID audit and correction (Requirement 8)

The requirement did not add any operation to the menu. It was done in two phases on the code
already corrected in Requirement 7.

**Phase 1, audit.** Each member audited the code he wrote, including the classes he created in
Requirement 7, against the five SOLID principles (`docs/solid-audit/leader-audit.md`,
`developer1-audit.md`, `developer2-audit.md`). The Technical Leader merged the three reports
into `docs/solid-audit/consolidated-audit.md`, audited on `develop` at `675e61c`: the 61 classes
were audited once each, the 17 violations of the reports became 13, and each one got its
responsible members. One new functional defect was left out of scope (OOS-02: the sale receipt
depends on the language of the computer). The answers to the five guiding questions are in
`docs/solid-analysis.md`.

**Phase 2, correction.** The thirteen violations were corrected without changing what the user
sees:

| Violation | Principle | Design change |
|---|---|---|
| V-01 | Single Responsibility | `StoreCatalog` lists and searches what the store sells; `SaleService` no longer builds the catalogue |
| V-02 | Single Responsibility | `AmountFormat` owns the money and percentage formats, keeping the current output of every place |
| V-03 | Single Responsibility | `MonthlyBalanceService` builds the monthly balance; `ReturnService` only manages returns |
| V-04 | Open/Closed | `Submenu.runSafely` also catches any other `GameZoneException`, so a new kind of error needs no edit |
| V-05 | Open/Closed | `Console` answers "is a console" and its category from one constant |
| V-06 | Open/Closed | `AccessoryFactory`, `PromotionFactory` and a single `register` per service replace one method per kind |
| V-07 | Liskov Substitution | Both catalogues follow the same `findById` contract |
| V-08 | Interface Segregation | One row contract per data file (`ProductCatalogRow`, `AccessoryCatalogRow`, `PromotionCatalogRow`) |
| V-09 | Interface Segregation | `InventoryCatalog` split into `ItemFinder`, `StockUpdater` and `StockRestorer`, then deleted |
| V-10 | Interface Segregation | `SaleMenu` depends on `ItemFinder` and `SellerLister` |
| V-11 | Dependency Inversion | `SaleService` and `ReturnService` depend on small contracts (`SaleStore`, `ReturnStore`, `SaleFinder`, `PersonFinder`, `PromotionChooser`, `WarrantyAssigner`, `WarrantyCanceller`) |
| V-12 | Dependency Inversion | `AccessoryService` depends on `ItemFinder` instead of `ProductService` |
| V-13 | Dependency Inversion | `Main` builds the submenus and `ConsoleMenu` receives them as `Submenu` |

The layer order is kept: the new contracts that the services use for the storage live in
`persistence`, the factories in `model`, and `AmountFormat` in its own `format` package, which
the model can use. The before and after diagrams are in `docs/solid-class-diagram.md`.

**No regression.** The two input scripts of `docs/regression/` were run on `develop` before the
first correction and on the final `develop`, on the same day: the screen output and the eight
data files are identical. Each correction, its commit and its check are recorded in
`docs/solid-refactor-log.md`.

## Team

See `TEAM.md`.
