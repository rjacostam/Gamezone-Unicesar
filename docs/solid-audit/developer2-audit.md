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

Seven findings: V-01 to V-07, plus the problems reported for the consolidation.---

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

