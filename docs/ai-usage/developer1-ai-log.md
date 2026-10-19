# AI Usage Log — Developer 1

Owner: Andrés Daniel Cuello Blanco

Each interaction with an AI tool is recorded as one entry with the fields below.

## Entry template

| Field | Content |
|---|---|
| Date | |
| Tool | |
| Phase and branch | |
| Goal | |
| Query | |
| Answer | |
| Decision | |
| Related commit |

---

## Entries

### Entry 1

| Field | Content |
|---|---|
| Date | 2026-10-17 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Taller 1 — `feature/product-module` |
| Goal | Understand which classes of the product module are mine before writing any code |
| Query | Which classes does Developer 1 own in the product module and what contract does docs/class-diagram.md define for each one? |
| Answer | Product (abstract), VideoGame, Console in the model layer, plus ProductRepository in persistence and ProductService in service. The class diagram fixes the attributes, the protected constructor, the abstract getDescription(), hasStock(), decreaseStock() and the service methods registerVideoGame, registerConsole, listAllProducts, findById and updateStock. |
| Decision | Accepted. Followed the class diagram exactly instead of inventing extra attributes or methods, because the diagram is the mandatory reference for the implementation. |
| Related commit | feat: add Product abstract class with common attributes and inventory rules |

### Entry 2

| Field | Content |
|---|---|
| Date | 2026-10-17 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Taller 1 — `feature/product-module` |
| Goal | Decide how to rebuild the Product class in the two concrete product types |
| Query | Should VideoGame and Console extend Product or be a separate hierarchy? |
| Answer | Extending Product reuses id, title, price and stock and lets the service hold a single List&lt;Product&gt;. A separate hierarchy would duplicate those four attributes and the inventory behaviour. |
| Decision | Accepted inheritance, with the constructor protected so only the subclasses can build a product, and getDescription() declared abstract so the compiler forces each subclass to write its own. |
| Related commit | feat: add VideoGame subclass with platform genre and age rating |

### Entry 3

| Field | Content |
|---|---|
| Date | 2026-10-17 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Taller 1 — `feature/product-module` |
| Goal | Apply the team review that rejected the first attempt of this branch |
| Query | The review says two classes are missing, the JavaDoc is incomplete and the user-visible text is in English. What has to change? |
| Answer | Add ProductRepository and ProductService, write descriptive JavaDoc sentences on every public method instead of only a @return tag, and translate every string the user sees in the menu into Spanish while keeping identifiers and JavaDoc in English. |
| Decision | Accepted all three. The Spanish rule was applied to getDescription() and to every exception message, because the menu prints them; identifiers, commit messages and JavaDoc stay in English as the workshop demands. |
| Related commit | feat: add ProductRepository with CSV saveAll and loadAll methods |

### Entry 4

| Field | Content |
|---|---|
| Date | 2026-10-17 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Taller 1 — `feature/product-module` |
| Goal | Choose the CSV layout that lets loadAll rebuild the correct subclass |
| Query | How should the catalogue file store two different product types in one file? |
| Answer | A type marker in the first column (VIDEOGAME or CONSOLE), then the four common attributes, then three extra columns whose meaning depends on the marker. The marker decides which class is instantiated when the line is read. |
| Decision | Accepted. Chose a single products.csv for the whole catalogue instead of two files, and made the repository skip the header, short lines and lines with unparsable numbers so a damaged file cannot stop the application. |
| Related commit | fix: skip catalogue lines with unparsable price or stock values |

### Entry 5

| Field | Content |
|---|---|
| Date | 2026-10-17 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Taller 1 — `feature/product-module` |
| Goal | Make sure the service does not let invalid products into the catalogue |
| Query | Where should the validation of identifier, title, price and stock live? |
| Answer | In the service, because ProductService is the only class that registers products and it is the layer that applies business rules before touching the repository. |
| Decision | Accepted a private validateProductData method called by both register methods, plus the duplicate identifier check. Rejected putting the checks in the repository, because a repository should only read and write files. |
| Related commit | fix: validate identifier title price and stock on product registration |

### Entry 6

| Field | Content |
|---|---|
| Date | 2026-10-17 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Taller 1 — `feature/product-module` |
| Goal | Verify the module works together with the person module already merged in develop |
| Query | Does the product module compile and behave correctly next to the Person, Customer, Seller, PersonRepository and PersonService classes that Dev 2 already merged? |
| Answer | Yes. A full compile of model, persistence, service and ui together returned no errors, and a run registering two products, updating stock and reloading from the file showed the catalogue persisted correctly and rejected duplicate identifiers, blank identifiers and insufficient stock with Spanish messages. |
| Decision | Accepted. Confirmed the layered dependencies hold: the model knows nothing about files, and only the service calls the repository. No change needed. |
| Related commit | feat: add preloaded video games and consoles in data catalogue |

### Entry 7

