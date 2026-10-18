# Warranty Module Analysis — GameZone Unicesar

Answers to the five guiding questions of Requirement 4. Every class and method named here
appears in `docs/warranty-class-diagram.md`.

---

## 1. How the hierarchy reflects common data and different rules

Both warranty types share the same data: an `id`, the `product` they cover, the `sale`
they come from, a `startDate` and an `endDate`. They also share two behaviours: knowing
whether they are active on a date (`isActive`) and printing a certificate
(`generateWarrantyCertificate`). What changes is the duration, the name of the type and
the cost.

The design uses an abstract class `Warranty` with two subclasses:

| Class | Duration | Type name | Additional cost |
|---|---|---|---|
| `BasicWarranty` | 6 months | `Garantía Básica` | `0` |
| `ExtendedWarranty` | 12 months | `Garantía Extendida` | 10% of the product price |

The common data and the two concrete methods are written once in `Warranty`. Each subclass
only answers three questions through **abstract methods**: `getDurationInMonths()`,
`getWarrantyType()` and `getAdditionalCost()`. The mechanism is **inheritance plus
polymorphism**: `Warranty` uses `getDurationInMonths()` to compute the end date, and Java
runs the version of the real object. The duration is never duplicated in two places.

---

## 2. Where "only consoles get a basic warranty" is decided

In the **service layer**, inside `SaleService.registerSale`. Deciding what happens when a
sale is registered is a business process that combines the sale, its products and the
warranty module, so it belongs to the service that coordinates the sale.

The mechanism is the **`instanceof`** operator: for each product of the sale,
`if (product instanceof Console)` checks its real type at runtime. A `Console` gets a
`BasicWarranty`; video games and accessories do not.

It does not belong in:
- **`Product`**: the model would have to know about warranties.
- **`ConsoleMenu`**: the rule would only apply to sales made from the menu.

---

## 3. How the end date is computed

`Warranty`'s constructor receives `id`, `product`, `sale` and `startDate`, and computes:

```java
this.endDate = startDate.plusMonths(getDurationInMonths());
```

`getDurationInMonths()` is abstract. `BasicWarranty` returns `6` and `ExtendedWarranty`
returns `12`, so each subclass gets its own end date without repeating the calculation.

It is done **in the constructor**, because:
- A warranty always has a valid `endDate` from the moment it exists. No code can create a
  warranty and forget to compute its end.
- `endDate` has no setter, so it can never become inconsistent with `startDate` and the
  type.
- The subclasses return constants and do not use their own fields in
  `getDurationInMonths()`, so calling it from the parent constructor is safe.

---

## 4. Where the 10% extended warranty cost is applied

In `SaleService.registerSale`, after the sale is created and the promotion is applied, and
before the inventory is updated and the sale is saved.

The method changes in these ways:

1. **New parameter.** `registerSale(customerId, sellerId, productIds,
   productIdsWithExtendedWarranty)`. An empty or `null` list means no extended warranty.
2. **Validation.** Every id in that list must be a console included in the sale.
   Otherwise the sale is rejected before anything changes.
3. **Warranties.** For each unit of the sale that is a `Console`:
   - it calls `WarrantyService.assignBasicWarranty(product, sale, sale.getDate())`;
   - if its id is in the list, it also calls `assignExtendedWarranty(...)` and adds
     `warranty.getAdditionalCost()` to the sale with `sale.addExtendedWarrantyCost(cost)`.

`Sale` keeps a new attribute, `extendedWarrantyCost`, and its total becomes:

```
total = subtotal - discountAmount + extendedWarrantyCost
```

The promotion discount is still computed only on the subtotal, so the warranty cost is
never discounted. The receipt shows a new line with the extended warranty cost.

---

## 5. Where "warranties expiring soon" lives

In **`WarrantyService.listWarrantiesExpiringSoon(int daysAhead)`**. It goes through the
warranties and keeps the ones that are active today and whose `endDate` is not after
today plus `daysAhead` days. It rejects a `daysAhead` that is not greater than zero.

Its only dependency is the list of warranties already held by the service. Each warranty
answers `isActive(today)` and `getEndDate()`.

This placement fits the layered architecture:
- Filtering the warranties of the whole store is a business query. It belongs to the
  service that owns them, as `listActivePromotions` does for promotions.
- The model keeps only the rule of one warranty (`isActive`).
- The menu only asks how many days and prints the result.
- The repository only reads and writes the file.

---

## Avoiding a circular dependency

A warranty points to a `Sale`, and `SaleService` needs `WarrantyService` to create
warranties. If `WarrantyRepository` needed `SaleService` to rebuild the sales when
loading, the objects could not be built in `Main`: each one would need the other first.

To avoid that:

- `WarrantyRepository` stores **only ids** in `data/warranties.csv`: type, warranty id,
  product id, sale id and start date. The end date is computed again by the constructor.
- `WarrantyRepository.loadAll(sales, catalog)` receives the sales and products already
  loaded, and links each warranty to them. It depends only on the model, like
  `SaleRepository`.
- `WarrantyService` starts empty. `loadWarranties(sales, catalog)` fills it.
- `Main` creates `WarrantyService`, passes it to `SaleService`, and once the sales are
  loaded calls `warrantyService.loadWarranties(saleService.listAllSales(),
  saleService.getCatalog())`.

This way every warranty points to the same `Sale` and `Product` objects used by the rest
of the system, and no object is loaded twice.
