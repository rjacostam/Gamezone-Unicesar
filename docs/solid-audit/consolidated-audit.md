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
