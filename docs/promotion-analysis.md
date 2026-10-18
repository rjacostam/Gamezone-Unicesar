# Promotion Module Analysis — GameZone Unicesar

Answers to the five guiding questions of Requirement 2. Every class and method named here
appears in `docs/promotion-class-diagram.md`.

---

## 1. How the hierarchy reflects shared data and different calculations

All promotions share the same data: an `id`, a `name`, a `startDate` and an `endDate`. They
also share a behaviour: knowing whether they are active on a date. What changes is how
each one computes its discount.

That is why the design uses an abstract class `Promotion` with three subclasses:

| Class | What it adds | How it computes the discount |
|---|---|---|
| `PercentageDiscount` | `percentage` | A percentage of the sale subtotal |
| `CategoryDiscount` | `percentage`, `targetCategory` | A percentage of the price of the products of one category |
| `BulkPurchaseDiscount` | `minimumQuantity`, `percentage` | A percentage of the subtotal, only if the sale has at least `minimumQuantity` units |

The mechanism is **polymorphism**. `PromotionService` works with a `List<Promotion>` and
calls `calculateDiscount(sale)` on each element. Java runs the version of the real object,
so the service, `SaleService` and the menu never need to know which concrete type they are
using, and no `instanceof` checks are needed. A new type of promotion would only need a
new subclass.

---

## 2. How `calculateDiscount` is declared in the base class

As an **abstract method** in `Promotion`:

```java
public abstract double calculateDiscount(Sale sale);
```

`Promotion` is also declared `abstract`, so it cannot be instantiated.

This guarantees that every concrete subclass implements `calculateDiscount`. A subclass
that does not implement it does not compile. Each implementation uses `@Override`.

`Promotion` declares a second abstract method, `getDescription()`, so each type can
describe its own rule when the menu lists the promotions.

---

## 3. Where the "best promotion" selection lives

In **`PromotionService.findBestPromotionFor(Sale sale)`**:

1. It takes the promotions active on the date of the sale.
2. It asks each one for `calculateDiscount(sale)`.
3. It returns the one with the highest amount, or `null` if no promotion gives a discount
   greater than zero.

It belongs in the service layer because choosing among all the promotions of the store is
a business rule that involves many objects. `PromotionService` is the class that knows the
list of promotions.

It does **not** belong in:

- **`Sale`**: a sale knows its own products and prices, but not the promotions of the
  store. Putting the selection there would make the model depend on the full promotion
  list and on how it is loaded.
- **`ConsoleMenu`**: the UI only reads input and shows results. If the rule lived there,
  it would not apply to sales registered any other way, and it would break the rule that
  business logic lives in the services.

`SaleService.registerSale` asks `PromotionService` for the best promotion and applies the
result to the sale.

---

## 4. Changes in `Sale` and `generateReceipt`

New private attributes in `Sale`:

- `subtotal`: sum of the prices of every unit.
- `appliedPromotionName`: name of the applied promotion, or `null` if none applies.
- `discountAmount`: discount in pesos, `0` by default.

New and changed methods:

- `calculateSubtotal()` adds up the prices.
- `calculateTotal()` now returns `subtotal - discountAmount`.
- `applyDiscount(promotionName, amount)` stores the promotion and the amount and
  recalculates the total.
- `getSubtotal()`, plus getters and setters for `appliedPromotionName` and
  `discountAmount`.

`generateReceipt()` now shows the subtotal, the discount with the name of the promotion
(or "sin promoción aplicable") and the final total.

Nothing that already existed breaks:

- The constructor keeps the same parameters.
- A sale without a promotion has a discount of `0`, so its total is the same as before.
- `getTotal()` still returns the amount the customer pays.

`SaleRepository` stores two more columns (promotion name and discount) so the discount is
not lost when the application is closed. Sales saved before this requirement still load
correctly, with no discount.

---

## 5. Where the validity check is done

In both, each with its own job:

- **`Promotion.isActive(LocalDate date)`** checks whether a date is between `startDate`
  and `endDate`, both included. Only the promotion knows its own dates, so the check
  belongs to it (*Information Expert*). It receives the date as a parameter, so it does
  not depend on the system clock.
- **`PromotionService`** decides *which* date to use and filters the list:
  - `listActivePromotions()` uses today's date;
  - `findBestPromotionFor(sale)` uses the date of the sale.

Expired and future promotions stay registered and appear in `listAllPromotions()`, but
`isActive` returns `false` for them, so they are never applied.

---

## Categories of the products

`CategoryDiscount` needs to know the category of each product of the sale. For that,
`Product` gets a new abstract method `getCategory()`:

- `VideoGame` returns `"VIDEOGAME"`.
- `Console` returns `"CONSOLE"`.
- `Accessory` returns `"ACCESSORY"`.

`CategoryDiscount` compares `product.getCategory()` with its `targetCategory`, again using
polymorphism instead of `instanceof`. In this requirement the allowed target categories
are `"VIDEOGAME"` and `"CONSOLE"`.
