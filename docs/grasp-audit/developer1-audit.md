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
- `CategoryDiscount.calculateDiscount` (lines 54-59) sums only the products of its own
  category, and each product answers with `getCategory()`. The knowledge it needs is its own
  category plus what each product is, and it obtains both without asking `Sale` for a total
  that does not exist.
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
| Evidence | Two promotions rebuild the base of the discount with the same loop: `PercentageDiscount.calculateDiscount` (lines 39-42) and `BulkPurchaseDiscount.calculateDiscount` (lines 47-50) each walk `sale.getProducts()` and add `product.getPrice()` of every unit. The value they are trying to produce already exists in `Sale.calculateSubtotal` (line 57) and is published by `Sale.getSubtotal` (line 193). |
| Explanation | The amount a discount is applied to is knowledge of the sale: how much it cost before any discount. `Sale` owns that number and already publishes it, but these two promotions ignore it and rebuild it from the list of products, so two classes carry a responsibility that one class already has. |
| Consequence | There are two answers to the same question and they can drift apart. If the base of a sale ever stops being the plain sum of the unit prices, for example when shipping or a tax is added, these two promotions keep summing prices and `Sale` keeps adding the extra, and the discount ends up calculated on a different base than the one printed in the receipt. The same loop is also copied into every new promotion that follows the same model. |
| Proposed solution | Have these two promotions read `sale.getSubtotal()` instead of walking the products, so the base of the discount is computed once, in the class that owns it. The rule of Requirement 2 is preserved: `getSubtotal()` returns the sum before discounts, so a promotion still never sees a discount already applied. |
| Other member's code | No. `Sale.calculateSubtotal` and `Sale.getSubtotal` belong to the Technical Leader, but the correction is entirely inside `PercentageDiscount` and `BulkPurchaseDiscount` and does not change them. |

**`CategoryDiscount` is not part of this violation.** Its loop (lines 54-59) is not a
duplicate of the subtotal: it holds the condition `if (targetCategory.equalsIgnoreCase(
product.getCategory()))` (line 56), so it adds the price of only the products of its own
category. That figure is not the total of the sale and `Sale` does not have it, because the
sale does not know which category a promotion was created for. Replacing that loop with
`getSubtotal()` would apply the percentage to every product of the sale instead of only to
the ones of the category, which is a different promotion and would break the behaviour that
Requirement 2 asks for. There is no duplicated calculation to remove there, so the promotion
is audited as correct in this pattern and its finding is not registered.


## Creator

**Verdict: correct.**

Correct applications:

- `ProductService.registerVideoGame` (lines 61–66) creates the `VideoGame` and
  `ProductService.registerConsole` (lines 89–94) creates the `Console`. The service is the
  class that receives every value of the new product from the caller and the one that has to
  add it to the catalogue it keeps in memory, so it is the right creator: it knows everything
  the constructor needs and it is the only class interested in the new object.
- No model class creates another model class. `Product`, `Accessory`, `Promotion`, `Warranty`
  and `Return` receive their collaborators already built and never build one, which keeps each
  of them from deciding the shape of a product it does not own.
- `GameZoneException` and its subclasses do not create exceptions; they are created by the code
  that detects the failure. That is the correct direction, because only the code that knows the
  problem can name it.
- The seven validators create nothing at all. They receive products, promotions, sales and
  returns already built and only answer whether they are acceptable, so none of them becomes a
  creator that should belong to a service.
- `Accessory.addCompatibleConsole` (line 106) creates only the entry it needs inside its own
  list, which is inside its own data and not an object of another class.

## Controller

**Verdict: not applicable.**

The pattern does not apply to any class in my scope. A controller receives an event from
outside the system, decides which use case has to run and delegates it to the objects that
know how. In the audited code no class receives an event: the only entry from the user is
`ConsoleMenu`, audited by the Technical Leader, and it delegates to `SaleService` and to
`registerVideoGame` and `registerConsole` of `ProductService`.

The validators of `com.gamezone.validation` are not controllers. They are called with values
already read, each one answers a single question, returns nothing and never decides which
operation should run, so giving them a controller role would mean giving them decisions that
are not theirs. The leader found the real controller defect of the system in `ConsoleMenu`
and in `SaleService`, both outside my scope.
## Low Coupling

**Verdict: violation** (one point, V-02, registered under Polymorphism).

Correct applications:

- `CategoryDiscount.calculateDiscount` (line 55) asks each product for its category with
  `getCategory()` and never mentions `VideoGame` or `Console`, so a promotion does not depend on
  the concrete products it can be applied to.
- `AccessoryValidator.validateCompatibleConsole` (line 62) identifies a console through the
  behaviour of `getCategory()` instead of importing the class `Console`, so the validation layer
  does not need to know the concrete type of a product.
