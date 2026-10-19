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
## Open/Closed Principle

**Verdict: violation** (one point, V-02).

Correct applications:

- A new promotion type is added by writing one class: `Sale` stores a `Promotion` and applies it
  through `calculateDiscount` (line 40), `getDescription` (line 47), `getTypeCode` (line 55) and
  `getExtraFields` (line 66), so no class that uses a promotion has to be edited.
- A new warranty type is added by writing one class. The sale stores a `Warranty` and reads its
  duration with `getDurationInMonths` (line 44) and its cost with `getAdditionalCost` (line 57),
  so it never learns what kind of warranty it is holding.
- A new accessory type is added by writing one class: `getAccessoryType` (line 55 in `Accessory`)
  is the only member that names the kind, and `ConsoleMenu` reads it without a case per subclass.
- A new kind of failure is added as a new subclass of `GameZoneException` (line 10), and the
  handler in `ConsoleMenu` keeps catching the base type, so no handler has to be edited.
- A new validation rule is added as a new static method in the validator of its own module, and
  no service has to change to call it.

Violation V-02:

| Field | Content |
|---|---|
| ID | V-02 |
| Principle | Open/Closed Principle |
| Module | Catalogue (Taller 1) and the rule "only consoles accept an extended warranty" (R6, V-04) |
| Evidence | The same question, "is this product a console", has two independent answers inside the same class. `Product.isConsole` (line 62) returns false as a default and `Console` overrides it to answer true, while `Product.getCategory` (line 43) is abstract and `Console` answers it with the text `CONSOLE`. The two answers are hard coded in the same subclass, one as a boolean literal and one as a string literal. Six call sites already use the predicate: `SaleService.assignWarranties` (line 312), `ReturnService` (line 181), `AccessoryMenu` (line 98), `SaleMenu` (line 81), `AccessoryValidator.validateCompatibleConsole` (line 62) and `SaleValidator.validateExtendedWarrantyRequest` (line 74). |
| Explanation | The two mechanisms were not introduced together: the predicate came with the correction of V-04 of the GRASP audit, and the category came earlier as the marker of the catalogue file. Nothing in the code ties them together, so a subclass that overrides one of them and not the other is perfectly valid Java and perfectly wrong for the store. The class was open for extension, and the extension it invites is the one that breaks it. |
| Consequence | A new kind of console, for example a portable console that belongs to the catalogue, has to be changed in two places instead of one, and nothing warns when only one of them was done. The rules that ask "is this a console" and the rules that filter by category can then answer differently about the same product, which is the exact defect that V-04 was registered to remove, reappearing one level below. |
| Proposed solution | Keep one answer and derive the other. The category cannot disappear, because `ProductRepository` writes it as the marker of the row, so the predicate should be derived from the same constant the subclass uses for its category, instead of a second hard-coded literal. Then a rename of the category or a new console type is a single change and the two answers cannot disagree. |
| Other member's code | Partly. The six call sites include `SaleService`, `SaleMenu` and `AccessoryMenu`, which belong to the Technical Leader, but the two literals that have to agree are both in `Product` and `Console`, which are mine, so the correction is mine. |
## Liskov Substitution Principle

**Verdict: violation** (one point, V-01, registered under the Interface Segregation Principle).

Correct applications:

- `Warranty.coverageDescription` (line 130) is abstract and each warranty answers with its own
  text, so a `BasicWarranty` can be used anywhere a `Warranty` is expected without the caller
  having to know which one it holds.
- `Product.isConsole` (line 62) has a default that is correct for most subclasses, and `Console`
  narrows it. The default is not a behaviour that only makes sense for the base class.
- `Accessory.getDescription` (line 65) builds the common part of the text and each accessory calls
  `super.getDescription()` and appends its own attributes (`Controller` line 54, `Cable` line 58,
  `Memory` line 59), so every accessory answers with a complete description and none of them has
  to be tested to know it is one.
- `Promotion.calculateDiscount` (line 40) can be answered with zero: a promotion that is not
  active returns zero, and a caller handles that the same way for the three types.
- `GameZoneException` and its subclasses keep the same contract: every one of them builds a
  message and offers `getErrorCode`, so catching the base type is enough and no subclass needs a
  special case.

Violation V-01 as it breaks this principle:

| Field | Content |
|---|---|
| ID | V-01 |
| Principle | Liskov Substitution Principle |
| Module | Catalogue (Taller 1), accessories (R1, R7) |
| Evidence | `Product` declares `public abstract List<String> getExtraFields();` (line 54) and `Accessory extends Product` (line 14 in `Accessory`), so the three accessories inherit the contract. `VideoGame` (line 65) and `Console` (line 74) answer with the three columns of `products.csv`, while `Controller` (line 43), `Cable` (line 46) and `Memory` (line 47) answer with the one or two columns of `accessories.csv`. Both answers satisfy the signature and both are read through the base type: `ProductRepository` line 56 and `AccessoryRepository` line 141. |
| Explanation | Substitution asks whether a subclass can be used wherever its parent is used, and here it can be, because the method exists and returns a list of strings. What it cannot do is return the *same meaning*. A caller that holds a `Product` and calls `getExtraFields()` receives columns, and there is nothing in the answer that says which file those columns belong to. The two hierarchies share one contract with two different meanings, which is the case where a substitutable type quietly hands back the wrong thing. |
| Consequence | A repository written for one file can be pointed at the other hierarchy: it compiles, because the signature is satisfied, and it writes a row with the columns of the other file. Nothing detects it at compile time and nothing fails at run time either, because the row is still a syntactically valid row. |
| Proposed solution | The correction is the one of V-01, written in the Interface Segregation section: stop making the two hierarchies share one contract for two files, either by publishing a marker that names the file next to the columns, or by declaring the column contract in the hierarchy that owns the file. |
| Other member's code | Yes. `ProductRepository` and `AccessoryRepository`, which are the two callers, belong to Developer 2. |