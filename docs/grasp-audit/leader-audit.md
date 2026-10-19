# Leader GRASP Audit — GameZone Unicesar

Author: Rafael (Technical Leader). Audited code: `develop` at commit `6c10ce2`.

## Scope

Classes and methods audited, by module and requirement, following the responsibility
table of Requirement 7.

| Module (requirement) | Class | Methods audited |
|---|---|---|
| Sales (Taller 1), discount and receipt (R2), warranty cost (R4) | `Sale` | constructor, `calculateSubtotal`, `calculateTotal`, `applyDiscount`, `addExtendedWarrantyCost`, `generateReceipt`, getters and setters. `canBeReturned` is audited by Developer 1. |
| Sales (Taller 1), extended in R2 and R4 | `SaleRepository` | `saveAll`, `loadAll`, `toLine`, `fromLine`, `findCustomer`, `findSeller`, `findProduct` |
| Sales (Taller 1), accessories (R1), promotions (R2), warranties (R4), integration (R5) | `SaleService` | constructor, `registerSale` and its private steps (`requireCustomer`, `requireSeller`, `resolveItems`, `checkStock`, `applyBestPromotion`, `assignWarranties`, `updateInventory`, `saveSales`), `getCatalog`, `findItem`, queries |
| User interface (Taller 1), integration of R1–R4, exceptions (R6) | `ConsoleMenu` | `start`, the seven submenus, `registerSale`, `readAccessoryType`, promotion category selection, `runSafely`, input helpers |
| Startup (Taller 1) | `Main` | `main` |
| Returns (R3) | `ProductService` | `restoreStock` |
