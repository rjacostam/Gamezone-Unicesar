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
