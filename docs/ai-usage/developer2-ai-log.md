# AI Usage Log â€” Developer 2

Owner: JosÃ© David Cervantes Solano

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
| Related commit | |

---

## Entry 001 â€” Implementation of the people module

| Field | Content |
|---|---|
| Date | 2026-10-17 |
| Tool | OpenCode with the free model `nemotron-3.5-super-vl-free` |
| Phase and branch | Taller 1, Part 5 (implementation of the people module). Branch `feature/person-module`, created from `develop` after the analysis documents were merged. |
| Goal | Implement the five classes of the people module assigned to Developer 2 in `TEAM.md`: `Person`, `Customer`, `Seller`, `PersonRepository` and `PersonService`, following the design already approved in `docs/class-diagram.md` and `docs/analysis.md`. |
| Query | "Read the repository and review the diagrams again to guide the implementation." / "Write `Person` with its common attributes, constructor and common methods." / "Write `Customer` with its own attributes." / "Write `Seller` with its own attributes." / "Write `PersonRepository` to save and load people from files." / "Write `PersonService` with register and list methods." / "Do not compile, just make sure the code is valid." |
| Answer | The model read the merged design documents and implemented the five classes exactly as they were specified there. Because the machine has no Java 17 and no Maven, the code could not be compiled, so the model verified it statically: balance of braces and parentheses on every file, detection of unused imports, verification that each declared package matches its directory, verification of all 39 method signatures against the class diagram one by one, and a simulation of the CSV parsing against the preload file. All checks passed. |
| Decision | **Accepted:** every attribute and signature of the merged class diagram, including `getRole()` returning `"Cliente"` / `"Vendedor"` and `getSummary()` building the listing line, both declared abstract in `Person`; the `PersonRepository` constructor receiving the two file paths; the data kept in two separate files, `customers.csv` and `sellers.csv`; `registerCustomer` receiving plain values and returning the `Customer`; `findSellerById` accepting the identification or the employee code; the semicolon delimiter, chosen because names may contain commas; and validation with `IllegalArgumentException` messages written in Spanish, because the menu shows them directly to the user. **Modified:** a first draft of the duplicate check called `findCustomerById` twice and returned `Object`; it was rewritten to iterate once and return `Person`, because calling a search twice is duplicated logic and returning `Object` loses the type of the result. **Discarded:** an earlier personal draft that used `identificationNumber`, a single `people.csv` file, validation inside the domain classes and `equals`/`hashCode`; all of it was discarded because the design merged by the team is the obligatory reference for the implementation and none of those elements appear in it. |
| Related commit | `c967af9`, `74722bb`, `675b7e9`, `7a997b7`, `80fcdfa`, `e8beabc` |

---

## Entry 002 â€” Improvements to the people module

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | OpenCode with the free model `nemotron-3.5-super-vl-free` |
| Phase and branch | Taller 1, Part 5 (review of the people module). Branch `feature/person-module-improvements`, created from the updated `develop` after the module had been merged. |
| Goal | Apply the seven improvements requested by the Technical Leader on the merged people module, without changing the public signatures, the attributes, the file formats or the `throws IOException` clauses. |
| Query | "Validate that the customer phone has only digits." / "Store the e-mail in lower case." / "Make `getSummary()` show the role, for example `[Cliente]123 \| Ana....`" / "Make `PersonRepository` ignore damaged lines instead of failing on load." / "Make `findSellerById` ignore capitalisation and spaces." / "Complete the JavaDoc of the module." / "Update the AI usage log." |
| Answer | The model applied the seven changes as one commit each, pushed after every commit, and verified the result statically because the machine has no Java 17 and no Maven: brace and parenthesis balance on every file of the whole project, detection of unused imports, and confirmation that no public signature, attribute or `throws` clause changed. The model also reviewed every call site of the module in `Main`, `SaleService` and `ConsoleMenu` to confirm that the behaviour changes are compatible with the code of the other members. |
| Decision | **Accepted:** the phone validation was placed in `PersonService`, because the analysis document states that the validation lives in the services and not in the domain classes; the digits were compared against the ASCII range `'0'` to `'9'` instead of using `Character.isDigit(char)`, because that method also accepts digits of other writing systems; the e-mail normalisation was placed in `Customer.setEmail(String)` and the constructor was changed to call that setter, so the address is normalised no matter how the customer is built; `Locale.ROOT` was used for the lower casing, because the default locale of the machine would change the result for some letters; the role prefix of `getSummary()` was written in each subclass instead of in a shared helper, to keep `Person` exactly as it appears in `docs/class-diagram.md`; and the damaged record check was implemented as a private helper that rejects records with an empty field, since the field count was already being verified. **Modified:** nothing outside the five assigned files was touched, and `docs/` was not modified apart from this log, which is not a diagram document. `data/sellers.csv` was left untouched and no test data was committed. **Discarded:** the idea of making `findCustomerById(String)` case-insensitive as well. It was not requested, and it is reported to the Technical Leader as an open question, because `ProductService.findById(String)` already ignores capitalisation and the two searches would otherwise behave differently. |
| Related commit | `3285d3c` (phone validation), `213fd77` (e-mail in lower case), `b7278da` (role in the summary line), `533fe36` (skip damaged records), `6f24e6f` (seller search ignoring capitalisation), `b95ceb0` (JavaDoc), plus the commit that carries this log |