- The seven validators depend on `model` and `exceptions` only. None of them imports
  `persistence`, `service` or `ui`, so a change in the storage or in the menu does not reach the
  validations and a new interface would reuse them without change.
- `GameZoneException` and its subclasses depend on nothing inside the project, so the failures of
  any module can be caught by catching the base type.
- `Promotion.calculateDiscount` (line 39) receives a `Sale` and works only through its public
  interface, and the three promotions never import each other.
- `Return.discountRatio` (line 99) reads `sale.getSubtotal()` and `sale.getDiscountAmount()`
  through getters instead of manipulating the fields of `Sale`, which belong to the Technical
  Leader.

The only violation of this pattern is the defect of V-02, which is registered once in the
Polymorphism section: see V-02 there. In short, `ProductRepository` imports the two concrete
classes `Console` and `VideoGame` and casts to them, so a repository that should depend only on
the abstraction it stores depends on the two implementations that exist today. It is not
repeated here because the same edit corrects the three patterns.

## High Cohesion

**Verdict: violation** (one point, V-03).

Correct applications:

- Each of the three promotions has exactly one reason to change: the rule of its own discount.
  `PercentageDiscount` only knows a percentage, `CategoryDiscount` only knows a category and
  `BulkPurchaseDiscount` only knows a minimum quantity.
- `ProductService` groups operations on the catalogue: registering products, finding them by
  identifier and updating their stock. Its `restoreStock` is audited by the Technical Leader.
- The seven validators each answer one family of questions: `ProductValidator` the eight checks
  of a product, `PromotionValidator` those of a promotion and its code, `WarrantyValidator` the
  date rules of a warranty. None of them checks something that belongs to another module.
- `GameZoneException` and its subclasses carry only a failure: a code, a message and a cause.
- `BasicWarranty` and `ExtendedWarranty` answer only with their own duration, type and cost.

Violation V-03:

| Field | Content |
|---|---|
| ID | V-03 |
| Pattern | High Cohesion |
| Module | Catalogue (Taller 1), returns (Requirement 3), warranties (Requirement 4) |
| Evidence | Four classes of the `model` package build the text that the user reads: `Product.getDescription` (line 33, abstract, answered by `VideoGame.getDescription`, line 37, and by `Console`), `Accessory.getDescription` (line 65), `Return.generateReturnReceipt` (line 119, which writes lines 125–145) and `Warranty.generateWarrantyCertificate` (line 123, which writes lines 126–145). The last two also compose the text with `StringBuilder` and `String.format(Locale.US, ...)`, and decide the layout of the document with `if (product != null)`, `if (sale != null)` and `if (active)`. |
| Explanation | A description, a receipt and a certificate are presentation. A model class that owns data and rules now also owns the wording and the layout of what the user reads, so it changes for two unrelated reasons: a new business rule and a new wording. The three kinds of document are different responsibilities that happen to share the same classes. |
| Consequence | Every change in the wording of the menu reaches the domain classes. A second interface, for example a web one, would have to carry the Spanish text of the console inside the model. The layout of a receipt and the layout of a certificate also grow inside the classes that should only know the numbers. |
| Proposed solution | Move the wording to the presentation layer: a formatter per document, next to `ConsoleMenu`, that receives the model objects and reads their getters, the way the receipt formatter already proposed by the leader would do for `Sale`. The model keeps the numbers and the rules and stops composing sentences. |
| Other member's code | Partly. The same defect exists in `Sale.generateReceipt`, which belongs to the Technical Leader, and the correction covers both, so both should be registered once in the consolidation instead of twice. |
## Polymorphism

**Verdict: violation** (two points, V-02 and V-04). The pattern is applied correctly in the
rest of the audited code.

Correct applications:

- `Warranty` declares three abstract methods (lines 45, 52 and 58) and `BasicWarranty` and
  `ExtendedWarranty` answer each one with its own value. The base class computes the end date with
  `getDurationInMonths()` (line 36) and prints the cost with `getAdditionalCost()` (line 143)
  without ever asking what type of warranty it has.
- `Promotion` declares `calculateDiscount` (line 39) and `getDescription` (line 46) abstract, and
  `SaleService`, audited by the Technical Leader, applies a promotion through the base type.
- `Product.getDescription` (line 33) and `Product.getCategory` (line 41) are abstract, and
  `VideoGame` and `Console` answer both, so the catalogue is used through the base type.
- `Accessory.getAccessoryType` (line 55) is abstract and `Controller`, `Cable` and `Memory` answer
  it, which is what lets `ConsoleMenu` select an accessory without a case per subclass.
- `Return.calculateRefundAmount` (line 71) works with any `Product` and never asks for its type,
  so a new product is returned like the existing ones.

