# Leader SOLID Audit — GameZone Unicesar

Author: Rafael Junior Acosta Mendoza (Technical Leader). Audited code: `develop` at commit
`675e61c`.

## Scope

| Module (requirement) | Class | Members audited |
|---|---|---|
| Sales (Taller 1), discount (R2), warranty cost (R4), R7 | `Sale` | constructor, `calculateSubtotal`, `calculateTotal`, `applyDiscount`, `addExtendedWarrantyCost`, `countUnitsOf`, `findProductById`, getters and setters. `canBeReturned` is audited by Developer 1. |
| Sales (Taller 1) | `SaleRepository` | `saveAll`, `loadAll`, `toLine`, `fromLine`, `findCustomer`, `findSeller`, `findProduct` |
| Sales (Taller 1), integrations of R1, R2, R4, R5, R7 | `SaleService` | constructor, `registerSale` and its private steps, `listAllSales`, `findById`, `findSalesByCustomer`, `findSalesBySeller`, `getCatalog`, `findItem`, `catalogOf`, `nextSaleId` |
| User interface (Taller 1, R6, R7) | `ConsoleMenu`, `Submenu`, `ProductMenu`, `PersonMenu`, `SaleMenu`, `AccessoryMenu`, `PromotionMenu`, `WarrantyMenu`, `ReturnMenu`, `ConsoleInput`, `ReceiptGenerator` | every method; these classes were created or changed by me in the GRASP correction |
| Startup (Taller 1) | `Main` | `main` |
| Returns (R3, R7) | `ProductService` | `restoreStock` |

## Single Responsibility Principle

**Verdict: violation** (two points).

Correct applications:

- `Sale` keeps the data of one sale and its amounts. After the GRASP correction it no longer
  builds the receipt, so its only reason to change is a change in the model of a sale.
- `SaleRepository` reads and writes `sales.csv`: `toLine`, `fromLine` and the three private
  look-ups that rebuild the references all belong to the same purpose, storing and recovering
  sales.
- `ConsoleInput` only reads and checks what the user types; it changes only if the way the
  input is captured changes.
- `ProductService.restoreStock` finds the product, delegates the increase to
  `Product.increaseStock` and saves; the calculation of the new stock is no longer in the
  service.

| Field | Content |
|---|---|
| ID | V-01 |
| Principle | Single Responsibility |
| Module | Sales (Taller 1), accessories integration (R1) |
| Evidence | `SaleService.getCatalog` (lines 214–218) builds the catalogue by joining `productService.listAllProducts()` and `accessoryService.listAllAccessories()`, and `SaleService.findItem` (line 353) searches the same two catalogues one after the other. |
| Explanation | Registering and querying sales are the responsibility of this class. Knowing how the store catalogue is made of products and accessories, and how to search it, is a different responsibility. |
| Consequence | A change in how the catalogue is built or searched, for example a third kind of item, forces a change in `SaleService` even when the sale process does not change. |
| Proposed solution | Move the building and searching of the catalogue to a separate class, and let `SaleService` use it. |
| Other member's code | No. |

| Field | Content |
|---|---|
| ID | V-02 |
| Principle | Single Responsibility |
| Module | User interface (R7) |
| Evidence | `ReceiptGenerator.generate` writes money with `String.format("...$%,.2f...")` in lines 35, 37, 39, 44 and 45. The same money format is copied in about ten places of the system, and this copy is the only one without `Locale.US`, so it uses the locale of the computer. |
| Explanation | `ReceiptGenerator` should build the receipt, but it also decides the money format by itself. The money format has no single owner in the system. |
| Consequence | Receipts and other screens can show amounts with different formats, and changing the format means editing many classes. |
| Proposed solution | A common component that formats money, used by every place. It must keep the current output of each place: it must not force `Locale.US` on the receipt, because on a computer configured in Spanish that would change what the user sees, which Requirement 8 forbids. |
| Other member's code | Yes, Developer 1: his V-03 reports the same format in his classes, so it is registered once in the consolidation and each member corrects his own classes. |

## Open/Closed Principle

**Verdict: violation** (one point).

Correct application: the `switch` of `ConsoleMenu.start` (lines 73–80) turns the option chosen
by the user into the submenu to open. Adding an eighth module means adding one option to the
main menu, which is the expected change of a navigation class; it does not choose a business
rule by the type of an object, and no unrelated class has to be edited.

| Field | Content |
|---|---|
| ID | V-03 |
| Principle | Open/Closed |
| Module | Exception handling (R6), moved to `Submenu` in R7 |
| Evidence | `Submenu.runSafely` catches the four exceptions one by one: `ResourceNotFoundException` (line 43), `BusinessRuleException` (line 45), `InvalidDataException` (line 47) and `PersistenceException` (line 49). It never catches `GameZoneException`, the abstract root they already extend. |
| Explanation | A new kind of system error is added as a new subclass of `GameZoneException`, but `runSafely` has to be edited to handle it. The class is not closed for that change. |
| Consequence | If a fifth exception is created and nobody edits `Submenu`, the error is not turned into a message and the user sees a stack trace. |
| Proposed solution | Keep the four catch blocks and add at the end `catch (GameZoneException e)` with a general message. The four current messages do not change, and a new exception is handled without editing `Submenu`. |
| Other member's code | No. Reported also by Developer 1 for the consolidation. |

