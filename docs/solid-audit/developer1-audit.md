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

## Single Responsibility Principle

**Verdict: violation** (two points, V-01 and V-03).

Correct applications:

- `ProductService` (line 38) groups the operations on the catalogue and nothing else: it
  registers, it finds and it updates stock. Each of its methods changes because one rule of the
  catalogue changed, never because of a rule of a different module.
- The seven validators of `com.gamezone.validation` are split by subject rather than gathered in
  one class: `ProductValidator` (5 methods), `PromotionValidator` (5), `ReturnValidator` (4),
  `AccessoryValidator` (3), `SaleValidator` (3), `PersonValidator` (3) and `WarrantyValidator`
  (2). Each one changes because its own family of checks changed.
- `Return` (line 70 `calculateRefundAmount`, line 103 `discountRatio`) holds the refund rules of
  one return and nothing else. After the correction of V-05 it no longer builds any document.
- `WarrantyCertificateFormatter` and `ReturnReceiptFormatter` do one thing each: write the text
  of one document. `Warranty` and `Return` do not write documents any more.
- `GameZoneException` (line 10) and its four subclasses carry only a failure: a code, a message
  and a cause. They change when the way the store reports a problem changes, and for no other
  reason.
- Each of the three promotions has exactly one reason to change: the rule of its own discount.

V-01 and V-03 are described here in the part each one adds to this principle. V-01 is registered
once, under the Interface Segregation Principle, where the whole defect and its correction are
written; what it adds to this principle is that `Product` and `Promotion` change because the
layout of a data file changed, which is a reason that has nothing to do with a rule of the store.

Violation V-03:

| Field | Content |
|---|---|
| ID | V-03 |
| Principle | Single Responsibility Principle |
| Module | Catalogue (Taller 1), accessories (R1), promotions (R2), returns (R3), warranties (R4), presentation (R7) |
| Evidence | The way an amount is written for the user is written literally, again, in seven places: `Accessory.getDescription` (line 67), `ReturnReceiptFormatter.format` (lines 63, 64, 66, 71 and 73) and `WarrantyCertificateFormatter.format` (line 67), all of them as `String.format(Locale.US, "$%,.2f", ...)`. The way a percentage is written is repeated in three more: `PercentageDiscount.getDescription` (line 53), `CategoryDiscount.getDescription` (line 73) and `BulkPurchaseDiscount.getDescription` (line 61), as `String.format(Locale.US, "%.2f%%", percentage)`. |
| Explanation | "How much is $2,500,000.00 in this store" is one single decision, and "a percentage is written with two decimals and a percent sign" is another one. Ten call sites repeat those two decisions verbatim, and each of them sits inside a class that was written for a different reason. A class that prints a receipt, a class that prints a certificate and a class that describes a video game all carry a copy of the money format, none of them owning it. |
| Consequence | Changing the money format, for example adding a currency code, moving the symbol or asking for more decimals, means editing ten lines spread over six classes in three packages. A line that is forgotten produces a document that shows one format right next to another, and nothing fails: the code still compiles and the store still runs. |
| Proposed solution | One small fabricated class with the two decisions in it, for example `Money.format(double)` and `Percent.format(double)`, and the ten call sites use it. It has no state and no dependency, it is exactly the Pure Fabrication case of the GRASP audit, and it gives the format a single owner. |
| Other member's code | Yes. `Sale.generateReceipt` (Technical Leader) writes money the same way, so the same correction covers it and it should be registered once in the consolidation rather than twice. |