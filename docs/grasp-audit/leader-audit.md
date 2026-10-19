# Leader GRASP Audit — GameZone Unicesar

Author: Rafael (Technical Leader). Audited code: `develop` at commit `6c10ce2`.

## Scope

Classes and methods audited, by module and requirement, following the responsibility
table of Requirement 7.

| Module (requirement) | Class | Methods audited |
|---|---|---|
| Sales (Taller 1), discount and receipt (R2), warranty cost (R4) | `Sale` | constructor, `calculateSubtotal`, `calculateTotal`, `applyDiscount`, `addExtendedWarrantyCost`, `generateReceipt`, getters and setters. `canBeReturned` is audited by Developer 1. |
| Sales (Taller 1), extended in R2 and R4 | `SaleRepository` | `saveAll`, `loadAll`, `toLine`, `fromLine`, `findCustomer`, `findSeller`, `findProduct` |
| Sales (Taller 1), accessories (R1), promotions (R2), warranties (R4), integration (R5) | `SaleService` | constructor, `registerSale` and its private steps (`requireCustomer`, `requireSeller`, `resolveItems`, `checkStock`, `applyBestPromotion`, `assignWarranties`, `updateInventory`, `saveSales`), `getCatalog`, `findItem`, queries |
| User interface (Taller 1), integration of R1–R4, exceptions (R6) | `ConsoleMenu` | `start`, the seven submenus, `registerSale`, `readAccessoryType`, promotion category selection, `runSafely`, input helpers |
| Startup (Taller 1) | `Main` | `main` |
| Returns (R3) | `ProductService` | `restoreStock` |

## Information Expert

**Verdict: violation** (one point). The pattern is applied correctly in the rest of the
audited code.

Correct applications:
- `Sale.calculateSubtotal` and `Sale.calculateTotal` (lines 57–75) only use attributes of
  `Sale`: its products, the discount and the extended warranty cost. Each `Product` gives
  its own price with `getPrice()`. The sale already has all the information, so it does
  not need to ask another class.
- Promotions: in `SaleService.applyBestPromotion` (line 302), the discount is computed by
  `Promotion.calculateDiscount(sale)`, the class that knows the rules of each promotion,
  and it is stored by `Sale.applyDiscount`, because the amount belongs to that sale and
  is needed for its total.

| Field | Content |
|---|---|
| ID | V-01 |
| Pattern | Information Expert |
| Module | Returns (Requirement 3) |
| Evidence | `ProductService.restoreStock` (lines 162–168): `product.setStock(product.getStock() + quantity);` |
| Explanation | The service reads the stock of the product, adds the units and writes the value back. `Product` owns the `stock` attribute and already changes it itself in `decreaseStock`, so it is the expert for increasing it too. |
| Consequence | The rule for changing the stock is split between `Product` (decrease) and `ProductService` (increase). A new stock rule, such as a maximum stock, would have to be added in two places. |
| Proposed solution | Add `Product.increaseStock(int quantity)`, the counterpart of `decreaseStock`, and make `restoreStock` call it. Pattern applied: Information Expert. |
| Other member's code | Yes, Developer 1 (`Product`). |

## Creator

**Verdict: correct.**

- `SaleService.registerSale` creates the sale (line 129:
  `new Sale(nextSaleId(), LocalDate.now(), customer, seller, soldProducts)`). It has the
  data needed to initialize it (customer, seller, items and the next id) and it records
  the sales it creates (line 143: `sales.add(sale)`). Both are conditions of the pattern.
- `Main.main` (lines 41–60) creates the repositories, the services and the menu. It has
  the data needed to build them, such as the paths of the data files.