---

## Entry 003 â€” Accessory module repository and service

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | OpenCode with the free model `nemotron-3.5-super-vl-free` |
| Phase and branch | Requerimiento 1 del Parcial Primer Corte (mÃ³dulo de accesorios). Branch `feature/accessory-module`, taken from the updated `develop` with the model classes of Developer 1 already merged. |
| Goal | Implement the two classes of the accessory module assigned to Developer 2 in `docs/accessory-class-diagram.md`: `AccessoryRepository` and `AccessoryService`, together with `data/accessories.csv`, following the specification of the Technical Leader exactly. |
| Query | "Create `data/accessories.csv` with the header and the three preloaded accessories." / "Implement `AccessoryRepository` with `saveAll` and `loadAll` that declare `throws IOException`, rebuild the right subclass from the first column, return an empty list when the file is missing, skip the header and ignore damaged lines." / "Implement `AccessoryService` with the constructor loading the accessories and turning an `IOException` into an `IllegalStateException` like `ProductService` does." / "Add the three register methods, the four queries and `updateStock`, saving after every change." / "Reject invalid data with `IllegalArgumentException` and a Spanish message." |
| Answer | The model read the accessory class diagram and the model classes of Developer 1 to use their exact API, read `ProductService` to copy the `IllegalStateException` pattern, and implemented the two classes and the data file with one commit per logical unit, six commits in total, each pushed right after it was created. Because the machine has no Java 17 and no Maven, the code could not be compiled, so it was verified statically: brace and parenthesis balance on every file, detection of unused imports, and confirmation of the twelve signatures and three attributes against the class diagram. The preload file was also checked to have eight columns per line and to be saved in UTF-8. |
| Decision | **Accepted:** the file errors of `AccessoryService` become `IllegalStateException` in the constructor and in a private save method, exactly like `ProductService`, instead of propagating the checked exception. `updateStock` removes units by delegating to `Product.decreaseStock(int)`, because that method already rejects a negative quantity and an insufficient stock, and duplicating those checks would have repeated logic that already lives in the domain. The type check of `listAccessoriesByType` compares against the three constants instead of accepting any string, so a typo is reported instead of silently returning an empty list. Records of the accessories file are accepted with seven or eight columns, so a line that lost its trailing semicolon for a controller is still read. **Modified:** the compatible console identifiers are trimmed before being validated and before being stored. **Discarded:** validating the attributes specific to each type, such as the connection type of a controller, because the diagram and the instructions of the Technical Leader do not define a rule for them, and a rule that was not requested would be an invented requirement. |
| Related commit | `5679e6a` (preload data file), `fd33932` (`AccessoryRepository`), `d49440a` (`AccessoryService` registration rules), `916101b` (`AccessoryService` queries), `26e41cf` (`updateStock`), plus the commit that carries this log |

---

