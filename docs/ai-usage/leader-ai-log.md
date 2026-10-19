# AI Usage Log — Technical Leader

Owner: Rafael Junior Acosta Mendoza

Each interaction with an AI tool is recorded as one entry with the fields below.

## Entry 1

| Field | Content |
|---|---|
| Date | 2026-10-17 |
| Tool | Claude Code (Claude Opus 5.5) |
| Phase and branch | Repository setup — `main`, `feature/project-setup` |
| Goal | Set up the initial repository and Maven project structure for the four layers. |
| Query | Asked for help reading the workshop statement and creating the repository skeleton (README, .gitignore, license, pom.xml, packages, data and docs folders, TEAM.md). |
| Answer | The tool created the initial commit on `main`, the `develop` branch, the `pom.xml` for Java 17, the `model`, `persistence`, `service` and `ui` packages with package documentation, a placeholder `Main`, and the folder layout required by the workshop. |
| Decision | Accepted the structure. Domain classes, analysis answers and diagrams are left to the team. |
| Related commit | `chore: configure Maven project for Java 17`, `chore: create package structure for the four layers` |

## Entry 2 — Taller 1 and Requirements 1 to 4

| Field | Content |
|---|---|
| Date | 2026-10-17 and 2026-10-18 |
| Tool | Claude Code (Claude Opus 5.5) |
| Phase and branch | Taller 1 (`feature/sale-module` and integration), R1 `feature/accessory-module`, R2 `feature/promotion-module`, R4 `feature/warranty-module`, R3 `feature/return-module` |
| Goal | Complete my part as Technical Leader in each requirement and coordinate the team. |
| Query | Asked the tool to write my documents (analysis answers and class diagrams), my integration code in `Sale`, `SaleRepository`, `SaleService`, `ConsoleMenu` and `Main`, and the README; to compile and test the branches of the developers before I approved them; and to prepare the PR titles, descriptions and messages for the team. |
| Answer | The tool wrote that code and documentation, pushed my commits, compiled every developer branch and ran the menu with scripted inputs. It found errors before approval, for example a wrong package of `DateTimeParseException` and a Spanish variable name. |
| Decision | I decided the order of the work (one shared branch per requirement: leader docs, Developer 1, Developer 2, leader), asked for corrections when a review failed and approved each PR. I followed the tool's checks before every approval. |
| Related commit | PRs #1 to #20 |

## Entry 3 — Requirement 5 (integration)

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Claude Code (Claude Opus 5.5) |
| Phase and branch | Requirement 5 — one `fix/` or `refactor/` branch per adjustment |
| Goal | Resolve the nine integration adjustments (A1 to A9) without breaking the four modules. |
| Query | Asked the tool to implement my adjustments, write `integration-analysis.md` and `integrated-class-diagram.md`, review the developers' adjustments and verify the demo scenario. |
| Answer | The tool implemented and tested the adjustments assigned to me, checked the others, found that cancelled basic warranties were not saved (A7) and confirmed the demo totals: sale $3,040,000, refund $2,815,000, net $225,000. |
| Decision | Accepted. The release to `main` was made with PR #29. |
| Related commit | PRs #21 to #29 |

## Entry 4 — Requirement 6 (exceptions and validation)

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Claude Code (Claude Opus 5.5) |
| Phase and branch | Requirement 6 — `feature/exception-handling` |
| Goal | Design the exception hierarchy and handle each error type in the menu. |
| Query | Asked the tool to write `exception-analysis.md` and `exception-class-diagram.md`, the `runSafely` method of `ConsoleMenu`, the startup catch in `Main` and the README section, and to review the developers' branches. |
| Answer | The tool wrote those parts and changed the order of the work so that `develop` never crashed. It found that Developer 2's first version did not compile, and that a customer stayed in memory when saving failed. It compared the output before and after with the same inputs. |
| Decision | Accepted. I approved PRs #30 to #32 only after the fixes compiled and the data files stayed identical. |
| Related commit | PRs #30, #31, #32 |


---

# Requirement 7 — Phase 1 (Audit)

## Entry 5

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Claude Code (Claude Opus 5.5) |
| Phase and branch | Requirement 7, Phase 1 — `feature/grasp-audit` |
| Goal | Audit my own classes against the nine GRASP patterns. |
| Query | Asked the tool to explain each pattern and to guide me with questions on fragments of my code instead of giving me the findings. |
| Answer | The tool asked 16 questions. I answered each one with my verdict and reasoning, and the tool pointed out missing evidence, wrong classifications and findings that overlapped. |
| Decision | The verdicts and the five violations are my answers, corrected after the feedback. The tool translated them into English in the format of the statement, checked the line numbers against `develop` at `6c10ce2` and pushed the report. |
| Related commit | `8597c43` to `5bd1bbc` |

## Entry 6

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Claude Code (Claude Opus 5.5) |
| Phase and branch | Requirement 7, Phase 1 — reviews of PR #33 and PR #34 |
| Goal | Validate the evidence of the developer reports before approving them. |
| Query | Asked the tool to check both reports against the code: line numbers, owners of each class and duplicated findings. |
| Answer | It found a wrong evidence in Developer 1's V-01 (`CategoryDiscount`), a duplicated table, wrong class owners and classes of other members audited twice in Developer 2's report. |
| Decision | I returned each problem to its author and approved both PRs after the corrections. |
| Related commit | PR #33, PR #34 |

---

# Requirement 7 — Phase 2 (Correction)

## Entry 7

| Field | Content |
|---|---|
| Date | 2026-10-18 |
| Tool | Claude Code (Claude Opus 5.5) |
| Phase and branch | Requirement 7, Phase 2 — `feature/grasp-refactor` |
| Goal | Prepare the no regression check, correct my part of the violations and verify the corrections of the developers. |
| Query | Asked the tool to write the two menu scripts and the strategy of `grasp-refactor-log.md`, draw the before and after diagrams, implement my corrections (V-01, V-03, V-04, V-05, V-06 and V-07 in `ProductService`, `Sale`, `SaleService` and the `ui` package), update the README, and compile and test every commit of the developers. |
| Answer | The tool did those changes one commit at a time and ran the two scripts after each one. Every check gave the same output and the same data files as the reference run on `develop`. It also proposed the names that the team used to coordinate (`increaseStock`, `isConsole`, `InventoryCatalog`, `getTypeCode`) and the order of the turns. |
| Decision | Accepted. I gave the team the order of the turns, approved PRs #36 and #37 after the checks, and kept `getDescription` in the model as Developer 1 proposed, because moving it would create a new violation. |
| Related commit | `c628d84` to `6e7a5ab` (my corrections and logs), `99840bd` (final verification), `b718b8b` (after diagram), `f340cde` (README) |
