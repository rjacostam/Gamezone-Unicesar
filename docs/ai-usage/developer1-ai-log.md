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

### Entry 26

| Fecha | Herramienta | Fase y rama | Objetivo | Consulta | Respuesta | Decision | Commit relacionado |
|---|---|---|---|---|---|---|---|
| 2026-10-18 | Space Bunny (OpenCode) | Requirement 5, phase 4 - `fix/return-discounted-refund` (A5) | Find why a store that granted a discount would refund more than the customer paid | Requirement 3 refunds the sum of the list prices. What happens when the original sale had a promotion? | The customer paid the discounted price, but the return gave back the full list price, so the store lost the difference on every single return of a discounted sale. | Accepted refunding each unit at its list price minus the same proportion of the discount, using sale.getDiscountAmount() divided by sale.getSubtotal(), which is the only data the model already has. | fix: refund discounted sale proportionally to the applied discount |

### Entry 27

| Fecha | Herramienta | Fase y rama | Objetivo | Consulta | Respuesta | Decision | Commit relacionado |
|---|---|---|---|---|---|---|---|
| 2026-10-18 | Space Bunny (OpenCode) | Requirement 5, phase 4 - `fix/return-discounted-refund` (A5) | Decide how the discount ratio is computed and when there is no discount | Should the ratio be stored, passed as a parameter or read from the sale? | It must be read from the sale, because Return already keeps the reference and the constructor computes the refund before any service runs. | Accepted a private discountRatio method that returns zero when the sale is null, when the subtotal is not positive or when there is no discount, and otherwise returns the discount divided by the subtotal, capped at one so a refunded amount can never go negative. | fix: refund discounted sale proportionally to the applied discount |

### Entry 28

| Fecha | Herramienta | Fase y rama | Objetivo | Consulta | Respuesta | Decision | Commit relacionado |
|---|---|---|---|---|---|---|---|
| 2026-10-18 | Space Bunny (OpenCode) | Requirement 5, phase 4 - `fix/return-discounted-refund` (A5) | Check the result against the example the leader gave | Does a console of 2500000 in a sale with 125000 of discount refund 2375000 and show the three figures per unit? | Yes. The receipt printed "- C-01 PlayStation 5 Slim &#124; Precio: $2,500,000.00 &#124; Descuento: -$125,000.00 &#124; Reembolso: $2,375,000.00", a sale without discount refunded the full 2500000, and a partial return of a video game in a sale with a 5 percent discount refunded 237500. | Accepted showing the discount with a minus sign, because the receipt must say how much is taken away from the list price. Prices keep Locale.US with two decimals, the same as the other modules. | fix: show list price proportional discount and refund per item |
---

## Requirement 6 - Exceptions and validation

### Entry 29

| Fecha | Herramienta | Fase y rama | Objetivo | Consulta | Respuesta | Decision | Commit relacionado |
|---|---|---|---|---|---|---|---|
| 2026-10-18 | Space Bunny (OpenCode) | Requirement 6 - `feature/exception-handling` | Decide whether the root exception should be checked or unchecked | Should GameZoneException extend Exception or RuntimeException? | Extending RuntimeException keeps the failure in the operation that caused it, so the services and the repositories do not have to declare it in every signature, and the menu can catch it in one place. | Accepted RuntimeException, with the code GAMEZONE_ERROR on the first constructor and one code per subclass. The class is abstract so nobody throws the root directly. | feat: add GameZoneException base class with error code |

### Entry 30

| Fecha | Herramienta | Fase y rama | Objetivo | Consulta | Respuesta | Decision | Commit relacionado |
|---|---|---|---|---|---|---|---|
| 2026-10-18 | Space Bunny (OpenCode) | Requirement 6 - `feature/exception-handling` | Decide how each exception builds its message | Should the messages be built by the caller or by the constructor? | The diagram shows each constructor receiving the pieces of the message, so the caller never concatenates text. | Accepted building the message inside the exception. For ResourceNotFoundException the caller passes the kind of resource already with its article, such as "el producto", so the sentence is correct for every resource instead of needing a second form for masculine and feminine. | feat: add ResourceNotFoundException with typed message |

