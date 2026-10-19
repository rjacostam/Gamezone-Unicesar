# Developer 2 GRASP Audit — GameZone Unicesar

Author: José David Cervantes Solano (Developer 2). Audited code: `develop` at commit `6c10ce2`.

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
| Exception and validation layer | R6 | the seven repositories and the seven services | every public repository method and every `validate...` delegation inside my services |

Not audited here: `Product`, `VideoGame`, `Console`, `Accessory`, `Controller`, `Cable`,
`Memory`, `Promotion`, `PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount`,
`BasicWarranty`, `ExtendedWarranty`, `Return`, `Warranty` (Developer 1), `Sale`,
`SaleService`, `SaleRepository`, `ConsoleMenu`, `Main`, and the exception and validator
classes themselves (Technical Leader and Developer 1). `Main.main` and
`ConsoleMenu.runSafely` are not audited here either: both classes are mine, but the
Technical Leader already includes them in his own report, and a class must be audited once.

---

## Summary

| Pattern | Violation | Correct | Not applicable |
|---|---|---|---|
| Information Expert | 1 | 4 | 0 |
| Creator | 0 | 5 | 0 |
| Controller | 0 | 2 | 0 |
| Low Coupling | 1 | 3 | 0 |
| High Cohesion | 0 | 6 | 0 |
| Polymorphism | 2 | 2 | 0 |
| Pure Fabrication | 0 | 3 | 0 |
| Indirection | 0 | 4 | 0 |
| Protected Variations | 0 | 5 | 0 |

Four findings: V-01 to V-04, plus five problems reported for the consolidation.

---

## Information Expert

**Verdict: violation.**

### V-01 — Three helpers answer questions about the products of a sale instead of `Sale` itself

| Field | Content |
|---|---|
| ID | V-01 |
| Pattern | Information Expert |
| Module | Returns (R3) |
| Evidence | `ReturnService.countUnits(Sale sale, String productId)` (lines 247-259) walks `sale.getProducts()` and counts how many units of a product the sale contains. `ReturnService.findProduct(Sale sale, String productId)` (lines 347-359) answers which product of the sale carries an identifier. `ReturnRepository.findProduct(Sale sale, String productId)` (lines 295-304) repeats the same search inside the persistence layer. |
| Explanation | Both questions are knowledge of `Sale`, which owns the product list and already publishes it through `getProducts()`. How many units of a product a sale contains is knowledge of `Sale`; which product of a sale carries an identifier is knowledge of `Sale`. The three helpers walk that list from outside, so the knowledge of the domain is spread across a service and a repository instead of living in the class that owns the data. |
| Consequence | If `Sale` ever stops storing one entry per unit and starts summarising quantities, `countUnits` silently returns a wrong count and the partial return rule accepts more units than the customer bought. The same question is also answered twice, once in `ReturnService` and once in `ReturnRepository`, and the two copies have no way of being kept in step. |
| Proposed solution | Add the answers to `Sale`, the class that owns the data: `countUnitsOf(String productId)` and `findProductById(String productId)`. The three helpers then become one-line calls and the knowledge has a single place. The behaviour does not change, only where it lives. |
| Other member's code | Yes. `Sale` belongs to the Technical Leader (Requirement 3), so this correction has to be agreed with him before Phase 2 starts. |

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
| Evidence | `ReturnService` declares five collaborator fields (lines 55-59): `ReturnRepository repository`, `SaleService saleService`, `ProductService productService`, `AccessoryService accessoryService` and `WarrantyService warrantyService`. It also imports six model types (lines 3-8): `Accessory`, `Console`, `Customer`, `Product`, `Return` and `Sale`, and it names a concrete type in `viewReturnsByCustomer` (line 411, `sale.getCustomer()`). The two `instanceof` tests of `cancelWarrantiesOfConsoles` and `restoreStock` express the same dependency on concrete classes; they are evidence of V-03 and are recorded there so that the two lines are not cited twice. |
| Explanation | A return needs the sale, and it needs somebody who owns each kind of stock. Instead of talking to one thing, the service holds four collaborators and has to know which of them owns each item, so adding a fifth kind of item would mean a fifth collaborator and another `instanceof`. The dependency is not expressed as an abstraction, it is expressed as knowledge of the concrete classes. |
| Consequence | The number of collaborators a return needs grows with the number of modules the store sells, and a change of one line in one service is a reason to recompile and retest this one. |
| Proposed solution | Give the stock a single owner instead of two. The cleanest form is a small interface with `restoreUnits(String id, int quantity)` and `findById(String id)`, implemented by `ProductService` and `AccessoryService`, and have `ReturnService` hold the list of them. The `instanceof` tests then disappear because each service restores its own items (see V-03, which records the same evidence and is corrected by this same refactor). The signature of `registerReturn` does not change, so the menu is not affected. |
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

