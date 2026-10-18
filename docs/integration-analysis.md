# Integration Analysis — GameZone Unicesar

Requirement 5 joins the four modules built in Requirements 1 to 4 (accessories, promotions,
warranties and returns) into one system. Each module worked on its own, but some
conflicts only appear when they run together. This document describes each integration
adjustment: the cause, the solution applied, the branch and Pull Request where it was
integrated, and who made it.

| ID | Type | Branch | PR | Owner |
|---|---|---|---|---|
| A1 | Feature | `feature/accessory-category-discount` | #21 | Developer 1 |
| A2 | Fix | `fix/warranty-circular-dependency` | #22 | Developer 2 |
| A3 | Refactor | `refactor/unified-sale-registration` | #23 | Technical Leader |
| A4 | Fix | `fix/return-accessory-stock` | #24 | Developer 2 |
| A5 | Fix | `fix/return-discounted-refund` | #25 | Developer 1 |
| A6 | Fix | `fix/monthly-balance-report` | #26 | Developer 2 |
| A7 | Feature | `feature/return-warranty-cancellation` | #27 | Developer 2 |

The adjustments followed the phase order of the requirement: A1 (phase 2), then A2 and A3
(phase 3), then A4 to A7 (phase 4), and finally this documentation (A8) and the
publication to `main` (A9).

---

## A1 — Category promotions for accessories

**Cause.** Requirement 2 only allowed `VIDEOGAME` and `CONSOLE` as the target category of a
`CategoryDiscount`. Once accessories were part of the catalog, the store could not launch
a promotion on them.

**Solution.**
- `PromotionService.registerCategoryDiscount` now accepts `VIDEOGAME`, `CONSOLE` or
  `ACCESSORY`.
- `CategoryDiscount` already compared `product.getCategory()` with its target, and
  `Accessory.getCategory()` returns `ACCESSORY`. So every accessory is recognized through
  polymorphism, without `instanceof`.
- The menu offers "3. Accesorios" when registering a category promotion.
- `data/promotions.csv` adds `P-04 Accesorios al 15%`.

**Result.** A sale of two controllers ($700,000) receives "Accesorios al 15%" (−$105,000),
which is better than the 5% general promotion.

---

## A2 — Circular dependency in the warranty module

**Cause.** The Requirement 4 statement asked `WarrantyRepository` to resolve its references
to `Sale` while loading. Done literally, that creates the cycle
`SaleService → WarrantyService → WarrantyRepository → SaleService`, and the objects cannot
be built in `Main` through constructor injection.

**What was already prevented in Requirement 4.** The team detected the cycle while
designing the module: `WarrantyService` started empty, and `Main` loaded the warranties
after creating `SaleService`. But `WarrantyRepository.loadAll(sales, catalog)` still
rebuilt `Sale` and `Product` objects inside the persistence layer.

**Solution.**
- A new class `WarrantyRecord` in the persistence layer holds only data: type, id, product
  id, sale id and start date.
- `WarrantyRepository.loadAll()` takes no parameters. It returns `List<WarrantyRecord>` and
  no longer imports `Sale` or `Product`.
- `WarrantyService.loadWarranties(List<Sale> sales)` turns each record into a
  `BasicWarranty` or `ExtendedWarranty`. It finds the sale by id and the product inside
  `sale.getProducts()`, and skips records whose sale or product no longer exist.
- `Main` calls `warrantyService.loadWarranties(saleService.listAllSales())`.
- `docs/warranty-class-diagram.md` was updated.

**Why not inject `SaleRepository`.** The statement suggests giving `WarrantyService` a
`SaleRepository`. Loading the sales a second time would create duplicate `Sale` objects:
the same sale would exist twice in memory, and a warranty would not point to the sale the
rest of the system uses. Passing the sales already loaded keeps a single object per sale.

---

## A3 — Unified sale registration flow

**Cause.** Requirements 1, 2 and 4 each changed `SaleService.registerSale` separately.
Integrated, the order of the operations decides the result. For example, the discount
must be computed on the subtotal only, never on the warranty cost.

**Solution.** `registerSale` was reorganized into the eight steps of the requirement. Each
step is a private method with a numbered comment:

1. Check that the sale has at least one item, an existing customer and an existing seller.
2. Resolve each item as a product or an accessory and check its stock (`resolveItems`,
   `countUnits`, `checkStock`). Also validate the extended warranty requests.