| Field | Content |
|---|---|
| Date | 2026-10-17 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Taller 1 — `feature/product-module` |
| Goal | Check the number of atomic commits required by the workshop |
| Query | How many commits does the branch need and are they atomic? |
| Answer | The workshop asks for at least twelve atomic commits per member. The branch now has eleven code and data commits plus this documentation entry, and each one changes a single logical thing. |
| Decision | Accepted splitting the work into small steps, such as separating the registration methods from the listing and stock methods, instead of committing the whole service at once. |
| Related commit | docs: record product module work in developer1 AI usage log |

---

## Requirement 1 - Accessories

### Entry 8

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 1 - `feature/accessory-module` |
| Goal | Decide how to build the accessory hierarchy so the sales keep working unchanged |
| Query | Should the accessories extend Product or form a separate hierarchy? |
| Answer | They must extend Product, because an accessory is sold the same way as a video game or a console and Sale already keeps a List&lt;Product&gt;. A separate hierarchy would duplicate the four common attributes and would force Sale to keep a second list. |
| Decision | Accepted. Accessory extends Product and adds only compatibleConsoleIds. The compatibility is stored as console ids and not as Console objects, so Console does not change and the products module is not touched. |
| Related commit | feat: add Accessory abstract class with console compatibility |

### Entry 9

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 1 - `feature/accessory-module` |
| Goal | Avoid a warning the compiler reported when compiling the new base class |
| Query | Why does javac warn about a possible this escape in the Accessory constructor? |
| Answer | The constructor called addCompatibleConsole, which is a public method a subclass could override, so an overridden version could run before the subclass was fully initialized. |
| Decision | Accepted moving the check into a private containsConsoleId method that the constructor reuses, so the constructor no longer calls an overridable method. Rejected making the add methods final, because the class diagram does not ask for that. |
| Related commit | fix: avoid calling overridable method from Accessory constructor |

### Entry 10

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 1 - `feature/accessory-module` |
| Goal | Verify the accessory hierarchy behaves as the diagram describes |
| Query | Does the description, the type discriminator and the compatibility behave correctly on the three accessory types? |
| Answer | Yes. The descriptions come out as "Accesorio [A-01] Control DualSense | Precio: $350,000.00 | Stock: 8 | Compatible con: [C-01] | Conexion: Inalambrico", the discriminators return CONTROLLER, CABLE and MEMORY, isCompatibleWith ignores letter case, duplicated and blank ids are ignored, and removing a console works ignoring case too. |
| Decision | Accepted. The price uses Locale.US so the format does not depend on the language of the machine, and it matches the format the leader asked for. |
| Related commit | feat: add Memory accessory with capacity and memory type |

---

## Requirement 2 - Promotions

### Entry 11

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 2 - `feature/promotion-module` |
| Goal | Decide how the three promotion types share their data without repeating code |
| Query | How do the three promotions keep their common data while each one calculates its discount in a different way? |
| Answer | An abstract Promotion class holds id, name, startDate and endDate, declares isActive as a concrete method because the rule is the same for all, and declares calculateDiscount and getDescription as abstract so each subclass writes its own. |
| Decision | Accepted. isActive compares startDate &lt;= date &lt;= endDate with !isBefore and !isAfter, both limits included, and returns false when a date is missing. |
| Related commit | feat: add Promotion abstract class with validity period |

### Entry 12

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 2 - `feature/promotion-module` |
| Goal | Know how CategoryDiscount can tell which products belong to the target category |
| Query | Should CategoryDiscount use instanceof on the product types, or ask the product for its category? |
| Answer | The class diagram adds an abstract getCategory() to Product, implemented as VIDEOGAME, CONSOLE and ACCESSORY, and CategoryDiscount compares product.getCategory() with the target category. Using instanceof would couple the promotion to every product class, so a new product type would force a change in CategoryDiscount. |
| Decision | Accepted getCategory() and put it in Product, VideoGame, Console and Accessory. Accessory answers in the abstract class because every accessory shares the same category, so the three accessory types do not repeat it. Comparison uses equalsIgnoreCase so the target category works with any letter case. |
| Related commit | feat: add getCategory discriminator to the product hierarchy |

### Entry 13

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 2 - `feature/promotion-module` |
| Goal | Confirm the three discount rules calculate the right amount |
| Query | Do the three calculateDiscount implementations return the right value on a real sale? |
| Answer | Yes. On a sale of two video games and a console, PercentageDiscount 5 percent returned 150000; CategoryDiscount 20 percent on VIDEOGAME returned 100000, ignoring the console; BulkPurchaseDiscount 10 percent with a minimum of 3 returned 0 on a sale of 2 units and 310000 on a sale of 3 units. A sale outside the validity period and a null sale both returned 0. |
| Decision | Accepted computing the base by summing sale.getProducts() instead of using the sale total, so a promotion never depends on a discount already applied. |
| Related commit | feat: add BulkPurchaseDiscount promotion type with minimum quantity |

