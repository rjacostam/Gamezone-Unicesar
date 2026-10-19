# Leader GRASP Audit — GameZone Unicesar

Author: Rafael (Technical Leader). Audited code: `develop` at commit `6c10ce2`.

## Scope

Classes and methods audited, by module and requirement, following the responsibility
table of Requirement 7.

| Module (requirement) | Class | Methods audited |
|---|---|---|
| Sales (Taller 1), discount and receipt (R2), warranty cost (R4) | `Sale` | constructor, `calculateSubtotal`, `calculateTotal`, `applyDiscount`, `addExtendedWarrantyCost`, `generateReceipt`, getters and setters. `canBeReturned` is audited by Developer 1. |
| Sales (Taller 1), extended in R2 and R4 | `SaleRepository` | `saveAll`, `loadAll`, `toLine`, `fromLine`, `findCustomer`, `findSeller`, `findProduct` |
| Sales (Taller 1), accessories (R1), promotions (R2), warranties (R4), integration (R5) | `SaleService` | constructor, `registerSale` and its private steps (`requireCustomer`, `requireSeller`, `resolveItems`, `checkStock`, `applyBestPromotion`, `assignWarranties`, `updateInventory`, `saveSales`), `getCatalog`, `findItem`, queries |
| User interface (Taller 1), integration of R1–R4, exceptions (R6) | `ConsoleMenu` | `start`, the seven submenus, `registerSale`, `readAccessoryType`, promotion category selection, `runSafely`, input helpers |
| Startup (Taller 1) | `Main` | `main` |
| Returns (R3) | `ProductService` | `restoreStock` |

## Information Expert

**Verdict: violation** (one point). The pattern is applied correctly in the rest of the
audited code.

Correct applications:
- `Sale.calculateSubtotal` and `Sale.calculateTotal` (lines 57–75) only use attributes of
  `Sale`: its products, the discount and the extended warranty cost. Each `Product` gives
  its own price with `getPrice()`. The sale already has all the information, so it does
  not need to ask another class.
- Promotions: in `SaleService.applyBestPromotion` (line 302), the discount is computed by
  `Promotion.calculateDiscount(sale)`, the class that knows the rules of each promotion,
  and it is stored by `Sale.applyDiscount`, because the amount belongs to that sale and
  is needed for its total.

| Field | Content |
|---|---|
| ID | V-01 |
| Pattern | Information Expert |
| Module | Returns (Requirement 3) |
| Evidence | `ProductService.restoreStock` (lines 162–168): `product.setStock(product.getStock() + quantity);` |
| Explanation | The service reads the stock of the product, adds the units and writes the value back. `Product` owns the `stock` attribute and already changes it itself in `decreaseStock`, so it is the expert for increasing it too. |
| Consequence | The rule for changing the stock is split between `Product` (decrease) and `ProductService` (increase). A new stock rule, such as a maximum stock, would have to be added in two places. |
| Proposed solution | Add `Product.increaseStock(int quantity)`, the counterpart of `decreaseStock`, and make `restoreStock` call it. Pattern applied: Information Expert. |
| Other member's code | Yes, Developer 1 (`Product`). |

## Creator

**Verdict: correct.**

- `SaleService.registerSale` creates the sale (line 129:
  `new Sale(nextSaleId(), LocalDate.now(), customer, seller, soldProducts)`). It has the
  data needed to initialize it (customer, seller, items and the next id) and it records
  the sales it creates (line 143: `sales.add(sale)`). Both are conditions of the pattern.
- `Main.main` (lines 41–60) creates the repositories, the services and the menu. It has
  the data needed to build them, such as the paths of the data files.

## Controller

**Verdict: violation.**

`ConsoleMenu` receives the input of the user and delegates the operations to the
services, which is correct, except for one business decision.

| Field | Content |
|---|---|
| ID | V-02 |
| Pattern | Controller |
| Module | User interface and warranties (Requirement 4); validation (Requirement 6) |
| Evidence | The rule "only consoles accept an extended warranty" is decided in three places: `ConsoleMenu.registerSale` (line 277: `if (product instanceof Console && readYesNo(...))`), `SaleService.assignWarranties` (line 311: `if (product instanceof Console)`) and `SaleValidator.validateExtendedWarrantyRequest` (lines 65–85: `product.getCategory().equalsIgnoreCase("CONSOLE")`). |
| Explanation | The menu takes a business decision instead of only capturing the request and delegating it. The same rule is spread across the interface, the service and the validator. |
| Consequence | If the rule changes, several places must be modified, and if one is forgotten the system behaves inconsistently. The rule would also have to be copied into any other interface. |
| Proposed solution | Keep the rule in one single place that decides whether a product accepts an extended warranty, and make `ConsoleMenu`, `SaleService` and `SaleValidator` delegate to it. The class that will own the rule is agreed in the consolidation. |
| Other member's code | Yes, Developer 1 (`SaleValidator`). |

This finding is registered once, so the same rule does not appear in several violations.

## Low Coupling

**Verdict: correct.**

