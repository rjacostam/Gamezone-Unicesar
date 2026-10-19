# Developer 1 GRASP Audit — GameZone Unicesar

Author: Andrés Daniel Cuello Blanco (Developer 1). Audited code: `develop` at commit `6c10ce2`.

## Scope

Classes and methods audited, by module and requirement, following the responsibility
table of Requirement 7.

| Module (requirement) | Class | Methods audited |
|---|---|---|
| Catalogue (Taller 1) | `Product` | constructor, `getDescription`, `getCategory`, `hasStock`, `decreaseStock`, `setStock`, `getId`, `getTitle`, `setTitle`, `getPrice`, `setPrice`, `getStock`, `toString` |
| Catalogue (Taller 1) | `VideoGame` | constructor, `getDescription`, `getCategory`, `getPlatform`, `getGenre`, `getAgeRating` |
| Catalogue (Taller 1) | `Console` | constructor, `getDescription`, `getCategory`, `getBrand`, `getModel`, `getGeneration` |
| Catalogue persistence (Taller 1) | `ProductRepository` | constructor, `saveAll`, `loadAll`, `toLine`, `fromLine` and the private helpers they use |
| Catalogue service (Taller 1, R3) | `ProductService` | constructor, `registerVideoGame`, `registerConsole`, `listAllProducts`, `findById`, `updateStock`. `restoreStock` is audited by the Technical Leader |
| Accessories (R1) | `Accessory` | constructor, `getCategory`, `getAccessoryType`, `getDescription`, `isCompatibleWith`, `addCompatibleConsole`, `removeCompatibleConsole`, `getCompatibleConsoleIds`, `hasStock`, `decreaseStock`, `increaseStock` |
| Accessories (R1) | `Controller`, `Cable`, `Memory` | constructors and the methods inherited from `Accessory` that each one answers |
| Promotions (R2) | `Promotion` | `calculateDiscount`, `getDescription`, getters |
| Promotions (R2) | `PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount` | constructors, `calculateDiscount` and `getDescription` of each one |
| Warranties (R4) | `Warranty` | constructor, the private end-date calculation (line 36), `isActive`, `generateWarrantyCertificate`, `coverageDescription`, and the three abstract methods |
| Warranties (R4) | `BasicWarranty`, `ExtendedWarranty` | constructors and the three abstract methods of each one |
| Returns (R3) | `Return` | constructor, `calculateRefundAmount`, `generateReturnReceipt`, the private helpers `discountRatio` and `refundableAmount`, and the getters |
| Returns (R3) | `Sale` | `canBeReturned` only |
| Exceptions (R6) | `GameZoneException` | the three protected constructors and `getErrorCode` |
| Exceptions (R6) | `ResourceNotFoundException`, `BusinessRuleException`, `InvalidDataException`, `PersistenceException` | constructors, `getErrorCode`, `getMessage` |
| Validation (R6) | `ProductValidator`, `PromotionValidator`, `WarrantyValidator`, `SaleValidator`, `ReturnValidator`, `AccessoryValidator`, `PersonValidator` | every public static method |

`SaleService`, `SaleRepository`, `ConsoleMenu`, `Main` and `restoreStock` of
`ProductService` are audited by the Technical Leader and are not audited again here.

## Summary

| Pattern | Verdict | Findings |
|---|---|---|
| Information Expert | Violation | V-01 |
| Creator | Correct | — |
| Controller | Not applicable | — |
| Low Coupling | Violation | V-02 |
| High Cohesion | Violation | V-03 |
| Polymorphism | Violation | V-02, V-04 |
| Pure Fabrication | Correct | — |
| Indirection | Correct | — |
| Protected Variations | Violation | V-02, V-04, V-05 |

Four findings appear under more than one pattern, because a single defect can break
several patterns at the same time. Each defect is registered once, under the pattern that
guides its correction, and the other patterns it breaks are named inside that finding,
so no defect is described twice.

## Information Expert