### Entry 31

| Fecha | Herramienta | Fase y rama | Objetivo | Consulta | Respuesta | Decision | Commit relacionado |
|---|---|---|---|---|---|---|---|
| 2026-10-18 | Space Bunny (OpenCode) | Requirement 6 - `feature/exception-handling` | Decide why the validators are final with a private constructor | Why can a validator not be instantiated? | A validator only offers static methods and holds no state, so an instance would be useless and could keep data between calls. | Accepted final classes with a private constructor, which is the usual way to forbid instantiation from outside, and rejected a public constructor because it would invite using the class by object. | feat: add ProductValidator and PersonValidator |

### Entry 32

| Fecha | Herramienta | Fase y rama | Objetivo | Consulta | Respuesta | Decision | Commit relacionado |
|---|---|---|---|---|---|---|---|
| 2026-10-18 | Space Bunny (OpenCode) | Requirement 6 - `feature/exception-handling` | Check that every method throws the exception the table of the diagram asks for | Do the seven validators really throw the type and the error code that the table lists for each one? | Yes. I ran the forty-three cases of the table: a wrong format or range throws InvalidDataException with code INVALID_DATA, a lookup that returned null throws ResourceNotFoundException with code RESOURCE_NOT_FOUND, and a broken rule of the store throws BusinessRuleException with code BUSINESS_RULE. PersistenceException keeps the original cause and reports PERSISTENCE_ERROR, and the four exceptions are all GameZoneException. | Accepted the three exception types as they were and only adjusted the wording of the messages so the user reads a complete sentence, such as "El campo precio debe ser mayor que cero." | feat: add ReturnValidator with window and units checks |

### Entry 33

| Fecha | Herramienta | Fase y rama | Objetivo | Consulta | Respuesta | Decision | Commit relacionado |
|---|---|---|---|---|---|---|---|
| 2026-10-18 | Space Bunny (OpenCode) | Requirement 6 - `feature/exception-handling` | Decide how a validator checks that a console really is a console | Should validateCompatibleConsole use instanceof on Console? | The rule is about the category, and every product reports its category through getCategory(), which avoids importing the concrete classes. | Accepted comparing the category with CONSOLE, the same way CategoryDiscount does, and kept the two failures apart: null gives ResourceNotFoundException and a product that exists but is not a console gives BusinessRuleException. | feat: add AccessoryValidator and SaleValidator |
### Entry 34

| Fecha | Herramienta | Fase y rama | Objetivo | Consulta | Respuesta | Decision | Commit relacionado |
|---|---|---|---|---|---|---|---|
| 2026-10-18 | Space Bunny (OpenCode) | Requirement 6 - `feature/exception-handling` | Corregir una diferencia entre el validador y el servicio al rejecting la cantidad cero | validateQuantity solo rechazaba las cantidades negativas, pero restoreStock ya rechaza el cero. ¿Cual de las dos reglas es la correcta? | La regla del servicio es la correcta: una cantidad de cero unidades no es una operación, es un no-op que el usuario no debería poder ejecutar, y ademas el mensaje ya dice "debe ser mayor que cero". | Accepted cambiar la condición a quantity <= 0 y unificar el mensaje con el del servicio, de modo que el usuario lea lo mismo sin importar por donde entra la llamada. | fix: reject zero quantity in validateQuantity |

### Entry 35

| Fecha | Herramienta | Fase y rama | Objetivo | Consulta | Respuesta | Decision | Commit relacionado |
|---|---|---|---|---|---|---|---|
| 2026-10-18 | Space Bunny (OpenCode) | Requirement 6 - `feature/exception-handling` | Quitar el aviso del compilador sobre las excepciones | ¿Por qué javac avisa que las clases serializables no declaran serialVersionUID? | RuntimeException implementa Serializable, así que las cinco clases heredan esa obligación y el compilador avisa con -Xlint:serial. Sin ese campo, un cambio en la estructura de la clase rompe la deserialización de excepciones antiguas. | Accepted declarar private static final long serialVersionUID = 1L en las cinco, con un comentario que explica que mantiene la misma versión al recompilar. Verifiqué que el aviso desaparece. | fix: declare serialVersionUID in the exception hierarchy |
## Requirement 7 Phase 2 - GRASP refactor