Violation V-02:

| Field | Content |
|---|---|
| ID | V-02 |
| Pattern | Polymorphism |
| Module | Catalogue persistence (Taller 1) |
| Evidence | `ProductRepository.saveAll` decides with `if (product instanceof VideoGame)` (line 50) and `else if (product instanceof Console)` (line 60), and casts the object at lines 51 and 61. `ProductRepository.loadAll` decides with `if ("VIDEOGAME".equalsIgnoreCase(fields[0].trim()))` (line 116) and `else if ("CONSOLE".equalsIgnoreCase(fields[0].trim()))` (line 119). The repository imports both concrete classes (lines 4 and 6) and writes the columns of each type itself (lines 52–58 and 62–69). |
| Explanation | The repository asks what type each product is and then extracts the columns of that type, instead of asking the product to write and read its own row. The knowledge of the extra columns of a video game and of a console lives in the persistence layer, not in the product that owns that data. The same defect also produces the coupling of V-02 in Low Coupling and the cost of variation described in Protected Variations. |
| Consequence | A new product type does not fit without opening the repository: there is nothing in `Product` to implement, so the change is not local and no compiler error points at it. A mistake in the file format is made in the repository and the product cannot protect itself from it, because it does not know the format. |
| Proposed solution | Give `Product` the knowledge of its own row, with an abstract method that writes it and a construction that reads it, so each subclass answers for its own columns and `ProductRepository` only walks the list and writes what each product returns. Polymorphism is the pattern that guides this correction; the improvement in coupling and in the cost of variations is a consequence of the same edit. |
| Other member's code | No. `AccessoryRepository` has the same shape with the accessory types and belongs to Developer 2; it is mentioned here only so the consolidation can decide whether the two repositories are corrected together. |

Violation V-04:

| Field | Content |
|---|---|
| ID | V-04 |
| Pattern | Polymorphism |
| Module | Warranties (Requirement 4) |
| Evidence | `Warranty.coverageDescription` (line 155) is a private method of the base class whose body compares texts, `if ("Garantía Extendida".equals(getWarrantyType()))` (line 156), and it is used by `generateWarrantyCertificate` (line 141) to write the line "Cobertura: ...". |
| Explanation | The base class discovers the type of the object by comparing the text the subclass returns. The behaviour is chosen by the name of the type, so it does not travel with the behaviour of the subclass: `ExtendedWarranty` knows how long it lasts and how much it costs, but the base class has to recognise its name to know what it covers. |
| Consequence | A warranty whose name is written differently, with another accent or in another language, silently receives the coverage of the basic warranty and the certificate is wrong with no error and no exception. A third warranty type needs a new `if` inside `Warranty`, so a change in a subclass becomes a change in its parent. |
| Proposed solution | Move the coverage to the subclass. `coverageDescription()` becomes abstract in `Warranty` next to `getWarrantyType`, and `BasicWarranty` and `ExtendedWarranty` answer with their own text, so the certificate keeps calling the method without knowing the type. |
| Other member's code | No. |
## Pure Fabrication

**Verdict: correct.**

Correct applications:

- The seven validators of `com.gamezone.validation` add behaviour without adding data: each one
  is declared `final`, has a private constructor and offers only static methods, so it cannot be
  instantiated or extended and it holds no state. They exist because these checks did not belong
  to the model: `ProductValidator` (96 lines) groups the eight checks of a product,
  `PromotionValidator` (122 lines) those of a promotion and of its code, and
  `WarrantyValidator` (53 lines) the date rules of a warranty.
- These classes are not invented to move a method away from where it belongs. Every check
  answers a question about a value that comes from outside, and the classes that receive those
  values were audited as correct in Information Expert, because the model already holds
  everything it needs.
- `GameZoneException` is also a fabricated concept: a failure is not a `Product` nor a `Sale`,
  so the class has no data of its own beyond the code and the message that identify it.
- The price format with `String.format(Locale.US, "$%,.2f", price)` is repeated in several model
  classes, but it is not extracted into a fabricated formatter on purpose: moving it would hide
  the money behind another indirection while the real problem of V-03 is the wording of the
  documents, which the presentation layer will resolve with its own formatters.

## Indirection

**Verdict: correct.**

Correct applications:

- `Promotion` (line 12) is the indirection that lets `SaleService` and `ConsoleMenu` work with
  promotions without knowing the three concrete types. The service, audited by the Technical
  Leader, only calls `calculateDiscount` and `getDescription` on the base type.
- `Accessory.getAccessoryType` (line 55) is the indirection behind
  `ConsoleMenu.readAccessoryType`: the menu converts a number into a code and builds the
  accessory, and it never keeps a case per accessory subclass.
