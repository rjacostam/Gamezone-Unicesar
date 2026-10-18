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