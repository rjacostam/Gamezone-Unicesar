# Consolidated SOLID Audit — GameZone Unicesar

Consolidated by: Rafael Junior Acosta Mendoza (Technical Leader).

Audited code: `develop` at commit `675e61c`, with the GRASP correction of Requirement 7
already merged. The three individual reports use this commit:

- `docs/solid-audit/leader-audit.md` (cited as *leader*);
- `docs/solid-audit/developer1-audit.md` (cited as *dev1*);
- `docs/solid-audit/developer2-audit.md` (cited as *dev2*).

## Coverage

The system has 61 classes. Each one was audited once by its owner. The owner of the code
added in Requirement 7 comes from `docs/grasp-refactor-log.md`.

| Package | Class | Audited by |
|---|---|---|
| (root) | `Main` | Technical Leader |
| `ui` | `ConsoleMenu`, `Submenu`, `ProductMenu`, `PersonMenu`, `SaleMenu`, `AccessoryMenu`, `PromotionMenu`, `WarrantyMenu`, `ReturnMenu`, `ConsoleInput`, `ReceiptGenerator` | Technical Leader |
| `ui` | `ReturnReceiptFormatter`, `WarrantyCertificateFormatter` | Developer 1 |
| `model` | `Sale` (all members except `canBeReturned`) | Technical Leader |
| `model` | `Sale.canBeReturned` | Developer 1 |
| `model` | `Product`, `VideoGame`, `Console`, `Accessory`, `Controller`, `Cable`, `Memory` | Developer 1 |
| `model` | `Promotion`, `PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount` | Developer 1 |
| `model` | `Warranty`, `BasicWarranty`, `ExtendedWarranty`, `Return` | Developer 1 |
| `model` | `Person`, `Customer`, `Seller` | Developer 2 |
| `persistence` | `SaleRepository` | Technical Leader |
| `persistence` | `ProductRepository` | Developer 1 |
| `persistence` | `PersonRepository`, `AccessoryRepository`, `PromotionRepository`, `ReturnRepository`, `WarrantyRepository`, `WarrantyRecord` | Developer 2 |
| `service` | `SaleService` | Technical Leader |
| `service` | `ProductService` (all members except `restoreStock`) | Developer 1 |
| `service` | `ProductService.restoreStock` | Technical Leader |
| `service` | `PersonService`, `AccessoryService`, `PromotionService`, `ReturnService`, `WarrantyService`, `InventoryCatalog` | Developer 2 |
| `exceptions` | the five exception classes | Developer 1 |
| `validation` | the seven validators | Developer 1 |

No class was left without an audit and none was audited twice. In the review of the pull
requests, three claims about code of other members were returned to their authors and removed:
Developer 2 had listed classes of Developer 1 and of the Technical Leader as correct
applications, and Developer 1 had cited the removed `Sale.generateReceipt` and the owner of
`ProductRepository` wrongly.

## Team verdict per principle

| Principle | Leader | Dev1 | Dev2 | Team verdict | Final violations |
|---|---|---|---|---|---|
| Single Responsibility | Violation | Violation | Violation | **Violation** | V-01, V-02, V-03 |
| Open/Closed | Violation | Violation | Violation | **Violation** | V-04, V-05, V-06 |
| Liskov Substitution | Correct | Violation (V-01, registered under ISP) | Violation | **Violation** | V-07 |
| Interface Segregation | Violation | Violation | Violation | **Violation** | V-08, V-09, V-10 |
| Dependency Inversion | Violation | Violation (V-01, registered under ISP) | Violation | **Violation** | V-11, V-12, V-13 |

The 17 violations of the three reports become 13 after merging the ones that describe the same
defect (C1 to C4 below). A violation that breaks more than one principle is registered once,
under the principle that guides its correction, and the others are named inside it.

## Final list of violations

Full evidence, explanation and consequence of each one are in the report of origin; the agreed
solution and the responsible members are fixed here.