## High Cohesion

**Verdict: correct.**

Correct applications:

- `PersonService` only registers, lists and searches people. Every private helper
  (`people`, `withCustomer`, `requireText` before R6) exists to serve one of those.
- `PromotionService` only registers, lists and selects promotions. `validateCommonData`,
  `validatePercentage`, `validateTargetCategory` and `validateMinimumQuantity` are all
  promotion rules, so they belong together.
- `WarrantyService` only assigns, cancels and lists warranties. The identifier generation
  (`nextWarrantyId`, `parseIdNumber`) exists because of warranty assignment.
- `AccessoryService` only manages accessories. After R6 its `validateAccessoryData` is three
  lines that delegate to `ProductValidator`, and the remaining checks are accessory rules.
- `ReturnRepository` only reads and writes `data/returns.csv`.
- **`ReturnService` covers two responsibilities — returning products and the monthly financial
  report** (`calculateMonthlySales`, line 464, `calculateMonthlyReturns`, line 487 and
  `generateMonthlyBalance`, line 529). This was checked and is accepted. The statement of
  Requirement 3 asks for one report that consolidates sales and returns, and
  `ReturnService` is the only class that already knows both. Splitting it would need a
  report service, which would have to receive both services and would only move the
  dependencies one level up. The cohesion of this class is therefore a consequence of the
  requirement, not an accident of the implementation, and it is recorded here so that the
  trade-off is visible instead of hidden.

---

## Polymorphism

**Verdict: violation.**

### V-03 — `ReturnService` decides by type which service owns an item

| Field | Content |
|---|---|
| ID | V-03 |
| Pattern | Polymorphism |
| Module | Returns (R3) |
| Evidence | `cancelWarrantiesOfConsoles` (lines 176-186) skips every unit that is not a `Console`: `if (!(product instanceof Console)) { continue; }` (line 179). `restoreStock` (lines 310-323) splits the units with `product instanceof Accessory ? accessoryUnits : productUnits` (line 317) and then calls `accessoryService.restoreStock` or `productService.restoreStock` depending on the group (lines 320-323). |
| Explanation | The same program says "accessories go to one place and everything else to another" in two different shapes, once with a `continue` and once with a ternary. The behaviour of an item should be decided by the item, not by the service that is holding it. |
| Consequence | If a new kind of item appears, for example a subscription or a pre-order, both methods fall into the "everything else" branch and its stock is silently restored through the catalogue service. Nothing warns about it, and the bug only shows up as a wrong inventory number. |
| Proposed solution | Let each item answer what it is, through a method on `Product` such as `restoreStock()` or through an interface each service implements, so that the branch disappears from `ReturnService`. V-02 proposes the same change, so both findings are corrected by one refactor. |
| Other member's code | Yes. `Product` is Developer 1's, and `ProductService` would also have to accept the new collaborator. |

### V-04 — The repositories derive the type marker of a record with `instanceof` chains

| Field | Content |
|---|---|
| ID | V-04 |
| Pattern | Polymorphism |
| Module | Accessories (R1) and Promotions (R2) |
| Evidence | `AccessoryRepository.serialize` (lines 139-152) writes the marker with `if (accessory instanceof Controller)`, `else if (accessory instanceof Cable)`, `else if (accessory instanceof Memory)`. `PromotionRepository.getTypeOf` (lines 220-229) does the same with `PercentageDiscount`, `CategoryDiscount` and `BulkPurchaseDiscount`. `PromotionRepository.serialize` (lines 141-152) repeats the chain to write the extra columns. `WarrantyRepository.getTypeOf` (lines 208-212) uses `warranty instanceof BasicWarranty`. |
| Explanation | The marker written in the first column is a property of the object being written. All six branches restate the class hierarchy that the model already expresses with inheritance, so the hierarchy is duplicated as a chain of tests that has to be updated every time a type is added. |
| Consequence | Adding a fourth accessory type means editing three repositories. If one of them is missed, the new type is saved with the marker of the previous branch and it comes back from the file as the wrong class, or is silently skipped. |
| Proposed solution | Publish the marker from the object, with an abstract method such as `getAccessoryType()` and `getPromotionType()`, the way the accessory model already publishes `getCategory()`. The repositories then write `accessory.getTypeMarker()` and lose the chains. The file format does not change. |
| Other member's code | Yes. The method has to be declared on `Accessory`, `Promotion` and `Warranty`, which belong to Developer 1. |

**Correct applications:**

- `Customer.getSummary` and `Seller.getSummary` give the same summary in two classes without
  any test on the type: the menu calls `getSummary()` on a `Person` reference and the right
  version runs.
- `listAllAccessories` and `findBestPromotionFor` filter with `getAccessoryType()` and with
  the result of `calculateDiscount`, so no type test is needed.
- `findWarrantyByProduct` compares the end dates of two warranties instead of testing their
  types, so the longest warranty wins whatever its type is.
