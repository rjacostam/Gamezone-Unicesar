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