In this phase the Technical Leader authorised the use of AI to review code, not only to write
it. The entries below record how the tool was used in each correction.

### Entry 36

| Fecha | Herramienta | Fase y rama | Objetivo | Consulta | Respuesta | Decision | Commit relacionado |
|---|---|---|---|---|---|---|---|
| 2026-10-18 | Space Bunny (OpenCode) | Requirement 7 Fase 2 - `feature/grasp-refactor` | Anotar que en esta fase la IA si esta autorizada para revisar codigo, y fijar desde el primer momento que la revision es sobre el estado real de la rama y no sobre lo que yo recordaba de la rama anterior | La rama cambio por completo respecto a la que tenia en la cabeza: ahora existe el log de correcciones, la consolidacion renumero los hallazgos y el repositorio se movio. Que hago antes de escribir la primera linea de codigo? | Verificar el estado real en vez de asumirlo: git fetch y git checkout de la rama indicada, lectura del pom, busqueda de los marcadores de tipo que hoy escriben los repositorios y lectura de la solucion acordada en la auditoria consolidada. | Accepted trabajar siempre sobre el codigo leido en ese momento, no sobre el que recuerda de otra rama. Ademas, mvn no estaba instalado en mi equipo,asi que instale Maven 3.9.9 de forma portable en C:\Temp\opencode\tools apuntando al JDK 25 ya instalado, para poder correr el mvn clean compile que pidio el lider sin instalar nada en el sistema. | refactor: add increaseStock to Product (V-01) |
### Entry 37

| Fecha | Herramienta | Fase y rama | Objetivo | Consulta | Respuesta | Decision | Commit relacionado |
|---|---|---|---|---|---|---|---|
| 2026-10-18 | Space Bunny (OpenCode) | Requirement 7 Fase 2 - `feature/grasp-refactor` | Escribir `Product.increaseStock` para la correccion de V-01 | increaseStock es el opuesto de decreaseStock, pero decreaseStock rechaza solo lo negativo y restoreStock rechaza el cero. Que regla y que excepcion uso? | La regla es la de restoreStock, que es la mas restrictiva y la que el usuario ya conoce: quantity <= 0. La excepcion no puede ser la misma, porque Product no puede llamar a ProductValidator sin crear un ciclo entre model y validation, ya que validation depende de model. La clase ya lanza IllegalArgumentException en hasStock y en decreaseStock. | Accepted usar `quantity <= 0` con IllegalArgumentException y el mensaje "La cantidad debe ser mayor que cero". Verifique que increaseStock(2) sobre stock 3 deja 5, que el 0 y el -5 se rechazan sin cambiar el stock y que decreaseStock sigue funcionando. | refactor: add increaseStock to Product (V-01) |

### Entry 38

| Fecha | Herramienta | Fase y rama | Objetivo | Consulta | Respuesta | Decision | Commit relacionado |
|---|---|---|---|---|---|---|---|
| 2026-10-18 | Space Bunny (OpenCode) | Requirement 7 Fase 2 - `feature/grasp-refactor` | Escribir `isConsole` en Product y Console para la correccion de V-04 | La regla "solo consolas" hoy se decide comparando `getCategory()` con el texto "CONSOLE" en varios lados. Con un metodo que responda eso, ¿el resultado es el mismo? | Si, siempre que la implementacion no cambie lo que las subclases ya contestan. Product responde false y Console sobrescribe con true, que es exactamente lo que.compareIgnoreCase decidia. El riesgo real no era el metodo, era tocar por accidente `getCategory()`, porque hay otros usos legitimos de la categoria, por ejemplo la promocion P-02 que apunta a VIDEOGAME. | Accepted agregar el metodo sin tocar `getCategory()`, y comprobar con los dos productos que el metodo nuevo y la comparacion por texto dan el mismo booleano antes de pushear. | refactor: let a product answer whether it is a console (V-04) |