- The validators of R6 are chosen by the service, not by an `instanceof` chain, which is why
  adding an accessory type does not add a branch to the validation.

---

## Pure Fabrication

**Verdict: correct.**

Correct applications:

- `WarrantyRecord` (lines 20-99) holds five final fields (`type`, `id`, `productId`, `saleId`,
  `startDate`), one constructor and five getters, with no behaviour at all. It is therefore a
  pure fabrication: it models one line of `data/warranties.csv`, not a warranty of the store,
  because it cannot answer whether it is active, what it covers or how long it lasts. The
  fabrication is **justified by adjustment A2**. `WarrantyRepository.loadAll` takes no
  arguments, so the repository cannot reach the sales module; without this class the
  repository would have to resolve the `Sale` and the `Product` of every record itself, and
  the layers would close a cycle, `SaleService` to `WarrantyService` to `WarrantyRepository`
  back to `SaleService`. The class is the smallest object that breaks that cycle and it keeps
  the column layout of the file inside the persistence layer, which is what Protected
  Variations asks for. It is kept exactly as it is, so this is a correct application of the
  pattern and not a finding to correct in Phase 2.
- `PersonRepository.readQuietly` is a named step, not a new class. It gives a name to the
  behaviour of turning a disk failure into the typed exception, without widening the API of
  the package.
- `GameZoneException` and its four subclasses name the four kinds of failure the statement
  asks the menu to tell apart, so each one earns its existence.

---

## Indirection

**Verdict: correct.**

Correct applications:

- The repository layer as a whole is indirection that pays for itself: the services ask for
  objects and never learn that a file is involved, so the storage can be replaced without
  touching a single rule of the business.
- `WarrantyService.toWarranty` (lines 137-146) converts a record into a warranty in one
  obvious place, instead of spreading the construction over the loop of `loadWarranties`.
- `ReturnService.restoreStock` (lines 310-323) groups the units and then calls the service
  that owns each group, instead of repeating the call inside the loop.
- `PromotionRepository.deserialize` and `toRecord` keep the parsing of a line out of
  `loadAll`, so reading the file and understanding a line are two different steps.

---

## Protected Variations

**Verdict: correct.**

Correct applications:

- The layout of `data/warranties.csv` is written in `WarrantyRepository` and read in
  `toRecord`. Changing the number of columns or the delimiter touches those two methods and
  nothing else; no service and no domain class reads a file.
- The same holds for `customers.csv` and `sellers.csv` in `PersonRepository`,
  `accessories.csv` in `AccessoryRepository`, `promotions.csv` in `PromotionRepository` and
  `returns.csv` in `ReturnRepository`. Each format is protected by one class.
- The lines saved before adjustment A7 are still read by `ReturnRepository.readWarrantyRefund`
  (lines 231-244), which accepts five columns with a refund of zero as well as six. The change
  of the format did not break the files that already existed.
- The R6 validation layer protects the services from the change of the validation strategy: a
  service calls `ProductValidator.validateProductData` and nothing else, so replacing the
  internal implementation of the validators does not touch a single service.
- `PersonService.registerCustomer` writes the file before it adds the customer to the list
  (lines 101-104), so the order in which the in-memory state and the file change is fixed in one
  place and cannot drift.

---

## Findings reported for the consolidation

These problems were found while auditing my own code, but the fix is not mine alone. They
are not audited here; they are reported to the Technical Leader so that he can treat them in
the consolidated audit.

| # | Reported to | What was found | Why it is not mine |
|---|---|---|---|
| R-01 | Technical Leader (Rafael) | `ReturnService.countUnits` and `findProduct` answer questions about `Sale`; the answers would belong in `Sale` (see V-01). `Sale` is his class. | The audit of `Sale` is his. |
| R-02 | Developer 1 (Andrés) | `ReturnService.isReturnOf` (lines 335-338) and `isReturnOfSaleId` (lines 447-452) walk `Return.getSale()` to decide whether a registered return comes from a sale; a `comesFromSale(String saleId)` would belong in `Return`. The helpers are mine but the class that would receive the answer is not. | `Return` is Developer 1's class. |
| R-03 | Developer 1 (Andrés) | `WarrantyService.covers` (lines 248-256) asks whether a warranty covers a product in a sale; the warranty already stores both links. The helper is mine but the class that would receive the answer is not. | `Warranty` is Developer 1's class. |
| R-04 | Developer 1 (Andrés) | `Product` has no way to say that it is a console or that its stock must be restored somewhere else, which forces the `instanceof` tests of V-03. | `Product` is Developer 1's class. |
| R-05 | Developer 1 (Andrés) | `Accessory`, `Promotion` and `Warranty` do not publish the type marker used in the first column of their files, which forces the `instanceof` chains of V-04. | Those classes are Developer 1's. |