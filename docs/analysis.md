# GameZone Unicesar — Analysis (English)

Team GameZone Unicesar. Decisions below guide implementation in `com.gamezone`.

## 1. People: common vs specific attributes
All people share `name`, `nationalId` and `phone`. A `Customer` adds `email` (contact + purchase history is derived from sales, not stored twice). A `Seller` adds `employeeCode` and `shift`. This maps to a hierarchy: abstract `Person` holds common state and behavior, `Customer` and `Seller` extend it with their own fields. It avoids duplication and lets services handle any `Person` polymorphically.

## 2. Should a generic Person exist? Can it be instantiated?
Yes, as an abstraction, not as an object. There is no such thing in the store as a "plain person" without a role; every record is either a customer buying or a seller attending. So `Person` is `abstract`: it reuses code but cannot be instantiated. If someone tries `new Person(...)` the compiler stops it, forcing a concrete role.

## 3. Products: common vs specific
All products share `id`, `title`, `price` and `stock`. A `VideoGame` adds `platform`, `genre` and `ageRating`. A `Console` adds `brand`, `model` and `generation`. Same pattern: abstract `Product` with common inventory logic, two concrete subclasses with their own details.

## 4. Product description behavior
Each product type must show a full description combining its own fields. We declare `public abstract String getFullDescription()` in `Product`. The OOP mechanism is abstraction + polymorphism (dynamic dispatch): the base guarantees the contract, each subclass provides its own version with `@Override`, and callers just invoke it on a `Product` reference.

## 5. Sale relationships
A `Sale` involves one `Customer`, one `Seller` and one or more `Product`s. These are associations, not inheritance: a sale *has* a customer/seller/products, it *is not* a kind of person or product. Customer/Seller links are plain associations (1 each). Products link is composition-like aggregation (1..*): if the sale is discarded the line items go with it, but the catalog products themselves live on independently in inventory.

## 6. Who calculates the total?
`Sale` itself, via `calculateTotal()`. It is the information expert: it already owns the product list. Putting the sum in another class would create feature envy and spread the rule. `SaleService` still validates and orchestrates, but the math lives in `Sale` so total is always consistent with its items.

## 7. Guarantee at least one product
We enforce it twice: `Sale` constructor throws if the list is null or empty, and `SaleService.registerSale()` validates before persisting. Fail fast in domain, and friendly error in service/UI. A sale object can never exist in an invalid state.

## 8. Automatic stock update
`SaleService.registerSale()` orchestrates: 1) check every product has `stock > 0`, 2) call `productService.decreaseStock(id)`, 3) persist sale. Classes involved: `Sale` (items + total), `Product` (stock field), `ProductService` (stock rule), `ProductRepository` (file write), `SaleRepository` (file write). The UI never touches stock directly.

## 9. Four layers
- `model`: domain entities only (`Person,Customer,Seller,Product,VideoGame,Console,Sale`). No I/O, no Scanner.
- `persistence`: file gateways (`ProductRepository,PersonRepository,SaleRepository`). Only CSV read/write + mapping to model.
- `service`: business rules (`ProductService,PersonService,SaleService`). Validations, stock, totals, queries.
- `ui` + root: `ConsoleMenu` and `Main`. Menus, input parsing, wiring.
Placement criterion: single responsibility + dependency direction. If a class talks to files it goes to persistence; if it enforces a store rule it goes to service; if it prints/reads console it goes to ui.

## 10. Why no file logic in domain?
It mixes two reasons to change and couples everything to `java.io`. Problems: cannot unit-test `Sale` without files, any format change ripples to business code, and reuse becomes painful. Keeping persistence separate lets us swap CSV for another format touching only repositories.

## 11. Allowed dependencies
Allowed: `ui -> service`, `service -> model + persistence`, `persistence -> model`. `model -> none`. This keeps the domain stable and testable. Forbidden: `model -> persistence/ui`, `ui -> persistence` directly (must go through service). The direction points inward toward the domain, never outward.