## Liskov Substitution Principle

**Verdict: correct.**

- My only hierarchy is `Submenu` (line 15) and its seven submenus (`ProductMenu`, `PersonMenu`,
  `SaleMenu`, `AccessoryMenu`, `PromotionMenu`, `WarrantyMenu`, `ReturnMenu`). All of them
  implement `show()` and `ConsoleMenu` calls them the same way, without a special case for any
  of them. Any submenu can replace the base type.
- `PersonMenu.listSellers()` is an extra method. It extends what `PersonMenu` can do but does
  not stop it from working as a `Submenu`, so it does not break substitution. That `SaleMenu`
  depends directly on `PersonMenu` to call it is a dependency problem, recorded in the Interface
  Segregation section (V-05).
- The verdict is *correct* and not *not applicable*, because the hierarchy exists and was
  checked.

## Interface Segregation Principle

**Verdict: violation** (two points).

| Field | Content |
|---|---|
| ID | V-04 |
| Principle | Interface Segregation |
| Module | Correction of R7 (V-07), sales |
| Evidence | `SaleService` holds `List<InventoryCatalog> inventoryCatalogs` (line 52) and only calls `findById` (line 337, in `catalogOf`) and `updateStock` (line 324). It never calls `restoreStock`, which the interface also declares. |
| Explanation | `SaleService` depends on an operation it does not use. |
| Consequence | Any change in `restoreStock` or a new operation added for the return module also reaches the sale module. |
| Proposed solution | Split the interface into smaller contracts (one to find items, one to update the stock, one to restore it), so `SaleService` depends only on the operations it uses. |
| Other member's code | Yes, Developer 2: `InventoryCatalog` is his and his V-05 reports the same problem from the return side, so it is registered once in the consolidation. |

| Field | Content |
|---|---|
| ID | V-05 |
| Principle | Interface Segregation |
| Module | User interface (R7) |
| Evidence | `SaleMenu` receives a whole `ProductService` (line 19) and only calls `findById` (line 80). It also receives a concrete `PersonMenu` (line 20) only to call `listSellers()` (line 64). |
| Explanation | The menu depends on two classes that offer much more than it uses. |
| Consequence | A change in any method of `ProductService` or `PersonMenu` is a reason to review `SaleMenu`, even when the two operations it uses did not change. |
| Proposed solution | Let `SaleMenu` depend on small contracts with only the operations it uses, for example a product finder with `findById` and a seller lister with `listSellers`. |
| Other member's code | Yes, Developer 1: `ProductService` is his and would implement the small contract. |

## Dependency Inversion Principle

**Verdict: violation** (two points).

Correct application: `Main` (lines 41–63) is the composition root. It is the place that has to
know and create the concrete repositories and services with `new`, and connect them; the
classes inside the system are the ones that should depend on abstractions.

| Field | Content |
|---|---|
| ID | V-06 |
| Principle | Dependency Inversion |
| Module | Sales (Taller 1), integrations of R1, R2, R4 |
| Evidence | `SaleService` declares six fields with concrete classes (lines 44–49): `SaleRepository`, `ProductService`, `PersonService`, `AccessoryService`, `PromotionService` and `WarrantyService`. Only `inventoryCatalogs` (line 52) uses an abstraction. |
| Explanation | A high level service depends on the concrete classes of the storage and of the other modules instead of on abstractions. |
| Consequence | A change in any of those six classes is a reason to recompile and retest `SaleService`, and the service cannot be used with another implementation. |
| Proposed solution | Small interfaces built from what `SaleService` needs (store sales, find people, find and update items, find the best promotion, assign warranties), not copies of every method of each service, so the correction does not create a new Interface Segregation violation. |
| Other member's code | Yes: the services that would implement the interfaces belong to Developer 1 (`ProductService`) and Developer 2 (`PersonService`, `AccessoryService`, `PromotionService`, `WarrantyService`). Developer 2 reports the same kind of problem in `ReturnService` (his V-06). |

| Field | Content |
|---|---|
| ID | V-07 |
| Principle | Dependency Inversion |
| Module | User interface (R7) |
| Evidence | `ConsoleMenu` creates its collaborators with `new`: `new ConsoleInput()` and the seven submenus (lines 46–53). |
| Explanation | `ConsoleMenu` is not the composition root. By creating the submenus itself it depends on all their concrete classes and their constructors. |
| Consequence | Any change in how a submenu is built forces a change in `ConsoleMenu`, and the main menu cannot be built with other submenus. |
| Proposed solution | Build the submenus in `Main`, the composition root, and pass them to `ConsoleMenu` through its constructor. `ConsoleMenu` only coordinates the navigation between the submenus it receives. |
| Other member's code | No. |

## Summary

| Principle | Verdict | Findings |
|---|---|---|
| Single Responsibility | Violation | V-01, V-02 |
| Open/Closed | Violation | V-03 |
| Liskov Substitution | Correct | — |
| Interface Segregation | Violation | V-04, V-05 |
| Dependency Inversion | Violation | V-06, V-07 |

Boundary findings for the consolidation: V-02 (Developer 1, his V-03), V-04 (Developer 2, his
V-05), V-05 and V-06 (services of Developers 1 and 2).
