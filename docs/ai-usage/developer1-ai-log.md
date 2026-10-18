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