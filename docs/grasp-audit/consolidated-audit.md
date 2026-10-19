# Consolidated GRASP Audit — GameZone Unicesar

Consolidated by: Rafael Junior Acosta Mendoza (Technical Leader).

Audited code: `develop` at commit `6c10ce2`. The three individual reports use this same
commit, so every line number below refers to it.

Source reports:

- `docs/grasp-audit/leader-audit.md` (cited as *leader*);
- `docs/grasp-audit/developer1-audit.md` (cited as *dev1*);
- `docs/grasp-audit/developer2-audit.md` (cited as *dev2*).

## Coverage

The system has 48 classes. Each one was audited by its owner, once. Two classes were split
by method, following the responsibility table of Requirement 7.

| Package | Class | Audited by |
|---|---|---|
| (root) | `Main` | Technical Leader |
| `ui` | `ConsoleMenu` | Technical Leader |
| `model` | `Sale` (all methods except `canBeReturned`) | Technical Leader |
| `model` | `Sale.canBeReturned` | Developer 1 |
| `model` | `Product`, `VideoGame`, `Console` | Developer 1 |
| `model` | `Accessory`, `Controller`, `Cable`, `Memory` | Developer 1 |
| `model` | `Promotion`, `PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount` | Developer 1 |
| `model` | `Warranty`, `BasicWarranty`, `ExtendedWarranty` | Developer 1 |
| `model` | `Return` | Developer 1 |
| `model` | `Person`, `Customer`, `Seller` | Developer 2 |
| `persistence` | `SaleRepository` | Technical Leader |
| `persistence` | `ProductRepository` | Developer 1 |
| `persistence` | `PersonRepository`, `AccessoryRepository`, `PromotionRepository`, `ReturnRepository`, `WarrantyRepository`, `WarrantyRecord` | Developer 2 |
| `service` | `SaleService` | Technical Leader |
| `service` | `ProductService` (all methods except `restoreStock`) | Developer 1 |
| `service` | `ProductService.restoreStock` | Technical Leader |
| `service` | `PersonService`, `AccessoryService`, `PromotionService`, `ReturnService`, `WarrantyService` | Developer 2 |
| `exceptions` | `GameZoneException`, `ResourceNotFoundException`, `BusinessRuleException`, `InvalidDataException`, `PersistenceException` | Developer 1 |
| `validation` | the seven validators | Developer 1 |

No class was left without an audit and none was audited twice. During the review of the pull
requests, two overlaps were found and returned to their author before merging:
Developer 2 had included `Main` and `ConsoleMenu.runSafely`, which belong to the Technical
Leader, and an opinion on the validators, which belong to Developer 1. Both were removed from
`developer2-audit.md`.

## Team verdict per pattern

A pattern is a violation for the team when at least one report registers a violation of it
that survives the consolidation.

| Pattern | Leader | Dev1 | Dev2 | Team verdict | Final violations |
|---|---|---|---|---|---|
| Information Expert | Violation | Violation | Violation | **Violation** | V-01, V-02, V-03 |
| Creator | Correct | Correct | Correct | **Correct** | — |
| Controller | Violation | Not applicable | Correct | **Violation** | V-04 |
| Low Coupling | Correct | Violation | Violation | **Violation** | V-07, V-08 |
| High Cohesion | Violation | Violation | Correct | **Violation** | V-05, V-06 |
| Polymorphism | Violation | Violation | Violation | **Violation** | V-07, V-08, V-09 |
| Pure Fabrication | Correct | Correct | Correct | **Correct** | — |
| Indirection | Correct | Correct | Correct | **Correct** | — |
| Protected Variations | Correct | Violation | Correct | **Violation** | V-04 |

Some violations break more than one pattern. Each one is registered once, under the pattern
that guides its correction, and the other patterns are named inside it.

## Final list of violations

The 13 violations of the three reports become 9 after merging the ones that describe the same
defect. "Responsible" follows the rule of Requirement 7: each member corrects the part of the
code that is his.

### V-01 — Information Expert: the stock is increased outside `Product`

| Field | Content |
|---|---|
| Evidence | `ProductService.restoreStock` (lines 162–168): `product.setStock(product.getStock() + quantity);` (line 166). |
| Explanation | `Product` owns the stock and already decreases it in `decreaseStock`, but the increase is done by the service. |
| Consequence | A new stock rule has to be written in two places. |
| Agreed solution | Add `Product.increaseStock(int quantity)` and make `restoreStock` call it. |
| Origin | leader V-01 |
| Responsible | Developer 1 (`Product.increaseStock`), then Technical Leader (`restoreStock`). |

### V-02 — Information Expert: two promotions rebuild the subtotal

| Field | Content |
|---|---|
| Evidence | `PercentageDiscount.calculateDiscount` (lines 39–42) and `BulkPurchaseDiscount.calculateDiscount` (lines 47–50) add the price of every unit, a value that `Sale.getSubtotal` (line 193) already publishes. |
| Explanation | The base of the discount is knowledge of `Sale`. |
| Consequence | If the subtotal ever includes something else, the discount and the receipt use different bases. |
| Agreed solution | Read `sale.getSubtotal()` in those two promotions. `CategoryDiscount` stays as it is, because it only adds the products of its category. |
| Origin | dev1 V-01 |
| Responsible | Developer 1. |

### V-03 — Information Expert: the products of a sale are counted and searched from outside