- `SaleRepository.loadAll(products, customers, sellers)` (line 81) receives the objects
  already loaded and searches them with its own private methods (`findCustomer`,
  `findSeller`, `findProduct`, lines 131–156). It only imports classes of the model and
  the exceptions (lines 3–7). If it called `PersonService` or `ProductService`, it would
  depend on other modules and break the layer order.
- `SaleService` has six dependencies and every one is used to register a sale:
  - `SaleRepository`: loads the sales in the constructor (line 75) and saves them in
    `saveSales` (line 354);
  - `PersonService`: lines 76, 227 and 240 (`requireCustomer`, `requireSeller`);
  - `ProductService` and `AccessoryService`: `getCatalog` (lines 214–215), `findItem`
    (lines 341–343) and `updateInventory` (lines 324–326);
  - `PromotionService`: `applyBestPromotion` (line 303);
  - `WarrantyService`: `assignWarranties` (lines 312 and 314).

  Low coupling does not mean the fewest possible dependencies, but no unnecessary ones.
  None of these can be removed.

## High Cohesion

**Verdict: violation** (two points).

Correct application: `ConsoleMenu.runSafely` (line 111) keeps in one place the handling of
the exceptions and their conversion into messages for the user, so this logic is not
repeated in every operation of the menu.

| Field | Content |
|---|---|
| ID | V-03 |
| Pattern | High Cohesion |
| Module | Sales (Taller 1), receipt breakdown (Requirement 2) |
| Evidence | `Sale.generateReceipt()` (lines 105–126) builds Spanish text for the user with currency format (`$%,.2f`) and messages such as "sin promoción aplicable". |
| Explanation | `Sale` is a model class that holds the data of the sale and computes its amounts. Building the receipt is a different responsibility: how the information is shown to the user. |
| Consequence | A change only in the presentation, such as printing the receipt or making a PDF, forces a change in `Sale` even though the calculations did not change. |
| Proposed solution | Move the receipt to a new class `ReceiptGenerator` in the `ui` package. Only `ConsoleMenu` uses the receipt today (lines 284, 305, 314 and 570), so the layers are respected. The receipt must stay exactly the same: data, texts, currency format, order and line breaks. The correction is guided by Pure Fabrication. |
| Other member's code | No. |

| Field | Content |
|---|---|
| ID | V-04 |
| Pattern | High Cohesion |
| Module | User interface (Taller 1 and integrations of Requirements 1–6) |
| Evidence | `ConsoleMenu` (698 lines) holds the seven submenus: `productMenu` (line 127), `personMenu` (183), `saleMenu` (235), `accessoryMenu` (321), `promotionMenu` (421), `warrantyMenu` (494) and `returnMenu` (539), plus the input helpers (`readText`, `readInt`, `readDouble`, lines 620–664) and the error handling. |
| Explanation | Although everything is related to the console, one class handles all the modules of the store, reads the data, prints the listings and handles the errors. |
| Consequence | Every new module or operation makes the class bigger and harder to understand, and changes of different modules happen in the same file. |
| Proposed solution | Split it into `ProductMenu`, `PersonMenu`, `SaleMenu`, `AccessoryMenu`, `PromotionMenu`, `WarrantyMenu` and `ReturnMenu`, and leave `ConsoleMenu` as the main menu that gives access to them. The input helpers that every submenu uses go to their own class. Patterns applied: High Cohesion and Pure Fabrication. |
| Other member's code | No. |

## Polymorphism

**Verdict: violation** (one point).

Correct application: `Promotion.calculateDiscount(sale)` is called in
`SaleService.applyBestPromotion` (line 305) without asking which subclass of `Promotion` it
is. Each subclass computes the discount with its own rules.

The `instanceof Console` checks of the warranty rule are part of V-02 and are not repeated
here.

| Field | Content |
|---|---|
| ID | V-05 |
| Pattern | Polymorphism |
| Module | Accessories integration in sales (Requirement 1) |
| Evidence | `SaleService.updateInventory` (lines 321–329): `if (entry.getKey() instanceof Accessory) { accessoryService.updateStock(...); } else { productService.updateStock(...); }` |
| Explanation | The service asks for the concrete type of the item to decide which service updates its inventory. |
| Consequence | A new kind of product with its own inventory would require another case in `updateInventory`, and `SaleService` has to know every concrete type. |
| Proposed solution | Update the inventory through a common abstraction, so `SaleService` does not need to know whether the item is an `Accessory` or another type. The exact design, and whether it needs changes in classes of other members, is agreed in the consolidation. |
| Other member's code | To be confirmed in the consolidation. |

## Pure Fabrication

**Verdict: correct.**

- `Main` is a class that does not represent anything of the business. It exists to create
  the repositories, the services and the menu and to connect them (lines 41–60) before
  the program starts (line 63). Some part of the program has to know how the components
  are built, and keeping it in `Main` takes that technical responsibility away from the
  domain classes.
- The corrections of V-03 and V-04 also apply this pattern (`ReceiptGenerator` and the
  input helper class).