3. Create the sale, which computes its subtotal.
4. Apply the best active promotion (`applyBestPromotion`), computed only on the subtotal.
5. Assign the basic warranty of every console and the requested extended warranties
   (`assignWarranties`).
6. Leave the total as `subtotal − discount + extended warranty cost`.
7. Update the inventory through `ProductService` or `AccessoryService`
   (`updateInventory`).
8. Save the sale. Each warranty is saved when it is assigned.

Every rule is checked in steps 1 and 2, before anything changes, so a rejected sale leaves
the inventory and the warranties untouched. The receipt already showed the subtotal, the
discount with the promotion name, the extended warranty cost and the total, and the menu
already asked for the extended warranty of each console.

**Verification.** The same sales were run before and after the change. The receipts,
errors, warranties and stock files were identical.

---

## A4 — Stock of returned accessories

**Cause.** Requirement 3 restored stock only through `ProductService.restoreStock`, which
does not manage accessories. When an accessory was returned, its stock did not go back.

**Solution.**
- New method `AccessoryService.restoreStock(String accessoryId, int quantity)`, equivalent
  to the one in `ProductService`.
- `ReturnService` receives `AccessoryService` in its constructor and restores stock by
  item type: accessories through `AccessoryService`, video games and consoles through
  `ProductService`.
- `ReturnRepository` already linked returned accessories through the products of the
  sale, so loading did not need changes.

**Result.** Returning a controller brings its stock back from 7 to 8.

---

## A5 — Refund of discounted sales

**Cause.** `Return.calculateRefundAmount` added up the list prices of the returned items.
If the sale had a promotion, the customer got back more than they paid.

**Solution.**
- Each returned item is refunded in proportion to the discount of the original sale:
  `price × (1 − discountAmount / subtotal)`.
- The return receipt shows, for every item, the list price, the proportional discount and
  the refund.

**Result.** In a sale with 10% off ("Lleva 3 o más"), returning a $2,500,000 console
refunds $2,250,000 and returning a $350,000 controller refunds $315,000.

---

## A6 — Monthly balance report

**Cause.** Requirement 3 asks for the sales total, the returns total and the net balance.
`generateMonthlyBalance` returned only the net value. Also, with promotions and warranties
integrated, the sales total must use the final total of each sale.

**Solution.**
- New method `calculateMonthlySales(month, year)`: the sum of `getTotal()` of the sales of
  the month, with discounts and extended warranties included.
- New method `calculateMonthlyReturns(month, year)`: the sum of the refunds of the returns
  of the month.
- `generateMonthlyBalance` keeps its signature and returns the difference.
- The menu shows the three values.

---

## A7 — Cancelling warranties of returned consoles

**Cause.** No requirement defined what happens to the warranty of a returned console. In
the integrated system, a returned console cannot keep an active warranty.

**Solution.**
- New method `WarrantyService.cancelWarranties(String productId, String saleId)`. It removes
  the basic warranty and, if there is one, the extended warranty of one unit of that
  console in that sale. It saves the file and returns the refundable cost: `0` for the
  basic warranty and the additional cost for the extended one.
- `ReturnService.registerReturn` calls it once for each returned console unit and passes
  the sum to the new `Return`.
- `Return` keeps `warrantyRefund`, adds it to the refund amount, and shows it in the
  receipt ("Reembolso por garantías anuladas"). `ReturnRepository` stores it as a new
  column; older lines load with `0`.

**Defect found in review.** At first the file was saved only when the refund was not
zero, so cancelling a basic-only warranty was lost after restarting. The fix saves
whenever a warranty is removed.

---

## Integrated demo scenario

The scenario of the oral presentation was run on the integrated system:

1. Sale to customer `123` by seller `EMP-001`: `C-01` PlayStation 5 Slim ($2,500,000),
   `VG-01` Zelda ($250,000) and `A-01` DualSense controller ($350,000). Subtotal
   $3,100,000.
2. Best active promotion: "Lleva 3 o más" (10%), −$310,000.
3. Extended warranty for `C-01`: +$250,000. Total **$3,040,000**. Active warranties: one
   basic and one extended for `C-01`.
4. Partial return of `C-01` and `A-01`. Refund: $2,250,000 + $315,000 + $250,000 for the
   cancelled extended warranty = **$2,815,000**.
5. Stock back: `C-01` = 5 and `A-01` = 8. The warranties of `C-01` in that sale are gone.
6. Balance of 10/2026: sales $3,040,000, returns $2,815,000, net **$225,000**.
