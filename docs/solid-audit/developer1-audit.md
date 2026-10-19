# Developer 1 SOLID Audit — GameZone Unicesar

Author: Andrés Daniel Cuello Blanco (Developer 1). Audited code: `develop` at commit `675e61c`.

## Scope

Classes and methods audited, by module and requirement, following the responsibility table
of Requirement 8.

| Module (requirement) | Class | Members audited |
|---|---|---|
| Catalogue (Taller 1) | `Product` | constructor, `getDescription`, `getCategory`, `getExtraFields`, `isConsole`, `hasStock`, `decreaseStock`, `increaseStock`, `setStock`, the getters and setters and `toString` |
| Catalogue (Taller 1) | `VideoGame` | constructor, `getDescription`, `getCategory`, `getExtraFields`, the platform, genre and age rating accessors |
| Catalogue (Taller 1) | `Console` | constructor, `getDescription`, `getCategory`, `getExtraFields`, `isConsole`, the brand, model and generation accessors |
| Catalogue service (Taller 1, R3, R7) | `ProductService` | constructor, `registerVideoGame`, `registerConsole`, `listAllProducts`, `findById`, `updateStock`, and the fact that the class declares `implements InventoryCatalog`. `restoreStock` is not audited |
| Accessories (R1, R7) | `Accessory` | constructor, `getCategory`, `getAccessoryType`, `getDescription`, `isCompatibleWith`, `addCompatibleConsole`, `removeCompatibleConsole`, `getCompatibleConsoleIds` |
| Accessories (R1, R7) | `Controller`, `Cable`, `Memory` | constructors, `getAccessoryType`, `getExtraFields` and `getDescription` of each one |
| Promotions (R2, R7) | `Promotion` | `calculateDiscount`, `getDescription`, `getTypeCode`, `getExtraFields`, `isActive`, the getters and setters |
| Promotions (R2, R7) | `PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount` | constructors and the four methods each one answers |
| Warranties (R4, R7, R9) | `Warranty` | constructor, the four abstract methods, `isActive`, `coverageDescription`, the getters |
| Warranties (R4, R7, R9) | `BasicWarranty`, `ExtendedWarranty` | constructors and the four methods each one answers |
| Returns (R3) | `Return` | constructor, `calculateRefundAmount`, `discountRatio`, the getters |
| Returns (R3) | `Sale` | `canBeReturned` only |
| Exceptions (R6) | `GameZoneException` | the three protected constructors and `getErrorCode` |
| Exceptions (R6) | `ResourceNotFoundException`, `BusinessRuleException`, `InvalidDataException`, `PersistenceException` | constructors, `getErrorCode`, `getMessage` |
| Validation (R6) | the seven validators | every public static method |
| Presentation (R7, V-05) | `ReturnReceiptFormatter`, `WarrantyCertificateFormatter` | the single `format` method of each one, its dependencies and the visibility they need from the model |

`Sale`, `SaleRepository`, `SaleService`, `ConsoleMenu`, `SaleMenu`, `AccessoryMenu`, `Main`,
the repositories and the services of the other modules are audited by their own authors and
are not audited again here.

## Summary

| Principle | Verdict | Findings |
|---|---|---|
| Single Responsibility Principle | Violation | V-01, V-03 |
| Open/Closed Principle | Violation | V-02 |
| Liskov Substitution Principle | Violation | V-01 |
| Interface Segregation Principle | Violation | V-01 |
| Dependency Inversion Principle | Violation | V-01 |

Three defects were registered as three findings. V-01 breaks four principles at once and is
described once, under the Interface Segregation Principle, because that is the principle that
dictates its correction; the other three it breaks are named inside that finding.
