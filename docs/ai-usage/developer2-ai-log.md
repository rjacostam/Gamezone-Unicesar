# AI Usage Log — Developer 2

Owner: José David Cervantes Solano

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

## Entry 001 — Implementation of the people module

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

## Entry 002 — Improvements to the people module

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | OpenCode with the free model `nemotron-3.5-super-vl-free` |
| Phase and branch | Taller 1, Part 5 (review of the people module). Branch `feature/person-module-improvements`, created from the updated `develop` after the module had been merged. |
| Goal | Apply the seven improvements requested by the Technical Leader on the merged people module, without changing the public signatures, the attributes, the file formats or the `throws IOException` clauses. |
| Query | "Validate that the customer phone has only digits." / "Store the e-mail in lower case." / "Make `getSummary()` show the role, for example `[Cliente]123 \| Ana....`" / "Make `PersonRepository` ignore damaged lines instead of failing on load." / "Make `findSellerById` ignore capitalisation and spaces." / "Complete the JavaDoc of the module." / "Update the AI usage log." |
| Answer | The model applied the seven changes as one commit each, pushed after every commit, and verified the result statically because the machine has no Java 17 and no Maven: brace and parenthesis balance on every file of the whole project, detection of unused imports, and confirmation that no public signature, attribute or `throws` clause changed. The model also reviewed every call site of the module in `Main`, `SaleService` and `ConsoleMenu` to confirm that the behaviour changes are compatible with the code of the other members. |
| Decision | **Accepted:** the phone validation was placed in `PersonService`, because the analysis document states that the validation lives in the services and not in the domain classes; the digits were compared against the ASCII range `'0'` to `'9'` instead of using `Character.isDigit(char)`, because that method also accepts digits of other writing systems; the e-mail normalisation was placed in `Customer.setEmail(String)` and the constructor was changed to call that setter, so the address is normalised no matter how the customer is built; `Locale.ROOT` was used for the lower casing, because the default locale of the machine would change the result for some letters; the role prefix of `getSummary()` was written in each subclass instead of in a shared helper, to keep `Person` exactly as it appears in `docs/class-diagram.md`; and the damaged record check was implemented as a private helper that rejects records with an empty field, since the field count was already being verified. **Modified:** nothing outside the five assigned files was touched, and `docs/` was not modified apart from this log, which is not a diagram document. `data/sellers.csv` was left untouched and no test data was committed. **Discarded:** the idea of making `findCustomerById(String)` case-insensitive as well. It was not requested, and it is reported to the Technical Leader as an open question, because `ProductService.findById(String)` already ignores capitalisation and the two searches would otherwise behave differently. |
| Related commit | `213fd77` (phone validation), `3285d3c` (e-mail in lower case), `533fe36` (role in the summary line), `b7278da` (skip damaged records), `6f24e6f` (seller search ignoring capitalisation), `b95ceb0` (JavaDoc), plus the commit that carries this log |