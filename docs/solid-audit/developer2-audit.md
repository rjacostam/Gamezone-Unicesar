# Developer 2 SOLID Audit — GameZone Unicesar

Author: José David Cervantes Solano (Developer 2). Audited code: `develop` at commit `675e61c`.

Every verdict below is about the code that is my responsibility. Problems that were found
in code owned by another member are listed in
[Findings reported for the consolidation](#findings-reported-for-the-consolidation)
instead of being audited here.

---

## Scope

| Module | Requirement | Classes audited | Members audited |
|---|---|---|---|
| People | Taller 1 | `Person`, `Customer`, `Seller`, `PersonRepository`, `PersonService` | `getRole`, `getSummary`, `setEmail`, `saveCustomers`, `loadCustomers`, `saveSellers`, `loadSellers`, `registerCustomer`, `listCustomers`, `listSellers`, `findCustomerById`, `findSellerById` |
| Accessories | R1 | `AccessoryRepository`, `AccessoryService` | `saveAll`, `loadAll`, `serialize`, `deserialize`, `registerController`, `registerCable`, `registerMemory`, `listAllAccessories`, `listAccessoriesByType`, `findAccessoriesCompatibleWith`, `findById`, `updateStock`, `restoreStock` |
| Promotions | R2 | `PromotionRepository`, `PromotionService` | `saveAll`, `loadAll`, `serialize`, `deserialize`, `registerPercentageDiscount`, `registerCategoryDiscount`, `registerBulkPurchaseDiscount`, `listAllPromotions`, `listActivePromotions`, `findBestPromotionFor`, `findById` |
| Warranties | R4 | `WarrantyRepository`, `WarrantyService`, `WarrantyRecord` | `saveAll`, `loadAll`, `serialize`, `toRecord`, `loadWarranties`, `assignBasicWarranty`, `assignExtendedWarranty`, `cancelWarranties`, `findWarrantyByProduct`, `listAllWarranties`, `listActiveWarranties`, `listWarrantiesExpiringSoon` |
| Returns | R3 | `ReturnRepository`, `ReturnService` | `saveAll`, `loadAll`, `registerReturn`, `resolveReturnedProducts`, `restoreStock`, `catalogOf`, `cancelWarrantiesOfConsoles`, `viewAllReturns`, `viewReturnsByCustomer`, `viewReturnsBySale`, `calculateMonthlySales`, `calculateMonthlyReturns`, `generateMonthlyBalance` |
| Correction of R7 | R7 | `InventoryCatalog`, `AccessoryService`, `ReturnService`, `PromotionRepository`, `WarrantyRepository`, `AccessoryRepository` | the three methods of the interface, the two implementations that satisfy it, `catalogOf`, `serialize` in the three repositories |
| Exception and validation layer | R6 | the seven repositories and the seven services | every public repository method and every `validate...` delegation inside my services |

Not audited here: `Product`, `VideoGame`, `Console`, `Accessory`, `Controller`, `Cable`,
`Memory`, `Promotion`, `PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount`,
`BasicWarranty`, `ExtendedWarranty`, `Return`, `Warranty` (Developer 1), `Sale`,
`SaleService`, `SaleRepository`, `ConsoleMenu`, `Main`, and the exception and validator
classes themselves (Technical Leader and Developer 1). The menu classes are excluded
because the Technical Leader includes them in his own report and a class must be audited
once.

---

## Summary

| Principle | Violation | Correct | Not applicable |
|---|---|---|---|
| Single Responsibility | 1 | 5 | 0 |
| Open/Closed | 2 | 2 | 0 |
| Liskov Substitution | 1 | 0 | 0 |
| Interface Segregation | 1 | 2 | 0 |
| Dependency Inversion | 2 | 3 | 0 |

Seven findings: V-01 to V-07, plus the problems reported for the consolidation.

---

## Single Responsibility Principle

**Verdict: violation.**

### V-01 — `ReturnService` manages returns and also builds a financial report

| Field | Content |
|---|---|
| ID | V-01 |
| Principle | Single Responsibility |
| Module | Returns (R3) |
| Evidence | Four of the seven public methods manage returns: `registerReturn` (line 128), `viewReturnsByCustomer` (line 382), `viewReturnsBySale` (line 404) and `viewAllReturns` (line 530). The other three build a report out of two different modules: `calculateMonthlySales` (line 443) walks `saleService.listAllSales()`, `calculateMonthlyReturns` (line 466) walks the returns of the service and `generateMonthlyBalance` (line 508) subtracts one figure from the other. |
| Explanation | The class changes for two unrelated reasons. Adding a rule about what can be returned touches the first group; adding a column to the financial report touches the second one. The report is not knowledge of a return: it needs the whole history of sales, which this class only reaches by asking another service for a list it does not own. |
| Consequence | The return module has to be recompiled and retested when a figure of the report changes, and the report has to be recompiled and retested when a return rule changes. A reader looking for the rule of a partial return has to skip three methods about accounting. |
| Proposed solution | Move the three report methods to a `MonthlyBalanceService` in the same package that receives `SaleService` and the list of returns, and leaves `ReturnService` with the four methods about returns. The figures do not change, because the code moves without being rewritten. |
| Other member's code | No. `ReturnService` is entirely mine. The new class would need `SaleService`, but it would only call the two methods it already uses today. |

**Correct applications:**

- `PromotionService` only registers, lists and selects promotions. Every validation it runs
  is a promotion rule, so nothing in the class belongs to another module.
- `WarrantyService` only assigns, cancels and lists warranties, and the identifier
  generation (`nextWarrantyId`, `parseIdNumber`) exists because of warranty assignment.
- `AccessoryRepository` only reads and writes `data/accessories.csv`, and `ReturnRepository`
  only reads and writes `data/returns.csv`.
- `PersonService` only registers, lists and searches people. Its three private helpers
  (`people`, `withCustomer`, `requireText` before R6) exist to serve one of those.
- `WarrantyRecord` carries one line of `data/warranties.csv` from the repository to the
  service and answers nothing about warranties, so it has a single reason to change: the
  layout of that file.

---

## Open/Closed Principle

**Verdict: violation.**

### V-02 — A new kind of accessory changes `AccessoryService` and `AccessoryRepository`

| Field | Content |
|---|---|
| ID | V-02 |
| Principle | Open/Closed |
| Module | Accessories (R1) |
| Evidence | Registration is split in three methods that repeat the same shape: `registerController` (line 77), `registerCable` (line 106) and `registerMemory` (line 136). Reading the file branches three more times: `AccessoryRepository.deserialize` tests `TYPE_CONTROLLER` (line 183), `TYPE_CABLE` (line 187) and `TYPE_MEMORY` (line 191). |
| Explanation | The module is open for reading a new kind of accessory and closed for adding one. The kinds of accessory are written in six places, so a fourth kind is not an extension of the module, it is an edit of it. The class hierarchy already says which classes exist; the two classes repeat that list as code. |
| Consequence | A fourth kind of accessory means editing the service and the repository, and any place that is missed produces a record that is written but cannot be read back, or read back as the wrong class. The three methods are also three copies of the same validation and save sequence, so a rule fixed in one is not fixed in the other two. |
| Proposed solution | Move the creation of the concrete accessory to the model, with a factory such as `AccessoryFactory.create(type, id, title, price, stock, extras)`, and give the service one `register` method that takes the accessory the factory built. The repository then asks the same factory in `deserialize`, so the list of kinds exists once. |
| Other member's code | Yes. `Accessory`, `Controller`, `Cable` and `Memory` are Developer 1's, and the menu that calls the three register methods is the Technical Leader's, so both need to agree before Phase 2 of R8. |

### V-03 — A new kind of promotion changes `PromotionService` and `PromotionRepository`

| Field | Content |
|---|---|
| ID | V-03 |
| Principle | Open/Closed |
| Module | Promotions (R2) |
| Evidence | `PromotionService` repeats the registration in `registerPercentageDiscount` (line 61), `registerCategoryDiscount` (line 89) and `registerBulkPurchaseDiscount` (line 116). `PromotionRepository.deserialize` tests `TYPE_PERCENTAGE` (line 184), `TYPE_CATEGORY` (line 187) and `TYPE_BULK` (line 191). |
| Explanation | The same defect as V-02 in the other module: the kinds of promotion are written as code in the service and again in the repository, so the module has to be edited to grow. |
| Consequence | A second kind of percentage discount, for example a discount for a date range, is not added by extending the class; it is added by copying a method and adding a branch, and every branch copied is another place where a validation can drift. |
| Proposed solution | The same factory, `PromotionFactory.create(type, id, name, start, end, extras)`, with one `register` method in the service and one call to the factory in `deserialize`. The three existing classes keep answering `calculateDiscount` and `getTypeCode`, so the file does not change. |
| Other member's code | Yes. `PercentageDiscount`, `CategoryDiscount` and `BulkPurchaseDiscount` are Developer 1's and the menu is the Technical Leader's. |

**Correct applications:**

- `AccessoryRepository.serialize` writes the extra columns with
  `String.join(DELIMITER, accessory.getExtraFields())` (line 141). Since the R7 correction,
  a new kind of accessory publishes its own fields and this method is not edited, which is
  what Open/Closed asks for.
- `PromotionRepository.serialize` and `WarrantyRepository.serialize` write the marker with
  `getTypeCode()` (lines 136 and 141), so a new kind of promotion or warranty does not add a
  branch to the writing side.

---

## Liskov Substitution Principle

**Verdict: violation.**

### V-04 — `findById` answers differently depending on which catalogue is asked

| Field | Content |
|---|---|
| ID | V-04 |
| Principle | Liskov Substitution |
| Module | Correction of R7 (V-07) |
| Evidence | The interface declares `Product findById(String id)` (`InventoryCatalog`, line 36). `ProductService.findById` (lines 128-135) compares `product.getId().equalsIgnoreCase(id)` directly and returns `null` when nothing matches, without trimming and without a null or blank guard. `AccessoryService.findById` (lines 209-220) returns `null` for a null or blank identifier and trims the one it receives first, `String searched = id.trim()` at line 213. |
| Explanation | Both classes satisfy the signature, so the compiler is satisfied, but not the contract. The interface does not say whether the identifier is trimmed, and the two answers are different: a caller that asks both catalogues for `"A-01 "` gets the accessory from one of them and `null` from the other. A subtype that answers a question differently from its supertype cannot be substituted for it. |
| Consequence | Today the defect is masked twice: `catalogOf` asks the catalogues in order and falls back to the product catalogue when none answers, and the direct callers trim the identifier at the call site. Any new caller that trusts the interface to answer uniformly, a search box that does not trim, or a service that reads an identifier from a file, gets the right answer from one catalogue and `null` from the other, and the difference only appears with an identifier that carries spaces. |
| Proposed solution | Decide the contract once and make both obey it. The cheapest is the one already used by the accessory service: the identifier is trimmed and a null or blank answers `null`. The two lines of `ProductService.findById` change and nothing else does, because the trimmed form is what every caller already sends. |
| Other member's code | Yes. `ProductService` is Developer 1's and `findById` is the method V-07 put in the interface, so the two implementations have to be corrected together or not at all. |

---

## Interface Segregation Principle

**Verdict: violation.**

### V-05 — No client of `InventoryCatalog` uses its three methods

| Field | Content |
|---|---|
| ID | V-05 |
| Principle | Interface Segregation |
| Module | Correction of R7 (V-07) |
| Evidence | The interface declares three methods: `findById` (line 36), `updateStock` (line 55) and `restoreStock` (line 71). Of the two clients, `SaleService.updateInventory` calls only `updateStock` (line 324) and `ReturnService.restoreStock` calls only `restoreStock` (line 303). Both call `findById` through `catalogOf`. No client of the interface ever calls all three. |
| Explanation | An interface should be the smallest set of operations its clients need. This one is the union of what the sale module and the return module need, so each of them depends on one method it will never call. The interface was built for two consumers that need different things, which is the situation the principle warns about. |
| Consequence | A fifth operation added for one module has to be declared in the interface and implemented by both catalogues, even when the other module can never use it, and a catalogue that has no meaning for that operation has to answer something artificial. The interface also grows for both clients every time either one grows. |
| Proposed solution | Split it by client. A `CatalogSearch` with `findById`, and two interfaces that extend it: `StockUpdater` with `updateStock` for the sale path and `StockRestorer` with `restoreStock` for the return path. Each service then declares the field of the one interface it uses, and a catalogue only implements what it really does. The classes and the file formats do not change. |
| Other member's code | Yes. `ProductService` is Developer 1's and `SaleService` is the Technical Leader's, so the split has to be agreed with both of them before Phase 2 of R8. |

**Correct applications:**

- The R6 validators are used as static calls and each service asks only for the rules it
  applies: `ReturnService` calls `ReturnValidator` and `SaleValidator`, `AccessoryService`
  calls `AccessoryValidator` and `ProductValidator`, and `PromotionService` calls only
  `PromotionValidator`. No service is forced to know a rule of a module it does not touch.
- `WarrantyRecord` is not an interface and does not pretend to be one: it declares five
  getters and nothing else, so the only client that uses it, `WarrantyService.loadWarranties`,
  depends on exactly the data it needs.

---

## Dependency Inversion Principle

**Verdict: violation.**

### V-06 — `ReturnService` depends on five concrete classes

| Field | Content |
|---|---|
| ID | V-06 |
| Principle | Dependency Inversion |
| Module | Returns (R3) |
| Evidence | The class declares `private ReturnRepository repository` (line 53), `private SaleService saleService` (line 54), `private ProductService productService` (line 55), `private AccessoryService accessoryService` (line 56) and `private WarrantyService warrantyService` (line 57). The only abstraction it holds, `private final List<InventoryCatalog> inventoryCatalogs` (line 61), is used inside `restoreStock` and `catalogOf` only. |
| Explanation | Four of the five are services of other modules and one is the storage of this one. Every one of them is named as a concrete type, so the return module cannot be built, read or tested without the sale module, the catalogue module, the accessory module, the warranty module and the file format. R7 already showed the cost: the type of stock an item has is decided by asking two of them, because the service holds the classes rather than the operation. |
| Consequence | A change in `SaleService` is a reason to recompile and retest `ReturnService`, and the same for the other three. The class cannot be tested without five collaborators, and the reason a test needs them is that the code names them, not that the behaviour needs them. |
| Proposed solution | Depend on the operation instead of on the class. Three small abstractions are enough: a `ReturnStore` with `loadAll` and `saveAll` for the repository, a `SaleFinder` with `findById` and `listAllSales`, and a `WarrantyCanceller` with `cancelWarranties`. The existing classes implement them without any change, the same way `ProductService` and `AccessoryService` already implement `InventoryCatalog`. |
| Other member's code | Yes. `SaleService` and `ProductService` are not mine, and `AccessoryService` is mine, so the interfaces have to be agreed before Phase 2 of R8. |

### V-07 — `AccessoryService` reaches into the concrete `ProductService` of another module

| Field | Content |
|---|---|
| ID | V-07 |
| Principle | Dependency Inversion |
| Module | Accessories (R1) |
| Evidence | The class holds `private ProductService productService` and uses it twice: `findAccessoriesCompatibleWith` calls `productService.findById(consoleId.trim())` (line 309) and the identifier check calls `productService.findById(id) != null` (line 328). Both are the same question, whether the catalogue holds an item with that identifier. |
| Explanation | Both call sites are already the operation `InventoryCatalog.findById`, the abstraction the accessory service itself implements. Instead of using it, the module names the concrete class of the catalogue, so the accessory module is compiled and tested against the catalogue module. |
| Consequence | The two modules cannot be built or tested apart, and a third catalogue, a subscription for example, would make this a second concrete field and a second `instanceof` on the question of whether an item exists. R7 removed exactly that kind of test from `ReturnService`, and the same knowledge is still expressed here as a field of a concrete class. |
| Proposed solution | Replace the field with the abstraction the class already satisfies: hold a `List<InventoryCatalog>` of the catalogues it has to consult, or the narrower `CatalogSearch` of V-05, and ask `catalog.findById(...)`. The two call sites do not change their shape and `ProductService` needs no new method. |
| Other member's code | Partly. The field type is Developer 1's class, but `InventoryCatalog` already exists and is the contract that fits, so the correction is mostly on my side. |

**Correct applications:**

- `InventoryCatalog` is a real abstraction and both catalogues implement it, so `ReturnService`
  and `SaleService` can ask a list of them who owns an item without naming either class.
  This is the Dependency Inversion applied to the one place where it was needed.
- Every service receives its repository through the constructor and none of them builds
  one: `PersonService`, `AccessoryService`, `PromotionService`, `ReturnService` and
  `WarrantyService` all hold `new` nowhere in their body. The wiring happens once, in the
  composition root, which is where it belongs.
- `ReturnService.catalogOf` asks the catalogues in a fixed order and falls back to the
  product catalogue (line 320), so the decision has a defined answer even when a catalogue
  does not hold the item, instead of depending on which service happens to be called first.

---

## Findings reported for the consolidation

These problems were found while auditing my own code, but the fix is not mine alone. They
are not audited here; they are reported to the Technical Leader so that he can treat them in
the consolidated audit.

| # | Reported to | What was found | Why it is not mine |
|---|---|---|---|
| S-01 | Developer 1 (Andrés) | `ProductService.findById` (lines 128-135) trims nothing and guards nothing, while `AccessoryService.findById` (lines 209-220) does both, so the two do not honour the same contract of `InventoryCatalog.findById` (see V-04). | `ProductService` is Developer 1's class. |
| S-02 | Developer 1 (Andrés) | `ProductService` is named as a concrete type by `AccessoryService` (lines 309 and 328) when both call sites are the operation `InventoryCatalog.findById` (see V-07). | The field type is Developer 1's class. |
| S-03 | Developer 1 (Andrés) | `ProductService` has to be split with the other catalogue when `InventoryCatalog` is segregated per client, otherwise the two implementations honour different contracts again (see V-05). | `ProductService` is Developer 1's class. |
| S-04 | Technical Leader (Rafael) | `SaleService` is named as a concrete type by `ReturnService` (line 54) and used only for `findById` and `listAllSales`, so it is a candidate for the `SaleFinder` abstraction (see V-06). | `SaleService` is the Technical Leader's class. |
| S-05 | Technical Leader (Rafael) | `SaleService.updateInventory` is the only client of `InventoryCatalog.updateStock`, so it is the side that decides how the interface is segregated (see V-05). | `SaleService` is the Technical Leader's class. |
| S-06 | Developer 1 (Andrés) | `Accessory`, `Controller`, `Cable`, `Memory` and the three promotions are the classes a factory would have to build in V-02 and V-03, so the creation cannot move into a factory without agreement on their constructors. | Those model classes are Developer 1's. |