### Entry 14

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 2 - `feature/promotion-module` |
| Goal | Fix the percentage shown in the promotion descriptions |
| Query | The description printed "5,00000%" instead of a clean percentage. Which format should be used? |
| Answer | The %g format printed five decimals and used the decimal separator of the machine. The %.2f format with Locale.US prints 5.00% and 12.50% and does not depend on the language of the machine. |
| Decision | Accepted String.format(Locale.US, "%.2f%%", percentage) in the three classes, the same approach already used for the accessory price, so the two modules print prices and percentages in the same way. |
| Related commit | fix: format promotion percentage with two decimals in descriptions |
---

## Requirement 4 - Warranties

### Entry 15

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 4 - `feature/warranty-module` |
| Goal | Decide how the end date of a warranty should be computed |
| Query | Should the end date be received in the constructor or computed from the duration? |
| Answer | The class diagram says the constructor receives only the start date and computes endDate as startDate.plusMonths(getDurationInMonths()), so the duration is written in one place and the end date can never disagree with the type. |
| Decision | Accepted computing the end date in the constructor of Warranty. This makes the compiler warn about a possible this escape, because getDurationInMonths() is called before the subclass is initialized, but it is safe here: both subclasses return a constant and never read their own fields. I kept it because the diagram asks for it. |
| Related commit | feat: add Warranty abstract class linked to product and sale |

### Entry 16

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 4 - `feature/warranty-module` |
| Goal | Decide how the warranty should keep its product and its sale |
| Query | Should Warranty store the Product and Sale objects or only their identifiers? |
| Answer | The diagram and the leader asked for direct references to Product and Sale, and the certificate needs the title of the product and the name of the customer, both reached through those objects. |
| Decision | Accepted storing the objects. Rejected storing only identifiers, because then the certificate would need the catalogue and the sales passed in every call. There are no setters, because a granted warranty never changes. |
| Related commit | feat: add warranty certificate generation in Spanish |

### Entry 17

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 4 - `feature/warranty-module` |
| Goal | Write the text of what each warranty covers without adding a method to the design |
| Query | The certificate must show the coverage, but the diagram only allows three abstract methods, so where does that text come from? |
| Answer | The diagram does not declare a method for the coverage, so I read it from getWarrantyType(): only "Garantía Extendida" also covers accidental damage, and the basic one covers factory defects only. |
| Decision | Accepted a private coverageDescription method that uses the type name, which keeps the public API exactly as the diagram shows. Rejected adding a fourth abstract getCoverage method, because the leader asked to follow the diagram exactly. |
| Related commit | feat: add warranty certificate generation in Spanish |

### Entry 18

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 4 - `feature/warranty-module` |
| Goal | Verify the two warranty types calculate their duration, their cost and their period correctly |
| Query | Do the three abstract answers and the certificate behave as the diagram describes? |
| Answer | Yes. On a console of 2500000 sold on 2026-10-18, BasicWarranty reported 6 months, "Garantía Básica", cost 0 and ends on 2027-04-18; ExtendedWarranty reported 12 months, "Garantía Extendida", cost 250000 and ends on 2027-10-18. isActive returned true on the start date and on the end date, and false one day later or before the start. The certificate showed the id, the type, the product, the sale, the customer, both dates, the coverage, the cost and whether it is active today. |
| Decision | Accepted. The cost uses Locale.US with two decimals so the certificate looks the same on any machine, the same way as the accessory price and the promotion percentages. |
| Related commit | fix: read product through its getter in extended warranty cost |
---

## Requirement 3 - Returns

### Entry 19

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 3 - `feature/return-module` |
| Goal | Decide how Return keeps its link with the original sale |
| Query | Should Return store the Sale object or only its identifier? |
| Answer | The diagram asks for a direct reference to Sale, and the receipt needs the date of the sale and the name of the customer, both reached through that object. |
| Decision | Accepted storing the Sale object. The returned units are kept in a copy of the list, both in the constructor and in the getter, so the return can never be changed from outside. There are only getters because a registered return never changes. |
| Related commit | feat: add Return class linked to the original sale |

### Entry 20

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 3 - `feature/return-module` |
| Goal | Decide where the refunded amount is computed |
| Query | Should the refund be calculated in the constructor, in the service or in the file? |
| Answer | The diagram says the constructor calls calculateRefundAmount(), and that method sums the prices of the returned units, stores the result and returns it. |
| Decision | Accepted computing it in the constructor, so a Return always knows its amount. This makes the compiler warn about a possible this escape, because a public method is called before the object is fully initialized. I kept it because the diagram asks for it, and the same warning already exists in the Sale class written by the leader for the same reason. |
| Related commit | feat: add refund amount calculation for the returned units |

