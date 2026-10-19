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
| Evidence | The way an amount is written for the user is written literally, again, in seven places, and every one of them repeats the same format string: `Accessory.getDescription` (line 67), `ReturnReceiptFormatter.format` (lines 63, 64, 66, 71 and 73) and `WarrantyCertificateFormatter.format` (line 67), all of them as `String.format(Locale.US, "$%,.2f", ...)`. The way a percentage is written is repeated in three more: `PercentageDiscount.getDescription` (line 53), `CategoryDiscount.getDescription` (line 73) and `BulkPurchaseDiscount.getDescription` (line 61), as `String.format(Locale.US, "%.2f%%", percentage)`. The sale receipt is the tenth copy and the odd one out: `ReceiptGenerator` writes money with `String.format` and **without** a locale, at lines 35, 37, 39, 44 and 45, for example `String.format("  Subtotal: $%,.2f%n", sale.getSubtotal())` at line 37. |
| Explanation | "How much is $2,500,000.00 in this store" is one single decision, and "a percentage is written with two decimals and a percent sign" is another one. Ten call sites repeat those two decisions verbatim, and each of them sits inside a class that was written for a different reason. A class that prints a receipt, a class that prints a certificate and a class that describes a video game all carry a copy of the money format, none of them owning it. The tenth copy is worse than the other nine: nine of them at least pin the locale with `Locale.US`, and `ReceiptGenerator` does not, so its answer depends on the locale of the machine that runs the store. |
| Consequence | Changing the money format, for example adding a currency code, moving the symbol or asking for more decimals, means editing ten lines spread over six classes in three packages. A line that is forgotten produces a document that shows one format right next to another, and nothing fails: the code still compiles and the store still runs. The copy without a locale is already inconsistent today: on a machine whose default locale separates the thousands with a dot, as `es_CO` does, the sale receipt prints `$2.500.000,00` while the return receipt and the warranty certificate print `$2,500,000.00` for the same amount, with no error and no test failure. |
| Proposed solution | One small fabricated class with the two decisions in it, for example `Money.format(double)` and `Percent.format(double)`, and the ten call sites use it. The locale belongs inside that class, so the ten copies cannot disagree about it either and `ReceiptGenerator` stops depending on the machine it runs on. It has no state and no dependency, it is exactly the Pure Fabrication case of the GRASP audit, and it gives the format a single owner. |
| Other member's code | Yes. `ReceiptGenerator`, which is the copy without a locale, is in `com.gamezone.ui` and belongs to the Technical Leader, so the same correction covers it and it should be registered once in the consolidation rather than twice. |
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
| Other member's code | Partly. `ProductRepository` and `Product`, which publishes the layout, are both mine, so the catalogue half of V-01 can be corrected without anyone else. `AccessoryRepository` (line 141), the other caller of the same contract, belongs to Developer 2 and is the half that has to be agreed with him. |
## Interface Segregation Principle

**Verdict: violation** (one point, V-01).

Correct applications:

- The validation layer is not one big validator: it is seven classes split by subject, and the
  biggest of them, `ProductValidator`, offers five methods instead of the nineteen that a single
  class covering the whole store would need. A caller that only validates a product never depends
  on a single check of a warranty or of a promotion.
- `Promotion` is a small contract with four members that every promotion genuinely needs:
  `calculateDiscount` (line 40), `getDescription` (line 47), `getTypeCode` (line 55) and
  `getExtraFields` (line 66), plus the data accessors. None of the three subclasses was forced to
  answer something meaningless.
- `Warranty` is the same shape: four methods that a warranty cannot do without, `getDurationInMonths`
  (line 44), `getWarrantyType` (line 51), `getAdditionalCost` (line 57) and `coverageDescription`
  (line 130). After the correction of V-09 the base class no longer carries a behaviour that only
  the extended warranty has.
- `GameZoneException` (line 10) declares one member, `getErrorCode` (line 56), and each subclass
  adds nothing that a caller has to implement.
- The two formatters of `com.gamezone.ui` do not implement an interface, so no class is forced to
  offer a `format` it cannot build.

Violation V-01:

