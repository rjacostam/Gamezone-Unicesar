# Analysis — GameZone Unicesar

Answers to the eleven guiding questions of the workshop. Every class, attribute and
method named here appears in `docs/class-diagram.md`.

---

## People

### 1. Common and specific attributes of people

Every person who interacts with the store has an identification, a name and a contact
phone. On top of that, each role adds its own data:

| Class | Attributes |
|---|---|
| `Person` | `id`, `name`, `phone` |
| `Customer` | `email` |
| `Seller` | `employeeCode`, `shift` |

The hierarchy reflects this split: the shared attributes are declared once in `Person`,
and `Customer` and `Seller` extend it and declare only what belongs to their role.

The purchase history of a customer is not stored as an attribute. It is obtained from
the registered sales (`SaleService.findSalesByCustomer`), so the same information is not
kept in two places and `Customer` does not depend on `Sale`.

### 2. Should a generic "person" class exist?

Yes, but as an `abstract` class. `Person` exists so the common data and methods are
written only once, and so the rest of the system can treat customers and sellers in the
same way when it only needs what they share.

In the store nobody is "just a person": everyone is a customer or a seller. Declaring
`Person` as `abstract` makes that a compiler rule, because `new Person(...)` does not
compile. Its constructor is `protected`, so only the subclasses can use it.

`Person` also declares two `abstract` methods that each role implements in its own way:

- `getRole()` returns `"Cliente"` or `"Vendedor"`.
- `getSummary()` returns the line shown when the person is listed, with the data of its
  role.

---

## Products

### 3. Common and specific characteristics of products

| Class | Attributes |
|---|---|
| `Product` | `id`, `title`, `price`, `stock` |
| `VideoGame` | `platform`, `genre`, `ageRating` |
| `Console` | `brand`, `model`, `generation` |

Everything the store sells has an identifier, a title, a price and units in inventory,
so those attributes live in `Product`. Each type adds what makes it different.

`Product` also owns the inventory rules that apply to any product: `hasStock(quantity)`
checks whether there are enough units and `decreaseStock(quantity)` removes the units
sold.

### 4. How the description is declared in the base class

As an `abstract` method in `Product`:

```java
public abstract String getDescription();
```

Every concrete subclass must implement it, or it does not compile. `VideoGame` and
`Console` override it with `@Override`, each one including its own attributes.

The mechanism is **polymorphism** through inheritance and abstract methods. The menu
calls `getDescription()` on a `Product` reference and Java runs the version of the real
object, so no `instanceof` checks are needed.

---

## Sales and relationships

### 5. Relationships between `Sale` and the other classes

| Relationship | Type | Multiplicity | Reason |
|---|---|---|---|
| `Sale` → `Customer` | Association | many sales to 1 customer | The customer exists before and after the sale and can buy many times. |
| `Sale` → `Seller` | Association | many sales to 1 seller | Same reasoning for the seller who served the sale. |
| `Sale` → `Product` | Aggregation | 1 sale to `1..*` products | The sale references products of the catalogue; it does not create them, and they keep existing after the sale. |

There is no inheritance: a sale is not a kind of person or product.

The product list keeps **one entry per unit sold**, so two copies of the same game appear
twice. This keeps the total rule exactly as the statement defines it, the sum of the
prices of all the products acquired, without an extra "sale line" class.

### 6. Who calculates the total?

`Sale`, with `calculateTotal()`. The sale holds the product list, so it has all the
information needed to add the prices (the GRASP *Information Expert* pattern). The
constructor calls `calculateTotal()`, so a sale always has its total ready, and
`generateReceipt()` uses it without depending on any other class.

If `SaleService` calculated the total, it would have to go through the internal list of
the sale, and every other place that needed the total would have to repeat the same
loop.

---

## Business rules

### 7. A sale cannot be registered without products

The rule is checked in `SaleService.registerSale(customerId, sellerId, productIds)`,
before anything else happens:

1. If `productIds` is empty, it throws an `IllegalArgumentException` with the message
   *"La venta debe contener al menos un producto."*
2. Then it checks that the customer, the seller and every product exist, and that there
   is enough stock.
3. Only when all the checks pass does it create the `Sale`, update the inventory and
   save.

It belongs in the service because `SaleService` is the only class that registers sales,
and because the full validation needs information that `Sale` does not have, such as the
existing customers and the inventory. The menu catches the exception and shows the
message to the user. Since the checks run before any change, a rejected sale never
leaves the inventory modified.

### 8. Automatic inventory update

When a sale is registered:

1. `SaleService` counts how many units of each product are being sold and checks
   `Product.hasStock(quantity)` for each one.
2. After creating the sale, it calls `ProductService.updateStock(productId, quantity)`
   for each product.
3. `ProductService` calls `Product.decreaseStock(quantity)` and saves the inventory with
   `ProductRepository.saveAll(...)`.
4. `SaleService` saves the sale with `SaleRepository.saveAll(...)`.

Classes involved: `SaleService` coordinates; `ProductService` owns the inventory and is
the only one that changes it; `Product` applies the change to itself; `ProductRepository`
and `SaleRepository` persist the result. `SaleService` never touches `ProductRepository`
directly.

---

## Layered architecture

### 9. Which classes belong to each layer

| Layer | Package | Classes | What belongs here |
|---|---|---|---|
| Model | `com.gamezone.model` | `Person`, `Customer`, `Seller`, `Product`, `VideoGame`, `Console`, `Sale` | The business concepts, with their data and their own rules |
| Persistence | `com.gamezone.persistence` | `PersonRepository`, `ProductRepository`, `SaleRepository` | Reading and writing the data files |
| Service | `com.gamezone.service` | `PersonService`, `ProductService`, `SaleService` | Business rules and the operations that combine several objects |
| User interface | `com.gamezone.ui` | `ConsoleMenu` | Reading input and showing results |

`Main`, in `com.gamezone`, creates every object and connects the layers.

The criterion is the reason a class could change. If it changes because the business
changes, it goes to the model. If it changes because the storage format changes, it
goes to persistence. If it changes because a rule or a process changes, it goes to
services. If it changes because of how the user interacts, it goes to the UI.

### 10. Why file logic does not belong in the domain classes

Because the domain classes would end up with two reasons to change: the business and the
file format. Mixing them causes concrete problems:

- Changing the CSV format, or moving to a database, would force changes in `Customer`,
  `Product` or `Sale`.
- The domain classes would need `java.io` and exception handling unrelated to the
  business.
- The domain could not be used or tested without real files.
- The same reading and writing code would be repeated in every class.

With repositories, file handling stays in three classes and the model only describes
the business.

### 11. Allowed and forbidden dependencies

Allowed: `ui → service → persistence → model`, and `service → model`.

| Dependency | Why it is allowed |
|---|---|
| `ui` → `service` | The menu only asks the services to do things. |
| `service` → `persistence` | The services decide when to load and save. |
| `service` → `model` | The services create and use domain objects. |
| `persistence` → `model` | The repositories turn domain objects into lines and back. |

Forbidden:

- `model` → any other layer, because the model must not depend on anything.
- `ui` → `persistence`, because the menu must not skip the business rules.
- `persistence` → `service` or `ui`.
- `service` → `ui`.

The dependencies point toward the model because it is the most stable part. The business
concepts change much less than the file format or the menu. When the outer layers depend
on the stable center, a change in the menu or in the files does not reach the business
rules.
