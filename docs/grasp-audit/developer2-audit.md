# Developer 2 GRASP Audit — GameZone Unicesar

Auditor: José David Cervantes Solano (Developer 2 — People, Accessories, Promotions,
Warranties and Returns, plus the repositories and the exception and validation layer).

Branch: `feature/grasp-audit`. The evidence refers to the state of `develop` at the
moment of this audit.

Every verdict below is about the code that is my responsibility. Problems that were found
in code owned by another member are listed in
[Findings reported for the consolidation](#findings-reported-for-the-consolidation)
instead of being audited here.

---

## Scope

| Module | Requirement | Classes audited | Members audited |
|---|---|---|---|
| People | Taller 1 (R1) | `Person`, `Customer`, `Seller`, `PersonRepository`, `PersonService` | `getRole`, `getSummary`, `toString`, `setEmail`, `saveCustomers`, `loadCustomers`, `saveSellers`, `loadSellers`, `registerCustomer`, `listCustomers`, `listSellers`, `findCustomerById`, `findSellerById`, `people`, `withCustomer` |
| Accessories | R1 | `AccessoryRepository`, `AccessoryService` | `saveAll`, `loadAll`, `serialize`, `deserialize`, `registerController`, `registerCable`, `registerMemory`, `listAllAccessories`, `listAccessoriesByType`, `findAccessoriesCompatibleWith`, `findById`, `updateStock`, `restoreStock` |
| Promotions | R2 | `PromotionRepository`, `PromotionService` | `saveAll`, `loadAll`, `serialize`, `deserialize`, `registerPercentageDiscount`, `registerCategoryDiscount`, `registerBulkPurchaseDiscount`, `listAllPromotions`, `listActivePromotions`, `findBestPromotionFor`, `findById` |
| Warranties | R4 | `WarrantyRepository`, `WarrantyService`, `WarrantyRecord` | `saveAll`, `loadAll`, `toRecord`, `readQuietly`, `loadWarranties`, `toWarranty`, `assignBasicWarranty`, `assignExtendedWarranty`, `cancelWarranties`, `removeOneWarranty`, `findWarrantyByProduct`, `covers`, `listAllWarranties`, `listActiveWarranties`, `listWarrantiesExpiringSoon` |
| Returns | R3 | `ReturnRepository`, `ReturnService` | `saveAll`, `loadAll`, `registerReturn`, `resolveReturnedProducts`, `countUnits`, `countReturnedUnits`, `restoreStock`, `cancelWarrantiesOfConsoles`, `viewAllReturns`, `viewReturnsByCustomer`, `viewReturnsBySale`, `calculateMonthlySales`, `calculateMonthlyReturns`, `generateMonthlyBalance` |
| Exception and validation layer | R6 | the seven repositories, the seven services, `ConsoleMenu.runSafely`, `Main` | every public repository method, every `validate...` delegation, the two `catch` blocks |

Not audited here: `Product`, `VideoGame`, `Console`, `Accessory`, `Controller`, `Cable`,
`Memory`, `Promotion`, `PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount`,
`BasicWarranty`, `ExtendedWarranty` (Developer 1), `Sale`, `SaleService`, `SaleRepository`,
`ConsoleMenu` except `runSafely`, `Main` except the startup catch, and the exception and
validator classes themselves (Developer 1).

---

## Summary

| Pattern | Violation | Correct | Not applicable |
|---|---|---|---|
| Information Expert | 1 | 4 | 0 |
| Creator | 0 | 5 | 0 |
| Controller | 0 | 4 | 0 |
| Low Coupling | 1 | 3 | 0 |
| High Cohesion | 0 | 6 | 0 |
| Polymorphism | 2 | 2 | 0 |
| Pure Fabrication | 1 | 3 | 0 |
| Indirection | 0 | 4 | 0 |
| Protected Variations | 0 | 6 | 0 |

Five findings: V-01 to V-05, plus five problems reported for the consolidation.

---

## Information Expert

**Verdict: violation.**

### V-01 — Classes other than the owner answer questions about sales, returns and warranties

| Field | Content |
|---|---|
| ID | V-01 |
| Pattern | Information Expert |
| Module | Returns (R3) and Warranties (R4) |
| Evidence | `ReturnService.countUnits(Sale sale, String productId)` (lines 247-259) walks `sale.getProducts()` and counts how many units of a product the sale contains. `ReturnService.findProduct(Sale sale, String productId)` (lines 347-359) answers which product of the sale carries an identifier. `ReturnService.isReturnOf(Sale sale, Return registeredReturn)` (lines 335-338) and `isReturnOfSaleId` (lines 447-452) decide whether a return comes from a sale. `ReturnRepository.findProduct(Sale sale, String productId)` (lines 295-304) repeats the same search inside the persistence layer. `WarrantyService.covers(Warranty warranty, String productId, String saleId)` (lines 248-256) decides whether a warranty covers a product in a sale. |
| Explanation | Every one of these questions is asked of the class that owns the answer, but it is answered outside of it. How many units of a product a sale contains is knowledge of `Sale`; which product of a sale carries an identifier is knowledge of `Sale`; whether a return comes from a sale is knowledge of `Return`, which already stores the sale and already publishes it through `getSale()`; whether a warranty covers a product in a sale is knowledge of `Warranty`, which stores both links. The six methods walk `getProducts()`, `getId()` and `getSale()` of objects that could answer themselves, so the knowledge of the domain is spread across the services and the repositories instead of living in the domain. |
| Consequence | If `Sale` ever stops storing one entry per unit and starts summarising quantities, `countUnits` silently returns a wrong count and the partial return rule accepts more units than the customer bought. If `Return` or `Warranty` changes how it links to its sale, the matching logic has to be found in three different classes. The same question is answered twice, once in `ReturnService` and once in `ReturnRepository`, and the two copies have no way of being kept in step. |
| Proposed solution | Add the answers to the classes that own the data: `countUnitsOf(String productId)` and `findProductById(String productId)` on `Sale`, and `comesFromSale(String saleId)` on `Return` and on `Warranty`. The six helpers then become one-line calls and the knowledge has a single place. The behaviour does not change, only where it lives. |
| Other member's code | Yes, partially. The methods `countUnits`, `findProduct` and `isReturnOf` would move onto `Sale` and `Return`, which belong to the Technical Leader (Requirement 3), so those corrections must be agreed with him. `Warranty.covers` would move onto `Warranty`, which belongs to Developer 1. |

**Correct applications:**

- `Customer.getSummary` and `Seller.getSummary` (model, R1) answer "how do I describe this
  person" from the attributes of the person itself, which is where the data is.
- `Return.calculateRefundAmount` (model, R3) answers "how much is owed for the returned
  units" from its own product list and its own warranty refund, instead of asking a service.
- `Warranty.generateWarrantyCertificate` (model, R4) builds the certificate from its own
  product, sale and dates.
- `PersonService.findCustomerById` and `findSellerById` (lines 133 and 157) search only the
  list the service owns, and they return `null` instead of answering questions about the
  people they find.

---

## Creator

**Verdict: correct.**

Correct applications:

- `PersonRepository` creates the `Customer` and `Seller` objects when it reads
  `data/customers.csv` and `data/sellers.csv`. The repository owns the format of those two
  files, so it is the class that knows how to rebuild them.
- `AccessoryRepository.deserialize` creates `Controller`, `Cable` and `Memory` from the type
  marker of each record. It owns the column layout of `data/accessories.csv`, therefore it
  knows which subclass a line means.
- `PromotionRepository.deserialize` creates the three promotion types for the same reason.
- `WarrantyService` creates `BasicWarranty` and `ExtendedWarranty` in
  `assignBasicWarranty` and `assignExtendedWarranty`, and it generates their identifiers with
  `nextWarrantyId`. The service owns the rule of which warranty the store grants, so it is
  the right place to decide that a new warranty is a basic one.
- `ReturnService` creates `Return` objects in `registerReturn`, including the identifier with
  `nextReturnId` and the date of the day. It also creates the plain collections it needs
  (`ArrayList`, `LinkedHashMap`), which is the cheapest choice when no behaviour is wanted.

---

## Controller

**Verdict: correct.**

Correct applications:

- `Main.main` is the controller of the application. It creates the repositories, the services
  and the menu, injects each dependency and starts the run. It contains no business rule.
- `ConsoleMenu.runSafely` (lines 111-122) handles the events of a submenu and routes each
  failure to a different message, so the user never sees a stack trace. It decides nothing
  about the business.
- `AccessoryService.restoreStock` and `WarrantyService.cancelWarranties` coordinate a change
  over several objects, which is what a controller does inside its own module.
- `ReturnService.registerReturn` coordinates the eight steps of a return, in order, across the
  catalogue, the accessories, the warranties and the file. It is the controller of that
  process without deciding any of the rules it applies.

---

## Low Coupling

**Verdict: violation.**

### V-02 — `ReturnService` depends on five collaborators and on the concrete product types

| Field | Content |
|---|---|
| ID | V-02 |
| Pattern | Low Coupling |
| Module | Returns (R3) |
| Evidence | `ReturnService` declares five collaborator fields (lines 55-59): `ReturnRepository repository`, `SaleService saleService`, `ProductService productService`, `AccessoryService accessoryService` and `WarrantyService warrantyService`. It also imports six model types (lines 3-8): `Accessory`, `Console`, `Customer`, `Product`, `Return` and `Sale`. The concrete types are used in `cancelWarrantiesOfConsoles` (line 179, `product instanceof Console`), in `restoreStock` (line 317, `product instanceof Accessory`) and in `viewReturnsByCustomer` (line 411, `sale.getCustomer()`). |
| Explanation | A return needs the sale, and it needs somebody who owns each kind of stock. Instead of talking to one thing, the service holds four collaborators and has to know which of them owns each item, so adding a fifth kind of item would mean a fifth collaborator and another `instanceof`. The dependency is not expressed as an abstraction, it is expressed as knowledge of the concrete classes. |
| Consequence | The number of collaborators a return needs grows with the number of modules the store sells, and a change of one line in one service is a reason to recompile and retest this one. The `instanceof` checks are also duplicated in V-03 and V-04, so the same knowledge is expressed in three different shapes. |
| Proposed solution | Give the stock a single owner instead of two. The cleanest form is a small interface with `restoreUnits(String id, int quantity)` and `findById(String id)`, implemented by `ProductService` and `AccessoryService`, and have `ReturnService` hold the list of them. The `instanceof` tests then disappear because each service restores its own items. The signature of `registerReturn` does not change, so the menu is not affected. |
| Other member's code | Yes. `ProductService` and `AccessoryService` would have to be declared as implementations of the new interface. `ProductService` is Developer 1's and `AccessoryService` is mine. |

**Correct applications:**

- `PersonService` depends only on `PersonRepository`. It does not call the catalogue, the
  sales or the menu, and it does not know they exist.
- `PromotionService` depends only on `PromotionRepository`. Even `findBestPromotionFor`, which
  takes a `Sale`, reads it through the methods of `Sale` instead of reaching into the sale
  module.
- `PromotionRepository` depends only on the three promotion classes, never on a service, which
  is the rule the architecture sets for the persistence layer.
- The R6 validators are used as static calls, so a service that adopts them does not gain a
  new collaborator object to construct or to keep alive.

---

## Findings reported for the consolidation

These problems were found while auditing my own code, but the fix is not mine alone. They
are not audited here; they are reported to the Technical Leader so that he can treat them in
the consolidated audit.

| # | Reported to | What was found | Why it is not mine |
|---|---|---|---|
| R-01 | Technical Leader (Rafael) | `ReturnService.countUnits` and `findProduct` answer questions about `Sale`; the answers would belong in `Sale` (see V-01). `Sale` is his class. | The audit of `Sale` is his. |
| R-02 | Technical Leader (Rafael) | `ReturnService.isReturnOf` and `ReturnRepository.findProduct` walk `Return.getSale()` and `Sale.getProducts()`; a `comesFromSale` and a `findProductById` would belong in the domain (see V-01). | Same as R-01. |
| R-03 | Developer 1 (Andrés) | `WarrantyService.covers` (lines 248-256) asks whether a warranty covers a product in a sale; the warranty already stores both links (see V-01). | `Warranty` is Developer 1's class. |
| R-04 | Developer 1 (Andrés) | `Product` has no way to say that it is a console or that its stock must be restored somewhere else, which forces the `instanceof` tests of V-03. | `Product` is Developer 1's class. |
| R-05 | Developer 1 (Andrés) | `Accessory`, `Promotion` and `Warranty` do not publish the type marker used in the first column of their files, which forces the `instanceof` chains of V-04. | Those classes are Developer 1's. |