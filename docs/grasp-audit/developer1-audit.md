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

**Verdict: violation** (one point, V-01). The pattern is applied correctly in the rest of
the audited code.

Correct applications:

- `Sale.canBeReturned` (line 241) reads only attributes of `Sale`, its own date and the
  current date. Nothing else is needed to answer, so `Sale` does not have to ask anyone.
- `Product.hasStock` (line 49) and `Product.decreaseStock` (line 61) work with the stock
  attribute of the same object and with the quantity given by the caller. The rule "a
  product cannot go below zero units" stays with the product that owns the number.
- `Accessory.isCompatibleWith` (line 78) answers from the list of compatible consoles that
  the accessory itself holds, and `getCompatibleConsoleIds` (line 131) returns a copy of
  it. No other class needs to know which consoles are compatible.
- `Return.calculateRefundAmount` (line 71) sums the refunds of the returned products and the
  proportional part of the warranty. What it needs is the list of returned products and the
  sale it belongs to, and it holds both.
- `Warranty.isActive` (line 68) compares its own dates with the date given by the caller,
  and the private calculation of the end date (line 36) uses its own start date with the
  duration that its subclass answers.
- `GameZoneException.getErrorCode` and the four subclasses build their own message from
  their own attributes, so no exception has to ask another one how to describe itself.

Violation V-01:

| Field | Content |
|---|---|
| ID | V-01 |
| Pattern | Information Expert |
| Module | Promotions (Requirement 2) |
| Evidence | The same calculation of the base of a discount is written three times: `PercentageDiscount.calculateDiscount` (lines 40–42), `CategoryDiscount.calculateDiscount` (lines 55–58) and `BulkPurchaseDiscount.calculateDiscount` (lines 48–50) each walk `sale.getProducts()` and add `product.getPrice()`. The value they are trying to produce already exists in `Sale.calculateSubtotal` (line 57) and is published by `Sale.getSubtotal` (line 193). |
| Explanation | The amount a discount is applied to is knowledge of the sale: how much it cost before any discount. `Sale` owns that number and already publishes it, but the three promotions ignore it and rebuild it from the list of products, so three classes carry a responsibility that one class already has. |
| Consequence | There are two answers to the same question and they can drift apart. If the base of a sale ever stops being the plain sum of the unit prices, for example when shipping or a tax is added, the promotions keep summing prices and `Sale` keeps adding the extra, and the discount ends up calculated on a different base than the one printed in the receipt. The same loop is also copied into every new promotion. |
| Proposed solution | Have the three promotions read `sale.getSubtotal()` instead of walking the products, so the base of a discount is computed once, in the class that owns it. The rule of Requirement 2 is preserved: `getSubtotal()` returns the sum before discounts, so a promotion still never sees a discount already applied. |
| Other member's code | No. `Sale.calculateSubtotal` and `Sale.getSubtotal` belong to the Technical Leader, but the correction is entirely inside the three promotion classes and does not change them. |