### Entry 21

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 3 - `feature/return-module` |
| Goal | Place canBeReturned in the right class |
| Query | Should the 30 day rule be checked in Sale or in the return service? |
| Answer | The diagram adds canBeReturned() to Sale, counting calendar days with ChronoUnit.DAYS.between(date, LocalDate.now()) and accepting values from 0 to 30. |
| Decision | Accepted putting it in Sale, next to its own date, and changed nothing else in that class: only the new method and the ChronoUnit import were added. A sale dated in the future returns false, because the number of days would be negative. |
| Related commit | feat: add Sale canBeReturned check for the 30 day window |

### Entry 22

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 3 - `feature/return-module` |
| Goal | Verify the refund, the receipt and the 30 day window on a real sale |
| Query | Do the three behaviours of the module work as the diagram describes? |
| Answer | Yes. Returning a video game of 250000 and a controller of 350000 refunded 600000, and returning only the video game refunded 250000, so partial returns work. The receipt showed the return id, its date, the sale, the date of the sale, the customer, each returned unit with its price, the reason and the amount. canBeReturned returned true on the same day, 10 days later and exactly 30 days later, and false 31 days later or with a future date. |
| Decision | Accepted. Prices use Locale.US with two decimals so the receipt prints the same as the warranty certificate and the accessory description. |
| Related commit | fix: reject return without identifier sale or products |
---

## Requirement 5 - Integration adjustment A1

### Entry 23

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 5, phase 2 - `feature/accessory-category-discount` (adjustment A1) |
| Goal | Find out what was really blocking a promotion over accessories |
| Query | Requirement 2 limited the target category to VIDEOGAME and CONSOLE. Which parts of the system still refuse accessories? |
| Answer | Three parts. PromotionService validates the category and rejected anything else, ConsoleMenu only offered two options, and promotions.csv had no accessory promotion. The model needed no change at all: CategoryDiscount already compares the target category with product.getCategory(), and every accessory answers ACCESSORY through that method. |
| Decision | Accepted changing only the service, the menu and the data file, and leaving the model untouched because it already worked. Changing CategoryDiscount would have been dead code. |
| Related commit | docs: document ACCESSORY as a category discount target |

### Entry 24

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 5, phase 2 - `feature/accessory-category-discount` (adjustment A1) |
| Goal | Allow the three categories in the place that validates them |
| Query | How should PromotionService accept ACCESSORY without changing how the other two categories are checked? |
| Answer | The class already keeps one private constant per accepted category, so a third constant CATEGORY_ACCESSORY and a third condition in validateTargetCategory follow the same pattern. |
| Decision | Accepted adding the constant, the condition, the third value in the error message and the JavaDoc. Rejected duplicating the validation, because the service is the only place that checks the category and the model stays independent. |
| Related commit | feat: accept ACCESSORY as target category for promotions |

### Entry 25

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 5, phase 2 - `feature/accessory-category-discount` (adjustment A1) |
| Goal | Verify the whole adjustment end to end |
| Query | Does a category promotion over accessories work from the data file to the discount amount? |
| Answer | Yes. The file loaded four promotions, including P-04 Accesorios al 15%. Registering a category promotion with ACCESSORY was accepted, an invalid category was still rejected with "La categoría debe ser VIDEOGAME, CONSOLE o ACCESSORY", and on a sale with one video game, one controller and one cable the discount was 64500, which is exactly fifteen percent of the two accessories and leaves the video game out of the base. |
| Decision | Accepted. The new preloaded promotion follows the same format and the same validity period as the other three. |
| Related commit | feat: add preloaded accessory category promotion |
---

## Requirement 5 - Integration adjustment A5

---

### Entry 26

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 5, phase 4 - `fix/return-discounted-refund` (A5) |
| Goal | Find why a store that granted a discount would refund more than the customer paid |
| Query | Requirement 3 refunds the sum of the list prices. What happens when the original sale had a promotion? |
| Answer | The customer paid the discounted price, but the return gave back the full list price, so the store lost the difference on every single return of a discounted sale. |
| Decision | Accepted refunding each unit at its list price minus the same proportion of the discount, using `sale.getDiscountAmount()` divided by `sale.getSubtotal()`, which is the only data the model already has. |
| Related commit | fix: refund discounted sale proportionally to the applied discount |

### Entry 27

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 5, phase 4 - `fix/return-discounted-refund` (A5) |
| Goal | Decide how the discount ratio is computed and when there is no discount |
| Query | Should the ratio be stored, passed as a parameter or read from the sale? |
| Answer | It must be read from the sale, because `Return` already keeps the reference and the constructor computes the refund before any service runs. |
| Decision | Accepted a private `discountRatio` method that returns zero when the sale is null, when the subtotal is not positive or when there is no discount, and otherwise returns the discount divided by the subtotal, capped at one so a refunded amount can never go negative. |
| Related commit | fix: refund discounted sale proportionally to the applied discount |