| Field | Content |
|---|---|
| ID | V-01 |
| Principle | Interface Segregation Principle |
| Module | Catalogue (Taller 1), accessories (R1, R7) |
| Evidence | `Product` declares one contract for the extra columns of a data file: `public abstract List<String> getExtraFields();` (line 54). `Accessory extends Product` (line 14 in `Accessory`), so the three accessories are forced to answer it. `VideoGame` (line 65) and `Console` (line 74) answer with the three columns of `products.csv`. `Controller` (line 43) answers with one column, its connection type; `Cable` (line 46) with two, its length and its connector type; `Memory` (line 47) with two, its capacity and its memory type, and those are the columns of `accessories.csv`. Neither repository is written against a narrower contract: `ProductRepository` line 56 and `AccessoryRepository` line 141 both read the answer through the same inherited method. |
| Explanation | The principle asks whether every class that inherits a contract can honour it, or whether it only inherits it because of the tree. An accessory is not stored in the product catalogue: it is in another file, with a different number of columns and a different meaning. Answering "which columns of `products.csv` are yours" is a question that does not exist for it, so the contract does not describe it. `Product` was made the parent of both hierarchies because it holds the identifier, the title, the price and the stock, and that single inheritance chain is what drags the file contract in with it. |
| Consequence | The two hierarchies are forced to share one contract with two meanings, so the answer cannot be used without knowing which hierarchy it came from. A repository written for one file compiles against the other and writes the wrong columns. A third catalogue that stores its own kind of item would have to be squeezed into the same method with a third meaning, and the alternative, ignoring it, leaves a subclass that answers with a value nothing reads. |
| Proposed solution | Segregate the two contracts. Either publish, next to the columns, a marker that names the file the product belongs to, so a caller can tell the two answers apart; or declare the column contract in a small interface owned by the persistence layer, for example `CatalogRow`, implemented by `VideoGame`, `Console` and each accessory, so each hierarchy declares only the file it is stored in. The second option also fixes the Dependency Inversion problem named below, because the domain would no longer know the column layout. Either way, `Product` stops carrying a contract that only some of its subclasses can honour. |
| Other member's code | Partly. `ProductRepository` is mine, and so are `Product`, `Accessory` and the three accessory subclasses, so the contract and the caller that serves the catalogue file are both under my responsibility. `AccessoryRepository` is the only part that belongs to Developer 2, and it is the half of the defect that has to be agreed with him, because the proposed correction changes the columns he writes. |

V-01 breaks three more principles at once, and they are named here instead of being registered
again:

- **Liskov Substitution**, because the signature is satisfied and the meaning is not, so a
  substitutable type can hand back the wrong thing. See the Liskov Substitution section.
- **Single Responsibility**, because `Product` and `Promotion` change when the layout of a data
  file changes, which is a reason that has nothing to do with a rule of the store.
- **Dependency Inversion**, because the domain, which is the high level, is the one that knows
  the column layout of the file, which is the low level detail. See the Dependency Inversion
  section.
## Dependency Inversion Principle

**Verdict: violation** (one point, V-01, registered under the Interface Segregation Principle).

Correct applications:

- `Accessory` holds `private List<String> compatibleConsoleIds` (line 16) instead of a list of
  `Console` objects, and `isCompatibleWith` (line 78) compares identifiers. The domain therefore
  never depends on the catalogue's object graph, and a console that is not loaded can still be
  named as a compatible one.
- The seven validators depend on `model` and on `exceptions` only. They never import
  `persistence`, `service` or `ui`, so replacing the way the store is saved, or writing a second
  interface, does not touch a single validation.
- The exceptions of `com.gamezone.exceptions` depend on nothing inside the project, and the menu
  catches `GameZoneException` (line 10), the abstract type, instead of one concrete failure. That
  is the dependency pointing the right way: from the interface to the detail.
- `ReturnReceiptFormatter` and `WarrantyCertificateFormatter` depend on `Return`, `Warranty`,
  `Product` and `Sale`, all of which are domain policy, and on nothing else. They do not import a
  repository, a service or a file, so the documents can be printed without any storage being
  present.
