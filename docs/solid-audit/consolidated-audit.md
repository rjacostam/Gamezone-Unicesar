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

## Team verdict per principle

| Principle | Leader | Dev1 | Dev2 | Team verdict | Final violations |
|---|---|---|---|---|---|
| Single Responsibility | Violation | Violation | Violation | **Violation** | V-01, V-02, V-03 |
| Open/Closed | Violation | Violation | Violation | **Violation** | V-04, V-05, V-06 |
| Liskov Substitution | Correct | Violation (V-01, registered under ISP) | Violation | **Violation** | V-07 |
| Interface Segregation | Violation | Violation | Violation | **Violation** | V-08, V-09, V-10 |
| Dependency Inversion | Violation | Violation (V-01, registered under ISP) | Violation | **Violation** | V-11, V-12, V-13 |

The 17 violations of the three reports become 13 after merging the ones that describe the same
defect (C1 to C4 below). A violation that breaks more than one principle is registered once,
under the principle that guides its correction, and the others are named inside it.