### Entry 28

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 5, phase 4 - `fix/return-discounted-refund` (A5) |
| Goal | Check the result against the example the leader gave |
| Query | Does a console of 2500000 in a sale with 125000 of discount refund 2375000 and show the three figures per unit? |
| Answer | Yes. The receipt printed `- C-01 PlayStation 5 Slim &#124; Precio: $2,500,000.00 &#124; Descuento: -$125,000.00 &#124; Reembolso: $2,375,000.00`, a sale without discount refunded the full 2500000, and a partial return of a video game in a sale with a 5 percent discount refunded 237500. |
| Decision | Accepted showing the discount with a minus sign, because the receipt must say how much is taken away from the list price. Prices keep `Locale.US` with two decimals, the same as the other modules. |
| Related commit | fix: show list price proportional discount and refund per item |

---

## Requirement 6 - Exceptions and validation

### Entry 29

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 6 - `feature/exception-handling` |
| Goal | Decide whether the root exception should be checked or unchecked |
| Query | Should `GameZoneException` extend `Exception` or `RuntimeException`? |
| Answer | Extending `RuntimeException` keeps the failure in the operation that caused it, so the services and the repositories do not have to declare it in every signature, and the menu can catch it in one place. |
| Decision | Accepted `RuntimeException`, with the code `GAMEZONE_ERROR` on the first constructor and one code per subclass. The class is abstract so nobody throws the root directly. |
| Related commit | feat: add GameZoneException base class with error code |

### Entry 30

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 6 - `feature/exception-handling` |
| Goal | Decide how each exception builds its message |
| Query | Should the messages be built by the caller or by the constructor? |
| Answer | The diagram shows each constructor receiving the pieces of the message, so the caller never concatenates text. |
| Decision | Accepted building the message inside the exception. For `ResourceNotFoundException` the caller passes the kind of resource already with its article, such as "el producto", so the sentence is correct for every resource instead of needing a second form for masculine and feminine. |
| Related commit | feat: add ResourceNotFoundException with typed message |

### Entry 31

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 6 - `feature/exception-handling` |
| Goal | Decide why the validators are `final` with a private constructor |
| Query | Why can a validator not be instantiated? |
| Answer | A validator only offers static methods and holds no state, so an instance would be useless and could keep data between calls. |
| Decision | Accepted `final` classes with a private constructor, which is the usual way to forbid instantiation from outside, and rejected a public constructor because it would invite using the class by object. |
| Related commit | feat: add ProductValidator and PersonValidator |

### Entry 32

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 6 - `feature/exception-handling` |
| Goal | Check that every method throws the exception the table of the diagram asks for |
| Query | Do the seven validators really throw the type and the error code that the table lists for each one? |
| Answer | Yes. I ran the forty-three cases of the table: a wrong format or range throws `InvalidDataException` with code `INVALID_DATA`, a lookup that returned null throws `ResourceNotFoundException` with code `RESOURCE_NOT_FOUND`, and a broken rule of the store throws `BusinessRuleException` with code `BUSINESS_RULE`. `PersistenceException` keeps the original cause and reports `PERSISTENCE_ERROR`, and the four exceptions are all `GameZoneException`. |
| Decision | Accepted the three exception types as they were and only adjusted the wording of the messages so the user reads a complete sentence, such as "El campo precio debe ser mayor que cero." |
| Related commit | feat: add ReturnValidator with window and units checks |

### Entry 33

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 6 - `feature/exception-handling` |
| Goal | Decide how a validator checks that a console really is a console |
| Query | Should `validateCompatibleConsole` use `instanceof` on `Console`? |
| Answer | The rule is about the category, and every product reports its category through `getCategory()`, which avoids importing the concrete classes. |
| Decision | Accepted comparing the category with `CONSOLE`, the same way `CategoryDiscount` does, and kept the two failures apart: null gives `ResourceNotFoundException` and a product that exists but is not a console gives `BusinessRuleException`. |
| Related commit | feat: add AccessoryValidator and SaleValidator |

### Entry 34

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 6 - `feature/exception-handling` |
| Goal | Fix a difference between the validator and the service about rejecting a zero quantity |
| Query | `validateQuantity` only rejected negative quantities, but `restoreStock` already rejects zero. Which of the two rules is the right one? |
| Answer | The rule of the service is the right one: a quantity of zero units is not an operation, it is a no-op the user should not be able to run, and the message already says "debe ser mayor que cero". |
| Decision | Accepted changing the condition to `quantity <= 0` and unifying the message with the one from the service, so the user reads the same thing no matter which path the call came in through. |
| Related commit | fix: reject zero quantity in validateQuantity |

### Entry 35

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 6 - `feature/exception-handling` |
| Goal | Remove the compiler warning about the exceptions |
| Query | Why does javac warn that serializable classes do not declare `serialVersionUID`? |
| Answer | `RuntimeException` implements `Serializable`, so the five classes inherit that obligation and the compiler warns with `-Xlint:serial`. Without that field, a change in the structure of the class breaks the deserialisation of old exceptions. |
| Decision | Accepted declaring `private static final long serialVersionUID = 1L` in the five, with a comment that explains it keeps the same version when recompiled. Verified that the warning disappears. |
| Related commit | fix: declare serialVersionUID in the exception hierarchy |