- `Return` (line 135 `getSale`) and `Warranty` (line 103 `getSale`) hold a `Sale`, which is
  another part of the same policy, not a mechanism underneath it.

Violation V-01 as it breaks this principle:

| Field | Content |
|---|---|
| ID | V-01 |
| Principle | Dependency Inversion Principle |
| Module | Catalogue (Taller 1), promotions (R2) |
| Evidence | The high level knows the details of the low level. `Product.getExtraFields` (line 54) is declared by the model and answered by the model, and what it returns is the column layout of `products.csv`: `VideoGame` line 65 returns platform, genre and age rating, `Console` line 74 returns brand, model and generation, which are the exact columns and the exact order of that file. `Promotion.getExtraFields` (line 66) does the same for `promotions.csv`, with the percentage and the type specific value. |
| Explanation | The dependency should go from the storage towards the policy, not the other way round. Here the policy has been made to depend on the storage format: the order and the number of the columns are decided by the domain classes, because the domain classes are the ones answering. `ProductRepository` is supposed to be the class that knows the file, and after this change it is the class that knows the least about it. |
| Consequence | Reordering, renaming or adding a column of `products.csv` is a change to `Product`, `VideoGame` and `Console`, reviewed as a change to the business model. The domain can no longer be reused by anything that stores the catalogue somewhere else, because its shape is fixed by one file. It is also what makes the Interface Segregation defect of V-01 possible: because the domain owns the layout, the two hierarchies were forced into a single method. |
| Proposed solution | Invert it: the persistence layer declares the layout and the domain does not know about it. A mapper in `persistence` per hierarchy, or the `CatalogRow` interface named in V-01 that the repositories consume and that the domain classes answer with attributes rather than with an ordered column list, moves the detail back to where it belongs. This is the same edit as the one proposed in V-01, so it is one correction and not two. |
| Other member's code | Partly. `ProductRepository` and `Product`, which is the class that publishes the layout, are both mine, so the catalogue half of this defect can be corrected without anyone else. `PromotionRepository` is the class that would own the layout of the promotion file and belongs to Developer 2, and `AccessoryRepository` is the other half of the two-meanings problem. |
## Findings registered for the consolidation

These are not new defects. They are findings of this audit that touch code of another member,
and that the consolidation has to merge with the other reports instead of counting them twice.

- **V-01 touches one repository of Developer 2, not two.** `ProductRepository` (line 56) is
  mine, and so are `Product`, `Accessory` and the three accessory subclasses, so the catalogue
  half of the correction is entirely under my responsibility and can be written without waiting.
  The half that has to be agreed with Developer 2 is `AccessoryRepository` (line 141), the other
  caller, because the proposed correction changes the columns it writes. The whole defect and
  its correction are in the Interface Segregation section.
- **V-03 touches `ReceiptGenerator` of the Technical Leader.** It is the tenth copy of the money
  format and the only one that does not pin the locale, so the same correction covers both and
  the two findings should become one, with a single fabricated formatter used by the call sites of
  all three members. The detail of the missing locale is in the Single Responsibility section.
- **V-02 touches the call sites, not the correction.** Six places ask `isConsole()` and two of
  them belong to the Technical Leader (`SaleService` line 312 and the two menus), but the two
  literals that have to agree are both in `Product` and `Console`, so the edit is mine and the
  callers do not change.
- **The audited code includes work from Requirement 7, and one finding is a consequence of it.**
  `getExtraFields` was added during the GRASP correction, to remove the `instanceof` chains from
  the repositories. It did remove them, and it moved the column layout into the domain, which is
  what V-01 registers. The consolidation should decide whether V-01 is a new correction or the
  second half of V-08 of the GRASP audit, because the same six classes are involved in both.
- **One line of this audit depends on an answer from the Technical Leader.** `Product.getExtraFields`
  exists because the repositories needed a marker and a column list. If the leader prefers the
  repositories to keep the layout and the model to publish attributes instead of an ordered list,
  V-01 becomes a change of `Product`, `Promotion` and `Accessory` only, with no repository
  touched. If he prefers a third catalogue in the future, the marker that names the file is the
  part that cannot be skipped.