## Entry 004 — Promotion module repository and service

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | OpenCode with the free model `nemotron-3.5-super-vl-free` |
| Phase and branch | Requerimiento 2 del Parcial Primer Corte (módulo de promociones). Branch `feature/promotion-module`, taken from the updated `develop` with the model classes of Developer 1 already merged. |
| Goal | Implement the two classes of the promotion module assigned to Developer 2 in `docs/promotion-class-diagram.md`: `PromotionRepository` and `PromotionService`, together with `data/promotions.csv`, following the specification of the Technical Leader exactly. |
| Query | "Create `data/promotions.csv` with the header and the three promotions valid today." / "Implement `PromotionRepository` with `saveAll` and `loadAll` that declare `throws IOException`, rebuild the right subclass from the first column, read the dates with `LocalDate.parse`, return an empty list when the file is missing, skip the header and ignore damaged lines." / "Implement `PromotionService` with the constructor loading the promotions and turning an `IOException` into an `IllegalStateException`." / "Add the three register methods, `listAllPromotions`, `listActivePromotions`, `findById` and `findBestPromotionFor`, saving after every registration." / "Reject invalid data with `IllegalArgumentException` and a Spanish message." |
| Answer | The model read the promotion class diagram and the promotion classes of Developer 1 to use their exact API, and checked that those classes only need `sale.getDate()`, `sale.getProducts()`, `product.getPrice()` and `product.getCategory()`, which already exist in the current state of `Sale`. It implemented the two classes and the data file with one commit per logical unit, six commits in total, each pushed right after it was created. Because the machine has no Java 17 and no Maven, the code could not be compiled, so it was verified statically: brace and parenthesis balance on every file, detection of unused imports, and confirmation of the eight public signatures and two attributes against the class diagram. The preload file was checked to have seven columns per line and to be saved in UTF-8. |
| Decision | **Accepted:** the file errors of `PromotionService` become `IllegalStateException` in the constructor and in a private save method, the same as the other services. The dates are read with `LocalDate.parse`, whose expected format is exactly the `yyyy-MM-dd` of the file, so no custom parser was written. The type written in the first column is derived from the concrete class with a private method, because the model of this module does not declare a type discriminator the way the accessory module does. `listActivePromotions` and `findBestPromotionFor` share one private method that filters by date, because both need the same rule and only differ in the date they use. `findBestPromotionFor` returns null when the sale has no date, and it never changes the sale. **Modified:** records of the promotions file are accepted with six or seven columns, so a line that lost its trailing semicolon is still read. **Discarded:** validating that the registered dates are not in the past, because a promotion is allowed to be registered before it starts and the statement says that future and expired promotions exist in the system. |
| Related commit | `84a69c3` (preload data file), `3287964` (`PromotionRepository`), `1a86691` (`PromotionService` registration rules), `549ac2d` (promotion queries), `4a4b4cf` (`findBestPromotionFor`), plus the commit that carries this log |

---

## Entry 005 — Warranty module repository and service

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | OpenCode with the free model `nemotron-3.5-super-vl-free` |
| Phase and branch | Requerimiento 4 del Parcial Primer Corte (módulo de garantías). Branch `feature/warranty-module`, taken from the updated `develop` with the model classes of Developer 1 already merged. |
| Goal | Implement the two classes of the warranty module assigned to Developer 2 in `docs/warranty-class-diagram.md`: `WarrantyRepository` and `WarrantyService`, avoiding the circular dependency described in the last section of `docs/warranty-analysis.md`. |
| Query | "Implement `WarrantyRepository` with `data/warranties.csv`, a header line, the format `type;id;productId;saleId;startDate`, and `loadAll(sales, catalog)` that receives the sales and the products already loaded and links each warranty by id, the same way `SaleRepository` does." / "The end date is not stored because the constructor computes it." / "Skip the damaged lines and the lines whose sale or product no longer exist." / "Implement `WarrantyService` starting with an empty list and loading the file through `loadWarranties(sales, catalog)`." / "Assign warranties with the next id, `W0001`, `W0002`..." / "Look up a warranty ignoring capitalisation and return the one that lasts longer." / "Reject a number of days that is not greater than zero." |
| Answer | The model read the warranty class diagram, the last section of `docs/warranty-analysis.md` and the warranty classes of Developer 1, and used `SaleRepository` as the reference for the linking by id. It implemented the two classes with one commit per logical unit, six commits in total, each pushed right after it was created. Because the machine has no Java 17 and no Maven, the code could not be compiled, so it was verified statically: brace and parenthesis balance on every file, detection of unused imports, confirmation of the public signatures against the class diagram, and a check of every method it invokes on the warranty classes of Developer 1. |
| Decision | **Accepted:** the repository stores only identifiers and receives the sales and the catalogue as arguments, so it depends on the model alone and `WarrantyService` starts empty, exactly as the analysis requires to avoid the cycle with `SaleService`. A record whose sale or product no longer exists is skipped instead of failing, which is a deliberate difference from `SaleRepository`, because `SaleRepository` throws in that situation and the specification of this module asks for the line to be skipped. The next identifier is built from the highest number already in use rather than from the size of the list, so an identifier is never reused. `getWarrantyType()` was not used as the marker of the first column, because it returns the name shown to the user, `"Garantía Básica"`, and not the `BASIC` marker of the file. The class documentation of both classes explains why the objects are loaded in that order, because that decision is the answer to the circular dependency question and will be asked in the oral defence. **Modified:** the identifiers used to link a warranty to its sale or product are compared exactly, following `SaleRepository`, and only `findWarrantyByProduct` ignores capitalisation, as the specification asks. **Discarded:** validating whether a product is eligible for a warranty, because that is a rule of the sale, so only the values needed to build the warranty are checked. No preload file was created, because the statement says this module does not need one. |
| Related commit | `87017bd` (`WarrantyRepository`), `63dbbdb` (`WarrantyService` loading and assignment), `8bbec09` (`findWarrantyByProduct`), `9a45d77` (active and expiring queries), `ed928f1` (documentation of the format and the query boundaries), plus the commit that carries this log |