---

## Requirement 7 Phase 2 - GRASP refactor

In this phase the Technical Leader authorised the use of AI to review code, not only to write
it. The entries below record how the tool was used in each correction.

### Entry 36

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 7 phase 2 - `feature/grasp-refactor` |
| Goal | Record that AI use is authorised to review code in this phase, and settle from the start that the review is done on the real state of the branch and not on what I remembered from the previous branch |
| Query | The branch had changed completely from the one I had in mind: there is now a correction log, the consolidation renumbered the findings and the repositories moved. What do I do before writing the first line of code? |
| Answer | Verify the real state instead of assuming it: `git fetch` and `git checkout` of the branch, read the pom, search for the type markers the repositories write today and read the solution agreed in the consolidated audit. |
| Decision | Accepted working always on the code read at that moment, not on the one remembered from another branch. Also, `mvn` was not installed on my machine, so I installed Maven 3.9.9 in a portable way under `C:\Temp\opencode\tools`, pointing at the JDK 25 that was already installed, to be able to run the `mvn clean compile` the leader asked for without installing anything on the system. |
| Related commit | refactor: add increaseStock to Product (V-01) |

### Entry 37

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 7 phase 2 - `feature/grasp-refactor` |
| Goal | Write `Product.increaseStock` for the correction of V-01 |
| Query | `increaseStock` is the opposite of `decreaseStock`, but `decreaseStock` only rejects the negative and `restoreStock` rejects zero. Which rule and which exception do I use? |
| Answer | The rule is the one of `restoreStock`, which is the most restrictive and the one the user already knows: `quantity <= 0`. The exception cannot be the same one, because `Product` cannot call `ProductValidator` without creating a cycle between `model` and `validation`, because `validation` already depends on `model`. The class already throws `IllegalArgumentException` in `hasStock` and in `decreaseStock`. |
| Decision | Accepted using `quantity <= 0` with `IllegalArgumentException` and the message "La cantidad debe ser mayor que cero". Verified that `increaseStock(2)` on a stock of 3 leaves 5, that 0 and -5 are rejected without changing the stock, and that `decreaseStock` still works. |
| Related commit | refactor: add increaseStock to Product (V-01) |

### Entry 38

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 7 phase 2 - `feature/grasp-refactor` |
| Goal | Write `isConsole` in `Product` and `Console` for the correction of V-04 |
| Query | The "only consoles" rule is decided today by comparing `getCategory()` with the text "CONSOLE" in several places. With a method that answers that, is the result the same? |
| Answer | Yes, as long as the implementation does not change what the subclasses already answer. `Product` answers false and `Console` overrides it with true, which is exactly what `compareIgnoreCase` decided. The real risk was not the method, it was touching `getCategory()` by accident, because there are other legitimate uses of the category, for example the promotion P-02 that targets VIDEOGAME. |
| Decision | Accepted adding the method without touching `getCategory()`, and checking with the two products that the new method and the text comparison give the same boolean before pushing. |
| Related commit | refactor: let a product answer whether it is a console (V-04) |

### Entry 39

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 7 phase 2 - `feature/grasp-refactor` |
| Goal | Publish the type marker of promotions and warranties for V-08 |
| Query | The leader asked to use exactly the texts the repositories write today. Where are they and is there a risk that they do not match? |
| Answer | The repositories keep them in private constants: `TYPE_PERCENTAGE`, `TYPE_CATEGORY` and `TYPE_BULK` in `PromotionRepository`, and `TYPE_BASIC` and `TYPE_EXTENDED` in `WarrantyRepository`. The risk was not the text but the confusion: `Warranty` already has `getWarrantyType()`, which returns "Garantía Básica" and "Garantía Extendida", the name the user reads, which is different from the marker of the file. Confusing the two would have changed the warranty certificate the user sees. |
| Decision | Accepted adding `getTypeCode()` as a new abstract method separate from `getWarrantyType()`, documenting in the JavaDoc that it is not the name the user reads. Verified the five markers against the current logic of the repositories and the five match. |
| Related commit | refactor: publish the type code of promotions and warranties (V-08) |

### Entry 40

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 7 phase 2 - `feature/grasp-refactor` |
| Goal | Remove the `instanceof` from `saveAll` of `products.csv` without the file changing |
| Query | The leader asked that the file stay byte for byte the same, but when I tested the round trip I saw that 250000 was saved as 250000.0. Is my change to blame? |
| Answer | No. `append(double)` always wrote the decimal, so the difference with the seeded file predates my change. That is why comparing the round trip against the original file was not a valid proof: it would have reported as my failure a difference that already existed. |
| Decision | Accepted generating a reference with the code from before the change, applying the change, and comparing the new round trip against that reference. Both gave 311 bytes and the same SHA-256, so the proof measures what really depends on my change. |
| Related commit | refactor: write the catalogue row from what each product publishes (V-08) |