| ID | Principle | Evidence | Explanation and consequence | Agreed solution | Origin | Responsible |
|---|---|---|---|---|---|---|
| V-01 | SRP | `SaleService.getCatalog` (lines 214–218) and `findItem` (line 353) build and search the product and accessory catalogues. | The sale service also owns the store catalogue; a change in how the catalogue is built or searched changes `SaleService`. | Move the building and searching of the catalogue to its own class used by `SaleService`. | leader V-01 | Technical Leader |
| V-02 | SRP | The money format `$%,.2f` is copied in about ten places: `Accessory.getDescription`, the two formatters of Developer 1, the three promotions (percentage format) and `ReceiptGenerator` (lines 35–45). Nine use `Locale.US`; `ReceiptGenerator` uses the locale of the computer. | The money and percentage formats have no owner; changing them means editing many classes in three packages. | One common component for money and percentage formats, used by every place, **keeping the current output of each place**: the nine places keep `Locale.US` and the receipt keeps the locale of the computer. Forcing `Locale.US` on the receipt would change what the user sees, so that difference is registered as OOS-02. | leader V-02, dev1 V-03 | Developer 1 (creates the component, his classes), Technical Leader (`ReceiptGenerator`) |
| V-03 | SRP | `ReturnService` manages returns (`registerReturn`, three queries) and also builds the monthly report (`calculateMonthlySales` line 443, `calculateMonthlyReturns` line 466, `generateMonthlyBalance` line 508). | Two reasons to change: return rules and the financial report. | Move the three report methods to a `MonthlyBalanceService`. | dev2 V-01 | Developer 2 |
| V-04 | OCP | `Submenu.runSafely` catches the four exceptions one by one (lines 43–49) and never `GameZoneException`. | A fifth exception means editing `Submenu`; if forgotten, the user sees a stack trace. | Keep the four catch blocks and add a final `catch (GameZoneException e)` with a general message. | leader V-03 (also reported by dev1) | Technical Leader |
| V-05 | OCP | "Is this a console" has two independent hard-coded answers in `Console`: `isConsole()` returns `true` and `getCategory()` returns `"CONSOLE"` (`Product` lines 43 and 62). | A new kind of console must change both; if only one is changed, the rules disagree. | Derive one answer from the other, from a single constant. | dev1 V-02 | Developer 1 |
| V-06 | OCP | The kinds of accessory and promotion are written as code twice: one `register...` method per kind in `AccessoryService` (lines 77, 106, 136) and `PromotionService` (lines 61, 89, 116), and one branch per kind in `AccessoryRepository.deserialize` (lines 183–191) and `PromotionRepository.deserialize` (lines 184–191). | A new kind is an edit of the service and of the repository; a missed branch writes a record that cannot be read back. | A factory per hierarchy and a single `register` method per service; `deserialize` uses the same factory. The menus keep their options and messages. | dev2 V-02, dev2 V-03 | Developer 2 (factories, services, repositories), Developer 1 (constructors of the model if needed), Technical Leader (`AccessoryMenu`, `PromotionMenu`) |
| V-07 | LSP | `InventoryCatalog.findById` has two behaviours: `ProductService.findById` (lines 128–135) neither trims nor guards null or blank; `AccessoryService.findById` (lines 209–220) does both. | A caller of the interface gets different answers for the same identifier. | Both implementations follow one contract: trim and answer `null` for a null or blank identifier. Every caller already sends trimmed identifiers, so the output does not change. | dev2 V-04 | Developer 1 (`ProductService`) |
| V-08 | ISP | `Product.getExtraFields()` (line 54) is one contract for the columns of two different files: `VideoGame` and `Console` answer the columns of `products.csv`, the accessories those of `accessories.csv`, and `Promotion` the same for `promotions.csv`. Read by `ProductRepository` (line 56) and `AccessoryRepository` (line 141). | One contract with two meanings; the domain knows the column layout of the files. It also breaks LSP, SRP and DIP (see dev1 V-01). Registered as a new violation, not as a part of GRASP V-08: the principle is different, but its cause is the GRASP correction V-08, which moved the columns into the model. | Separate the contracts so each hierarchy declares only the file it is stored in, and the layout is owned by the persistence layer. | dev1 V-01 | Developer 1 (model and `ProductRepository`), Developer 2 (`AccessoryRepository`, `PromotionRepository`) |
| V-09 | ISP | `InventoryCatalog` declares `findById`, `updateStock` and `restoreStock`; `SaleService` uses only `findById` and `updateStock` (lines 324, 337), `ReturnService` only `findById` and `restoreStock`. | Each client depends on an operation it never uses. | Split it into a finder, a stock updater and a stock restorer. | leader V-04, dev2 V-05 | Developer 2 (interfaces, `AccessoryService`, `ReturnService`), Developer 1 (`ProductService`), Technical Leader (`SaleService`) |
| V-10 | ISP | `SaleMenu` receives a whole `ProductService` (line 19) only for `findById` (line 80) and a concrete `PersonMenu` (line 20) only for `listSellers` (line 64). | The menu depends on much more than it uses. | Depend on small contracts: the finder that comes from V-09 and a seller lister. | leader V-05 | Technical Leader |
| V-11 | DIP | `SaleService` (lines 44–49) and `ReturnService` (lines 53–57) hold their repositories and other services as concrete classes. | High level services depend on concrete classes of the storage and of other modules. The fields `ProductService` and `AccessoryService` are already covered by V-01 and V-09, so this violation covers the rest: the two repositories, `PersonService`, `PromotionService`, `WarrantyService` and the `SaleService` used by `ReturnService`. | Small contracts built from what each service needs (store sales and returns, find sales, find people, choose the best promotion, assign warranties, cancel warranties), not copies of whole classes, so no new ISP violation appears. | leader V-06, dev2 V-06 | Technical Leader (contracts implemented by `SaleService`/`SaleRepository`, and `SaleService` fields), Developer 2 (contracts implemented by his services and `ReturnRepository`, and `ReturnService` fields) |
| V-12 | DIP | `AccessoryService` holds the concrete `ProductService` and uses it only for `findById` (lines 309 and 328). | The accessory module is compiled and tested against the catalogue module. | Depend on the finder contract that comes from V-09. | dev2 V-07 | Developer 2 |
| V-13 | DIP | `ConsoleMenu` creates `ConsoleInput` and the seven submenus with `new` (lines 46–53); it is not the composition root. | Any change in how a submenu is built changes `ConsoleMenu`. | Build the submenus in `Main` and pass them to `ConsoleMenu`, which receives them as `Submenu`, so the dependency is really inverted. | leader V-07 | Technical Leader |
