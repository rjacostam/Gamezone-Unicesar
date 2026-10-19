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
| People | Taller 1 (R1) | `Person`, `Customer`, `Seller`, `PersonRepository`, `PersonService` | `getRole`, `getSummary`, `setEmail`, `saveCustomers`, `loadCustomers`, `saveSellers`, `loadSellers`, `registerCustomer`, `listCustomers`, `listSellers`, `findCustomerById`, `findSellerById` |
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
| Open/Closed | 2 | 3 | 0 |
| Liskov Substitution | 1 | 3 | 0 |
| Interface Segregation | 1 | 2 | 0 |
| Dependency Inversion | 2 | 3 | 0 |

Seven findings: V-01 to V-07, plus the problems reported for the consolidation.