### Entry 39

| Fecha | Herramienta | Fase y rama | Objetivo | Consulta | Respuesta | Decision | Commit relacionado |
|---|---|---|---|---|---|---|---|
| 2026-10-18 | Space Bunny (OpenCode) | Requirement 7 Fase 2 - `feature/grasp-refactor` | Publicar el marcador de tipo de promociones y garantias para V-08 | El lider pidio usar exactamente los textos que hoy escriben los repositorios. ¿Donde estan y hay riesgo de que no coincidan? | Los repositorios los tienen en constantes privadas: `TYPE_PERCENTAGE`, `TYPE_CATEGORY`, `TYPE_BULK` en `PromotionRepository` y `TYPE_BASIC`, `TYPE_EXTENDED` en `WarrantyRepository`. El riesgo no era de texto sino de confusion: `Warranty` ya tiene `getWarrantyType()`, que devuelve "Garantía Básica" y "Garantía Extendida", el nombre que lee el usuario, que es distinto del marcador del archivo. Confundir los dos habria cambiado el certificado de garantia que ve el usuario. | Accepted agregar `getTypeCode()` como metodo abstracto nuevo y separado de `getWarrantyType()`, documentando en el JavaDoc que no es el nombre que ve el usuario. Verifique los cinco marcadores contra la logica actual de los repositorios y los cinco coinciden. | refactor: publish the type code of promotions and warranties (V-08) |
### Entry 40

| Fecha | Herramienta | Fase y rama | Objetivo | Consulta | Respuesta | Decision | Commit relacionado |
|---|---|---|---|---|---|---|---|
| 2026-10-18 | Space Bunny (OpenCode) | Requirement 7 Fase 2 - `feature/grasp-refactor` | Quitar el instanceof de `saveAll` de products.csv sin que el archivo cambie | El lider pidio que el archivo quedara byte por byte igual, pero al probar el round-trip vi que `250000` se guardaba como `250000.0`. ¿Mi cambio es el culpable? | No. `append(double)` siempre escribio el decimal, asi que la diferencia con el archivo sembrado es anterior a mi cambio. Por eso comparar el round-trip contra el archivo original no servia como prueba: habria marcado como falla mia una diferencia que ya existia. | Accepted generar una referencia con el codigo de antes del cambio, aplicar el cambio, y comparar el nuevo round-trip contra esa referencia. Dieron 311 bytes y el mismo SHA-256 en los dos casos. Asi la prueba mide lo que si depende de mi cambio. | refactor: write the catalogue row from what each product publishes (V-08) |

### Entry 41

| Fecha | Herramienta | Fase y rama | Objetivo | Consulta | Respuesta | Decision | Commit relacionado |
|---|---|---|---|---|---|---|---|
| 2026-10-18 | Space Bunny (OpenCode) | Requirement 7 Fase 2 - `feature/grasp-refactor` | Decidir donde viven las columnas extra y como implementarlo en las cuatro jerarquias | El metodo nuevo es abstracto en `Product`, pero `Accessory` tambien hereda de `Product`. ¿Lo implemento en `Controller`, `Cable` y `Memory`? | Si los implemento, tengo que adivinar cuantas columnas usa `accessories.csv`, y ese archivo es de otro miembro. Adivinar el formato de otro es exactamente el tipo de decision que el patron protege. Ademas el archivo tiene dos columnas extra y `Controller` solo tiene un campo, o sea que mi lista no coincidiria y el guardado quedaria mal. | Accepted implementarlo en `VideoGame` y `Console`, que si viven en `products.csv`, y en `Accessory` responder una lista vacia documentando que un accesorio no se guarda en el catalogo de productos. Asi el repositorio de accesorios, que es de otro miembro, sigue usando su propia logica sin cambios. | refactor: write the catalogue row from what each product publishes (V-08) |

### Entry 42