| Field | Content |
|---|---|
| Evidence | `ReturnService.countUnits` (lines 247–259), `ReturnService.findProduct` (lines 347–359) and `ReturnRepository.findProduct` (lines 295–304) walk `sale.getProducts()`. |
| Explanation | How many units of a product a sale has, and which product carries an id, is knowledge of `Sale`. The same search also exists twice. |
| Consequence | A change in how `Sale` stores its units silently breaks the partial return rule. |
| Agreed solution | Add `Sale.countUnitsOf(String productId)` and `Sale.findProductById(String productId)`, and make the three helpers call them. |
| Origin | dev2 V-01, dev2 R-01 |
| Responsible | Technical Leader (`Sale`), then Developer 2 (`ReturnService`, `ReturnRepository`). |

### V-04 — Controller: the rule "only consoles" is decided in five places

| Field | Content |
|---|---|
| Evidence | `ConsoleMenu.registerSale` (line 277), `SaleService.assignWarranties` (line 311), `ReturnService.cancelWarrantiesOfConsoles` (line 179), `SaleValidator.validateExtendedWarrantyRequest` (line 74) and `AccessoryValidator.validateCompatibleConsole` (line 62). |
| Explanation | The menu takes a business decision, and the same rule is repeated in the interface, two services and two validators. It also breaks Protected Variations, because a change in how a console is identified reaches every layer. |
| Consequence | Changing the rule means editing five places; forgetting one makes the system answer differently depending on the operation. |
| Agreed solution | One answer in the domain: a method on `Product` that tells whether the product is a console. Every place delegates to it. |
| Origin | leader V-02, dev1 V-05, and the line 179 of dev2 V-03 |
| Responsible | Developer 1 (method on `Product`, `SaleValidator`, `AccessoryValidator`), Technical Leader (`ConsoleMenu`, `SaleService`), Developer 2 (`ReturnService`). |

### V-05 — High Cohesion: model classes build the text that the user reads

| Field | Content |
|---|---|
| Evidence | `Sale.generateReceipt` (lines 105–126), `Return.generateReturnReceipt` (line 119), `Warranty.generateWarrantyCertificate` (line 123), `Product.getDescription` (line 33) and `Accessory.getDescription` (line 65). |
| Explanation | The model holds data and rules, but it also decides the wording and the layout of documents in Spanish. |
| Consequence | A change in the presentation forces changes in domain classes. |
| Agreed solution | Move each document to a formatter in the `ui` package (Pure Fabrication). The output must stay identical, character by character. |
| Origin | leader V-03, dev1 V-03 |
| Responsible | Technical Leader (`Sale` and its `ReceiptGenerator`, and the calls in `ConsoleMenu`), Developer 1 (`Return`, `Warranty`, `Product`, `Accessory` and their formatters). |

### V-06 — High Cohesion: `ConsoleMenu` handles the seven modules

| Field | Content |
|---|---|
| Evidence | `ConsoleMenu` (698 lines) holds the seven submenus (lines 127, 183, 235, 321, 421, 494, 539) and the input helpers (lines 620–664). |
| Explanation | One class reads, prints and handles errors for every module of the store. |
| Consequence | Every new module makes the class bigger, and changes of different modules happen in the same file. |
| Agreed solution | One submenu class per module, `ConsoleMenu` as the main menu, and the input helpers in their own class. |
| Origin | leader V-04 |
| Responsible | Technical Leader. |

### V-07 — Polymorphism: the services choose by type who owns the stock of an item

| Field | Content |
|---|---|
| Evidence | `SaleService.updateInventory` (lines 321–329, `instanceof Accessory` at line 323) and `ReturnService.restoreStock` (lines 310–323, `instanceof Accessory` at line 317). `ReturnService` also holds five collaborators (lines 55–59) because of it. |
| Explanation | The services ask for the concrete type to decide which service updates the stock. It also breaks Low Coupling, because each service has to know every catalogue. |
| Consequence | A new kind of item needs a new branch in both services. |
| Agreed solution | A common interface for the catalogues that own stock, implemented by `ProductService` and `AccessoryService`, so the services ask the catalogue instead of the type. |
| Origin | leader V-05, dev2 V-02, dev2 V-03 (line 317) |
| Responsible | Developer 2 (the interface, `AccessoryService`, `ReturnService`), Developer 1 (`ProductService`), Technical Leader (`SaleService`). |

### V-08 — Polymorphism: the repositories derive the type marker with `instanceof` chains

| Field | Content |
|---|---|
| Evidence | `ProductRepository.saveAll` (lines 50 and 60), `AccessoryRepository.serialize` (lines 141–147), `PromotionRepository.serialize` (lines 141–149) and `getTypeOf` (lines 222–225), `WarrantyRepository.getTypeOf` (line 210). |
| Explanation | The repositories restate the class hierarchy as a chain of tests. It also breaks Low Coupling and Protected Variations, because the persistence layer depends on every concrete type. |
| Consequence | A new type means editing the repository, and a missed branch saves it with the wrong marker. |
| Agreed solution | Each model hierarchy publishes its own type marker, and the repositories write it without asking for the type. The file formats do not change. |
| Origin | dev1 V-02, dev2 V-04, dev2 R-05 |
| Responsible | Developer 1 (marker methods on `Product`, `Accessory`, `Promotion`, `Warranty`, and `ProductRepository`), Developer 2 (`AccessoryRepository`, `PromotionRepository`, `WarrantyRepository`). |

### V-09 — Polymorphism: the warranty coverage is chosen by comparing a name

| Field | Content |
|---|---|
| Evidence | `Warranty.coverageDescription` (lines 155–156): `if ("Garantía Extendida".equals(getWarrantyType()))`. |
| Explanation | The base class finds out the type by comparing the text that the subclass returns. |
| Consequence | A new warranty type needs a new `if` in the parent class. |
| Agreed solution | Make the coverage an abstract method answered by each subclass. |
| Origin | dev1 V-04 |
| Responsible | Developer 1. |
