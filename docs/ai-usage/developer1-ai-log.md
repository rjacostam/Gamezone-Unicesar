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
