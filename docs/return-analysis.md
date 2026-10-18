# Return Module Analysis — GameZone Unicesar

Answers to the five guiding questions of Requirement 3. Every class and method named here
appears in `docs/return-class-diagram.md`.

---

## 1. Relationship between `Return` and `Sale`

It is an **association** (`Return "0..*" --> "1" Sale`):

- **Not inheritance.** A return is not a kind of sale. It does not share the sale's data
  or behaviour; it only refers to one.
- **Not composition.** The sale is not owned by the return. The sale exists before the
  return and keeps existing after it, and deleting a return must never delete the
  original sale.
- **Not aggregation.** A return is not "made of" sales. It points to exactly one sale,
  the one the customer bought.

The reference is fixed. `Return` keeps the original `Sale` in a private attribute with a
getter and no setter, because the sale of a return can never change. A sale can have
`0..*` returns (partial returns on different days).

---

## 2. How a partial return is represented

`Return` keeps `returnedProducts`, a `List<Product>` with **only the units being
returned**, one entry per unit, in the same way `Sale` stores its products. If a sale had
two copies of a game and one console, and the customer returns one copy of the game, the
list has a single entry: that game.

The entries are the **same `Product` objects** that appear in the original sale. Two
things depend on that:
- the return can use their price;
- the service can restore their stock.

The rest of the products stay in the sale and are not part of the return.

---

## 3. Where the 30-day rule is validated

The rule itself is a fact about the sale, so `Sale` gets a new method:

```java
public boolean canBeReturned()
```

It returns `true` when today is between the sale date and 30 calendar days after it.

The **decision** to accept or reject a return is made in the **service layer**, in
`ReturnService.registerReturn`. Before changing anything, it asks `sale.canBeReturned()`,
and if the answer is `false` it throws an `IllegalArgumentException` with a message in
Spanish. The menu can also call `canBeReturned()` to warn the user early, but the service
is the place that guarantees the rule.

To compute the difference between the two dates, Java offers
`java.time.temporal.ChronoUnit`:

```java
long days = ChronoUnit.DAYS.between(getDate(), LocalDate.now());
return days >= 0 && days <= 30;
```

---

## 4. How the stock is restored

When a sale is registered, the stock is reduced through `ProductService.updateStock`.
Returns follow the same idea in the opposite direction. `ProductService` gets the method:

```java
public void restoreStock(String productId, int quantity)
```

It adds the units back to the product and saves the inventory. `ReturnService` invokes it
for each returned product, grouping the units of the same product.

It matters to go through `ProductService` instead of changing the stock directly:
- `ProductService` is the only owner of the inventory. Only it knows how the stock is
  kept and saved, so the rule lives in one place.
- If `ReturnService` changed `stock` and wrote the file itself, the inventory logic would
  be duplicated. A future change, for example a validation or a new file format, would
  have to be made twice, and forgetting one copy would leave the stock inconsistent.

---

## 5. Where the monthly balance lives

In **`ReturnService.generateMonthlyBalance(int month, int year)`**:
- it adds up the totals of the sales of that month;
- it subtracts the refunds of the returns of that month;
- it returns the net balance.

It belongs to `ReturnService` because:
- the report is a business calculation that combines two modules, and the service layer
  is where processes that span several objects live;
- `ReturnService` already knows the returns and already depends on `SaleService`, so it
  can reach both sides without adding new dependencies;
- putting the report in the menu would place a business calculation in the UI, and
  putting it in `Sale` or `Return` would make one domain object know the whole list of
  the other.

Its dependencies are:
- its own list of returns;
- `SaleService.listAllSales()`, for the sales of the month.

A sale counts in the month of its date, and a return counts in the month of its own date.

---

## Loading the returns without breaking the layers

The statement suggests injecting `SaleService` and `ProductService` into
`ReturnRepository`. That would make the persistence layer depend on the service layer,
which the architecture forbids (`persistence → model` only).

As with sales and warranties:

- `ReturnRepository.loadAll(List<Sale> sales)` receives the sales already loaded. It
  links each return to its sale by id, and each returned unit to the product of that sale
  with the same id. Since a returned product must belong to its sale, the sale is enough
  to find it. This works for video games, consoles and accessories.
- `ReturnService` receives `ReturnRepository`, `SaleService` and `ProductService` as the
  statement asks, and loads its returns in the constructor with
  `repository.loadAll(saleService.listAllSales())`.
- `Main` creates `ReturnService` after `SaleService`.

## Scope of this requirement

This requirement follows its own statement. Some details are completed later by the
integration adjustments of Requirement 5:

- **A4:** the stock of returned **accessories**. Here only `ProductService.restoreStock`
  is used.
- **A5:** refunds of sales that had a **discount**. Here the refund is the sum of the list
  prices.
- **A6:** showing sales, returns and balance separately. Here
  `generateMonthlyBalance` returns the net balance.
- **A7:** cancelling the **warranties** of returned consoles.
