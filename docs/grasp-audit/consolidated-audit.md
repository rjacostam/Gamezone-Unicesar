# Consolidated GRASP Audit — GameZone Unicesar

Consolidated by: Rafael Junior Acosta Mendoza (Technical Leader).

Audited code: `develop` at commit `6c10ce2`. The three individual reports use this same
commit, so every line number below refers to it.

Source reports:

- `docs/grasp-audit/leader-audit.md` (cited as *leader*);
- `docs/grasp-audit/developer1-audit.md` (cited as *dev1*);
- `docs/grasp-audit/developer2-audit.md` (cited as *dev2*).

## Coverage

The system has 48 classes. Each one was audited by its owner, once. Two classes were split
by method, following the responsibility table of Requirement 7.

| Package | Class | Audited by |
|---|---|---|
| (root) | `Main` | Technical Leader |
| `ui` | `ConsoleMenu` | Technical Leader |
| `model` | `Sale` (all methods except `canBeReturned`) | Technical Leader |
| `model` | `Sale.canBeReturned` | Developer 1 |
| `model` | `Product`, `VideoGame`, `Console` | Developer 1 |
| `model` | `Accessory`, `Controller`, `Cable`, `Memory` | Developer 1 |
| `model` | `Promotion`, `PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount` | Developer 1 |
| `model` | `Warranty`, `BasicWarranty`, `ExtendedWarranty` | Developer 1 |
| `model` | `Return` | Developer 1 |
| `model` | `Person`, `Customer`, `Seller` | Developer 2 |
| `persistence` | `SaleRepository` | Technical Leader |
| `persistence` | `ProductRepository` | Developer 1 |
| `persistence` | `PersonRepository`, `AccessoryRepository`, `PromotionRepository`, `ReturnRepository`, `WarrantyRepository`, `WarrantyRecord` | Developer 2 |
| `service` | `SaleService` | Technical Leader |
| `service` | `ProductService` (all methods except `restoreStock`) | Developer 1 |
| `service` | `ProductService.restoreStock` | Technical Leader |
| `service` | `PersonService`, `AccessoryService`, `PromotionService`, `ReturnService`, `WarrantyService` | Developer 2 |
| `exceptions` | `GameZoneException`, `ResourceNotFoundException`, `BusinessRuleException`, `InvalidDataException`, `PersistenceException` | Developer 1 |
| `validation` | the seven validators | Developer 1 |

No class was left without an audit and none was audited twice. During the review of the pull
requests, two overlaps were found and returned to their author before merging:
Developer 2 had included `Main` and `ConsoleMenu.runSafely`, which belong to the Technical
Leader, and an opinion on the validators, which belong to Developer 1. Both were removed from
`developer2-audit.md`.

## Team verdict per pattern

A pattern is a violation for the team when at least one report registers a violation of it
that survives the consolidation.

| Pattern | Leader | Dev1 | Dev2 | Team verdict | Final violations |
|---|---|---|---|---|---|
| Information Expert | Violation | Violation | Violation | **Violation** | V-01, V-02, V-03 |
| Creator | Correct | Correct | Correct | **Correct** | — |
| Controller | Violation | Not applicable | Correct | **Violation** | V-04 |
| Low Coupling | Correct | Violation | Violation | **Violation** | V-07, V-08 |
| High Cohesion | Violation | Violation | Correct | **Violation** | V-05, V-06 |
| Polymorphism | Violation | Violation | Violation | **Violation** | V-07, V-08, V-09 |
| Pure Fabrication | Correct | Correct | Correct | **Correct** | — |
| Indirection | Correct | Correct | Correct | **Correct** | — |
| Protected Variations | Correct | Violation | Correct | **Violation** | V-04 |

Some violations break more than one pattern. Each one is registered once, under the pattern
that guides its correction, and the other patterns are named inside it.