| Fecha | Herramienta | Fase y rama | Objetivo | Consulta | Respuesta | Decision | Commit relacionado |
|---|---|---|---|---|---|---|---|
| 2026-10-18 | Space Bunny (OpenCode) | Requirement 7 Fase 2 - `feature/grasp-refactor` | Cambiar el `coverageDescription` privado por un metodo abstracto sin cambiar el certificado | El metodo viejo comparaba el nombre del tipo y devolvia dos textos fijos. ¿Como compruebo que el certificado no cambio sin tener la version anterior a mano? | La comparacion se puede hacer sin la version anterior, porque el metodo viejo solo tenia dos salidas posibles segun el nombre: "Garantía Extendida" daba el texto largo y cualquier otro daba el corto. Se comprobaron las dos combinations reales de la tienda, basica y extendida, y los dos certificados salieron con el mismo encabezado, fechas, costos, estado y linea de cobertura. | Accepted convertirlo en abstracto y responder en cada subclase con el mismo texto que ya producia. El certificado sigue llamando el mismo metodo, asi que no cambio ningun llamador. | refactor: let each warranty answer its own coverage (V-09) |
### Entry 43

| Fecha | Herramienta | Fase y rama | Objetivo | Consulta | Respuesta | Decision | Commit relacionados |
|---|---|---|---|---|---|---|---|
| 2026-10-18 | Space Bunny (OpenCode) | Requirement 7 Fase 2 - `feature/grasp-refactor` | Publicar en el modelo las columnas de accesorios y promociones sin cambiar los archivos | El repositorio convierte null con un helper privado `valueOf`, y en `List.of` un null lanza excepcion. ¿Como publico yo el mismo comportamiento? | El helper hace `valor == null ? "" : valor`. Con `List.of` no alcanza: un null no se puede ni construir la lista, porque el repositorio tambien usa `List.of` para las consolas compatibles y nunca ha luggage eso, o sea que un null ahi ya esta descartado por el propio diseno. Aun asi, para no depender de esa suposicion, se uso el mismo ternario del helper en vez de confiar en `List.of`. | Accepted convertir el null con el mismo ternario del repositorio y comprobar los tres accesorios con valor, con null y con cero, y las tres promociones con porcentaje entero, decimal, categoria null y minimo cero. Ademas se quito la lista vacia de `Accessory`, porque sus tres subclases ya responden y `Accessory` es abstracta. | refactor: let each accessory publish its own columns (V-08), refactor: let each promotion publish its own columns (V-08) |

### Entry 44

| Fecha | Herramienta | Fase y rama | Objetivo | Consulta | Respuesta | Decision | Commit relacionados |
|---|---|---|---|---|---|---|---|
| 2026-10-18 | Space Bunny (OpenCode) | Requirement 7 Fase 2 - `feature/grasp-refactor` | Escribir los dos formateadores del paquete ui y decidir si `getDescription` tambien se va | El comprobante imprime el descuento de cada unidad devuelta con `discountRatio()`, que es privado de `Return`. ¿Lo recalculo en la UI o cambio la visibilidad? Y si quito `getDescription`, ¿la UI se ve obligada a usar instanceof? | Recalcularlo en la UI duplicaria una regla del dominio, que es justo lo que se esta corrigiendo. La proporcion descontada la posee la devolucion, porque es la que sabe que unidades se devolvieron, asi que `Return` es el experto. Y sobre `getDescription`: `ConsoleMenu` lo imprime en doce lugares sin un solo instanceof; quitarlo obligaria al menu a preguntar el tipo de cada item para decidir que texto escribir, que es la misma cadena de instanceof que V-08 esta quitando. | Accepted hacer publico `discountRatio()` para que el formateador lo pida, y **dejar `getDescription`** con la justificacion escrita en el log, que es lo que pedia el enunciado. Los dos formateadores se compararon con los metodos del modelo en cinco escenarios, incluida una garantia vencida y dos casos con nulos, y las cinco cadenas salieron identicas. | refactor: build the return receipt and the certificate in the ui package (V-05) |