- `GameZoneException` is indirection for the handlers: `ConsoleMenu.runSafely`, audited by the
  Technical Leader, catches the base type and one handler covers the four subclasses, so adding a
  fifth kind of failure does not add a fifth catch.
- `Warranty` (line 14) is indirection for the sale: the sale keeps a warranty and never asks
  whether it is basic or extended, it only reads its duration and its cost.
- The `validation` package is indirection between the interface and the model: `ConsoleMenu`
  obtains a checked object instead of repeating the format rules in each of its submenus.
## Protected Variations

**Verdict: violation** (three points: V-05, and the consequences already registered as V-02
and V-04).

Correct applications:

- The services work with the base type `Product` and call `hasStock` (line 49) and
  `decreaseStock` (line 61), so a change in the rule of the stock stays inside `Product` and
  does not reach `ProductService`.
- `Return.calculateRefundAmount` (line 71) sums the refunds of the returned products without
  asking for their type, so a new product type is returned like the existing ones and `Return`
  does not change.
- A new kind of failure is added as a new subclass of `GameZoneException`, and the handlers keep
  catching the base type, so a new failure does not change the code that catches them.
- The sale applies a promotion and stores a warranty through their base types, so a new promotion
  or a new warranty does not change `Sale`.

Violation V-05:

| Field | Content |
|---|---|
| ID | V-05 |
| Pattern | Protected Variations |
| Module | Validation (Requirement 6) |
| Evidence | The category that identifies a console is written as a text in the validation layer: `AccessoryValidator.validateCompatibleConsole` (line 62: `if (!product.getCategory().equalsIgnoreCase("CONSOLE"))`) and `SaleValidator.validateExtendedWarrantyRequest` (line 74: `if (product.getCategory().equalsIgnoreCase("CONSOLE"))`). |
| Explanation | Deciding whether something is a console by comparing a string means the answer depends on a value that lives in the product, and two validators repeat the comparison. The variation "how a console is identified" reaches the validation layer, whose only job should be to receive the answer and act on it. |
| Consequence | The same category is compared in four places in the system: these two validators, plus `ConsoleMenu.registerSale` (line 277) and `SaleService.assignWarranties` (line 311), both audited by the Technical Leader. Changing how a console is identified, or adding a portable console, means editing the menu, the service and the two validators; forgetting one makes the system answer differently depending on the path by which the sale is registered. |
| Proposed solution | Ask the product instead of comparing its text: a behaviour on `Product` such as `isConsole()`, or a small fabricated class in `model` that decides which products accept an extended warranty, so `ConsoleMenu`, `SaleService`, `SaleValidator` and `AccessoryValidator` delegate to one answer. The leader already proposed this in his V-02; this finding adds `AccessoryValidator`, which was not in his evidence. |
| Other member's code | Partly. `SaleValidator` and `AccessoryValidator` are mine, but the rule is also in `ConsoleMenu` and in `SaleService`, which belong to the Technical Leader. It is reported because his V-02 did not include `AccessoryValidator`, and the consolidation should merge both into a single decision. |

The variations of V-02 and of V-04 are not registered again here. Adding a product type
already forces an edit of `ProductRepository`, and adding a warranty type already forces an
edit of `Warranty.coverageDescription`; both are already described and corrected in their own
finding.

## Findings reported for the consolidation

These are not new defects. They are findings of this audit that touch code of another member
and that the Technical Leader must consolidate with his own:

- **V-03 and the leader's V-03 describe the same defect.** The model classes build the Spanish
  text of the documents: `Sale.generateReceipt` on his side, and
  `Return.generateReturnReceipt`, `Warranty.generateWarrantyCertificate` and the
  `getDescription` of `Product` and of `Accessory` on mine. The correction is the same one, a
  formatter per
  document in the presentation layer, so both findings should become one.
- **V-05 and the leader's V-02 describe the same rule.** "Only consoles accept an extended
  warranty" is spread over four classes. He already merged his own three occurrences into one
  finding; mine adds `AccessoryValidator` (line 62) as a fourth occurrence.
- **V-02 also appears in `AccessoryRepository`**, which belongs to Developer 2, with the same
  branches on the accessory types. The leader decides whether both repositories are corrected
  in the same refactoring.
- **V-01 depends on an answer from the leader.** The correction reads `Sale.getSubtotal()`,
  which belongs to him. The audit needs him to confirm that `getSubtotal()` returns the sum
  before discounts, so that a promotion still never sees a discount already applied. If he
  prefers another public method on `Sale`, the correction stays inside my three promotion
  classes.
- **V-03 is a boundary correction.** Moving the wording out of the model touches the console
  that prints the documents, so `ConsoleMenu` is affected even though it was not audited here.