### Entry 41

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 7 phase 2 - `feature/grasp-refactor` |
| Goal | Decide where the extra columns live and how to implement it in the four hierarchies |
| Query | The new method is abstract in `Product`, but `Accessory` also extends `Product`. Do I implement it in `Controller`, `Cable` and `Memory`? |
| Answer | If I implement it there I have to guess how many columns `accessories.csv` uses, and that file belongs to another member. Guessing another member's format is exactly the kind of decision the pattern protects. Also, the file has two extra columns and `Controller` only has one field, so my list would not match and the save would be wrong. |
| Decision | Accepted implementing it in `VideoGame` and `Console`, which do live in `products.csv`, and having `Accessory` answer an empty list with the documentation that an accessory is not stored in the product catalogue. That way the accessory repository, which belongs to another member, keeps using its own logic unchanged. |
| Related commit | refactor: write the catalogue row from what each product publishes (V-08) |

### Entry 42

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 7 phase 2 - `feature/grasp-refactor` |
| Goal | Turn the private `coverageDescription` into an abstract method without changing the certificate |
| Query | The old method compared the name of the type and returned two fixed texts. How do I check that the certificate did not change without having the previous version at hand? |
| Answer | The comparison can be done without the previous version, because the old method only had two possible answers depending on the name: "Garantía Extendida" gave the long text and anything else gave the short one. Both combinations the store really has, basic and extended, were checked and both certificates came out with the same header, dates, costs, state and coverage line. |
| Decision | Accepted making it abstract and answering in each subclass with the same text it already produced. The certificate keeps calling the same method, so no caller changed. |
| Related commit | refactor: let each warranty answer its own coverage (V-09) |

### Entry 43

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 7 phase 2 - `feature/grasp-refactor` |
| Goal | Publish in the model the columns of accessories and promotions without changing the files |
| Query | The repository converts null with a private `valueOf` helper, and in `List.of` a null throws. How do I publish the same behaviour? |
| Answer | The helper does `valor == null ? "" : valor`. `List.of` is not enough: a null cannot even build the list, because the repository also uses `List.of` for the compatible consoles and has never had that problem, so a null there is already ruled out by the design itself. Even so, to avoid depending on that assumption, the same ternary as the helper was used instead of trusting `List.of`. |
| Decision | Accepted converting the null with the same ternary as the repository and checking the three accessories with a value, with null and with zero, and the three promotions with an integer percentage, a decimal one, a null category and a minimum of zero. Also, the empty list of `Accessory` was removed, because its three subclasses already answer and `Accessory` is abstract. |
| Related commits | refactor: let each accessory publish its own columns (V-08), refactor: let each promotion publish its own columns (V-08) |

### Entry 44

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 7 phase 2 - `feature/grasp-refactor` |
| Goal | Write the two formatters of the `ui` package and decide whether `getDescription` also goes |
| Query | The receipt prints the discount of each returned unit with `discountRatio()`, which is private in `Return`. Do I recalculate it in the UI or change the visibility? And if I remove `getDescription`, is the UI forced to use `instanceof`? |
| Answer | Recalculating it in the UI would duplicate a rule of the domain, which is exactly what is being corrected. The discounted proportion belongs to the return, because it is the one that knows which units were returned, so `Return` is the expert. And about `getDescription`: `ConsoleMenu` prints it in twelve places without a single `instanceof`; removing it would force the menu to ask for the type of every item to decide what text to write, which is the same `instanceof` chain that V-08 is removing. |
| Decision | Accepted making `discountRatio()` public so the formatter can ask for it, and keeping `getDescription` with the justification written in the log, which is what the statement asked for. The two formatters were compared against the model methods in five scenarios, including an expired warranty and two cases with nulls, and all five strings came out identical. |
| Related commit | refactor: build the return receipt and the certificate in the ui package (V-05) |

### Entry 45

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 7 phase 2 - `feature/grasp-refactor` |
| Goal | Delete the two model methods and finish V-05 |
| Query | When deleting the methods, what else is left unused in the classes? |
| Answer | `Locale` was used only inside the two deleted methods: `Return` used it in five calls to `String.format` and `Warranty` in one. Without those methods the import is unused, so leaving it would leave rubbish the reviewer would have to notice. Also, the JavaDoc of the formatters said the method "has not been removed yet", and if it is not updated the documentation ends up lying about the real state of the code. |
| Decision | Accepted deleting the two methods, removing the `import java.util.Locale` from the two classes and rewriting the JavaDoc of the formatters to describe the current situation. A search confirmed that no call to the two methods is left and that the two formatters still return the same strings: 464, 253, 283 and 256 characters, the same counts as the previous comparison. |
| Related commit | refactor: remove the document builders from the model (V-05) |

