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