---

## Entry 006 — Return module repository, service and monthly balance

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | OpenCode with the free model `nemotron-3.5-super-vl-free` |
| Phase and branch | Requerimiento 3 del Parcial Primer Corte (módulo de devoluciones). Branch `feature/return-module`, taken from the updated `develop` with the `Return` class and `Sale.canBeReturned()` of Developer 1 already merged. |
| Goal | Implement the two classes of the return module assigned to Developer 2 in `docs/return-class-diagram.md`: `ReturnRepository` and `ReturnService`, including the monthly balance, without breaking the layers as described in "Loading the returns without breaking the layers" of `docs/return-analysis.md`. |
| Query | "Implement `ReturnRepository` with `data/returns.csv`, the format `id;date;saleId;productIds;reason`, the product identifiers separated by commas, a semicolon inside the reason saved as a comma, and the refund not stored." / "Implement `loadAll(sales)` that links each return to its sale by id and each returned unit to the product of that sale with the same id." / "Skip the damaged lines and the lines whose sale or product cannot be found." / "Implement `ReturnService` with `ReturnRepository`, `SaleService` and `ProductService`, loading the returns in the constructor." / "Implement `registerReturn` with the checks in order, changing nothing until every check passes, restoring the stock of video games and consoles but not of accessories." / "Add the three queries ignoring capitalisation and the monthly balance." |
| Answer | The model read the return class diagram, the section about loading without breaking the layers, the `Return` class and the APIs of `Sale`, `SaleService` and `ProductService`. It implemented the two classes with one commit per logical unit, six commits in total, each pushed right after it was created. Because the machine has no Java 17 and no Maven, the code could not be compiled, so it was verified statically: brace and parenthesis balance on every file, detection of unused imports, confirmation of the public signatures against the class diagram, and a check of every method it invokes on the classes of the other members. The fully qualified name of `DateTimeParseException` was also checked, because in the promotion module it was written with the wrong package and the compilation failed. |
| Decision | **Accepted:** `ReturnRepository` receives the sales already loaded and resolves the returned products inside `sale.getProducts()`, so the persistence layer keeps depending only on the model and the statement suggestion of injecting the services into the repository was not followed, exactly as the analysis explains. `registerReturn` validates the sale, the thirty day window, the request and the available units before it creates anything, so a rejected request never restores stock. The available units are the units of the sale minus the units already returned in earlier returns of that same sale, which is what allows a partial return to be registered more than once. Each identifier is restored with a single `restoreStock` call carrying the number of units, instead of one call per unit. The monthly balance groups each side by the date of its own record, so a sale refunded in a later month counts as income in the month of the sale and as a refund in the month of the return. **Modified:** the header line is detected by comparing its first two columns instead of being skipped blindly, so a file without a header is also read. **Discarded:** restoring the stock of returned accessories, because the scope of the requirement assigns that adjustment to Requirement 5. |
| Related commit | `bb3219a` (`ReturnRepository`), `aeb223c` (`ReturnService` loading and queries), `abb90e3` (`registerReturn` with stock restoration), `138243a` (queries by customer and sale), `2b8edf6` (monthly balance), plus the commit that carries this log |