### Entry 46

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 7 phase 2 - `feature/grasp-refactor` |
| Goal | Decide whether `discountRatio` stays public |
| Query | The leader said that if only the formatter uses it, it should stay public. Does anyone else use it? |
| Answer | Not only the formatter: `refundableAmount`, which is private in `Return` and computes the refund of each returned unit, calls it too. That method stays because it is the refund rule, which is domain. So the method has one internal and one external use, and keeping it public is the right thing. |
| Decision | Accepted keeping it public and noting in the log that it is not only used by the formatter but also by `refundableAmount`, so that the justification is not built on a wrong premise. |
| Related commit | refactor: remove the document builders from the model (V-05) |

---

## Requirement 8 — Phase 1 (Audit)

The Technical Leader authorised the use of AI to review code for this audit as well. The entries
below record how the tool was used to write it and how the corrections of the review were applied.

### Entry 47

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 8 phase 1 - `feature/solid-audit` |
| Goal | Audit my code against the five SOLID principles with real evidence |
| Query | How do I separate a real defect from an opinion about the style? Do I write the report from memory or do I read the code? |
| Answer | Neither works. In SOLID the evidence is the exact line, so an approximate line number makes the finding impossible to verify and the report loses its value. And a style defect is not a SOLID defect: each principle asks one concrete question, and if the answer cannot be "yes" or "no" backed by a line, it is not a finding. |
| Decision | Accepted reading the code of the branch at the audited commit (`675e61c`) before writing a single line, taking the public signatures with their line numbers, and applying each principle its own question: SRP which class changes for more than one reason, OCP which class has to be edited to add a type, LSP which subclass satisfies the signature but not the meaning, ISP which subclass inherits a contract it cannot honour, DIP which high layer knows a detail of the low layer. A finding whose question was left unanswered was not registered. |
| Related commit | docs: add scope and summary to developer1 solid audit, and the five commits of each principle |

### Entry 48

| Field | Content |
|---|---|
| Date | 2026-10-19 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 8 phase 1 - `feature/solid-audit` |
| Goal | Apply the five corrections of the leader's review |
| Query | The leader pointed out three claims of mine that were not true: that `ProductRepository` belongs to another member, that `Sale.generateReceipt` exists, and that the menu catches the base class of the exceptions. Do I defend the report or correct it? |
| Answer | Correct it. All three were checked against the code before changing a word and all three were false: `ProductRepository` appears as Developer 1 in the coverage of the consolidated GRASP audit, `generateReceipt` no longer exists in `Sale`, and `Submenu.runSafely` catches the four subclasses one after another and no file in `ui` imports `GameZoneException`. Besides, verifying instead of defending surfaced two real problems: `ReceiptGenerator` writes money with `String.format` without `Locale.US`, unlike the other nine places, and a fifth kind of exception forces an edit of `Submenu`. |
| Decision | Accepted correcting each point in its own `docs:` commit, without rewriting the whole report, and reporting the two new problems for the consolidation instead of auditing them as my own code, because `Submenu` and `ReceiptGenerator` belong to the leader. |
| Related commits | docs: correct the ownership of ProductRepository in developer1 solid audit, docs: point V-03 at ReceiptGenerator instead of the removed sale receipt, docs: correct how the menu catches the exceptions in developer1 solid audit, docs: register V-01 only under interface segregation in developer1 solid audit, docs: remove the R9 reference from the developer1 solid audit scope |

### Entry 49

| Field | Content |
|---|---|
| Date | 2026-10-19 |
| Tool | Space Bunny (OpenCode) |
| Phase and branch | Requirement 8 phase 1 - `feature/solid-audit` |
| Goal | Put this log back in the language and the layout its own template asks for |
| Query | Entries 1 to 25 follow the template of the top of this file, in English and as a vertical table. From entry 26 on, the entries were written in a horizontal table, and from entry 34 on the content was in Spanish as well. Was that a decision or a slip? |
| Answer | A slip, and a long one. Nothing in the project asked for it: the template at the top of this file is in English with the fields Date, Tool, Phase and branch, Goal, Query, Answer, Decision and Related commit, and the entries of the first requirements follow it. The change happened when the GRASP correction started and it was carried forward without noticing, through twelve entries. Entries 26 to 33 had English content with the horizontal layout, and entries 34 to 48 had Spanish content as well. |
| Decision | Accepted rewriting entries 26 to 48 in the format of the template, translating the content of 34 to 48 into English, restoring the separators between sections and the blank line before each entry. The content of the entries was not changed in substance: no query, answer or decision was rewritten, only the language and the layout. The entries are numbered continuously and no entry was merged or removed, so the numbering of the commits referenced by each entry still matches. |
| Related commit | docs: bring the AI usage log back to the template format |
