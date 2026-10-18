# Exception and Validation Analysis — GameZone Unicesar

Answers to the five guiding questions of Requirement 6. Every class and method named here
appears in `docs/exception-class-diagram.md`.

## Scope

The statement names the three modules of the workshop (products, people and sales). It
also asks for "one validator per functional module". After Requirement 5 the system has
**seven** modules: products, people, sales, accessories, promotions, warranties and
returns. The new layer therefore covers all seven:

- seven validators;
- seven services;
- the seven repositories.

The reason is practical. If only three services were migrated, the other four would keep
throwing `IllegalArgumentException`. Once the menu stops catching that generic exception,
those errors would reach the user as a raw stack trace, which the requirement forbids.

---

## 1. Checked or unchecked root exception

`GameZoneException` extends **`RuntimeException`** (unchecked).

- **Signatures stay clean.** Services and repositories do not declare `throws` for
  business errors, so a method like `registerSale` does not carry four `throws` clauses
  that every caller must repeat.
- **Errors travel to the right place.** An error found in a repository or a validator can
  travel through the service up to `ConsoleMenu` without each layer catching and
  re-throwing it. Only the menu, which talks to the user, decides what message to show.
- **They are not recoverable in the middle layers.** A missing product, a broken business
  rule or a damaged file cannot be fixed by the service that finds it. Checked exceptions
  are meant for errors the caller can handle on the spot.
- **It removes a real problem we already had.** `PersonService` declared
  `throws IOException`, so the menu and `Main` had to catch an `IOException` from the
  persistence layer. With an unchecked `PersistenceException`, those `throws` clauses
  disappear.

The consequence for the signatures: the repositories stop declaring `throws IOException`,
because they catch it and throw `PersistenceException`. No service declares checked
exceptions. The JavaDoc of each public method still documents, with `@throws`, which
`GameZoneException` subclasses it can throw.

---

## 2. Why a custom hierarchy instead of `IllegalArgumentException`

With one generic exception, the menu cannot tell the user whether they typed something
wrong, whether a rule of the store was broken, or whether the system has a technical
problem. All three looked the same. The hierarchy gives each kind of error its own type:

| Exception | When | Message to the user |
|---|---|---|
| `ResourceNotFoundException` | A product, customer, seller, sale or other resource does not exist | "No se encontró el recurso solicitado: …" |
| `BusinessRuleException` | A rule is broken: not enough stock, duplicate id, more than 30 days… | "No se puede completar la operación: …" |
| `InvalidDataException` | A value has a wrong format or range: empty text, price not greater than zero… | "Los datos ingresados no son válidos: …" |
| `PersistenceException` | A data file cannot be read or written | "Ocurrió un problema al acceder a los datos del sistema. Contacte al administrador." |

Design principles applied:

- **Single responsibility.** Each exception type represents one category of error, and the
  validators hold the checks that used to be mixed with the business logic of the
  services.
- **Open/closed and polymorphism.** A new kind of error is a new subclass. Code that
  catches `GameZoneException` keeps working, and the menu only adds a catch when it needs
  a new message.
- **A common root.** `GameZoneException` lets the system treat every error of its own as
  one family, and it carries an `errorCode` that identifies the category.
- **Information hiding.** The persistence layer turns `IOException` into
  `PersistenceException`. The upper layers never depend on how files are handled, and the
  original cause is kept for traceability.

---

## 3. Where the validators are called

The validators live in `com.gamezone.validation`. They are classes with **static methods
only**, and their constructor is private so they cannot be instantiated.

They are called **from the services, at the start of each operation, before anything
changes**. For example, `SaleService.registerSale` validates the customer, the seller, the
items and the stock before creating the sale, so a rejected sale never leaves a
half-updated inventory.

This placement fits the layered architecture:

- The services decide **when** a rule applies. The validators only decide **whether** it
  holds.
- Validators receive the data or the domain objects they need as parameters. Their
  dependencies are `model` and `exceptions`, which keeps them "between service and
  model", as the statement asks.
- `exceptions` is a cross-cutting package: any layer may use it.

**The validators cannot access the persistence package**, and they do not call services
either. If a check needs data that is stored, for example whether an id is already used,
the service looks it up and passes the result to the validator. For instance:

```java
ProductValidator.validateUniqueProduct(findById(id), id);
```

This keeps the validators simple, easy to test and free of file access, as the
restrictions require.

---

## 4. How we make sure nothing breaks

The refactor changes **which exception** is thrown, not **when** an error happens.

1. **Same conditions.** Every `throw new IllegalArgumentException(...)` in the services is
   replaced by a call to a validator that checks the same condition. The validators were
   designed from an inventory of the existing checks, one method per rule.
2. **Same messages, now classified.** The user still sees the same explanation, now
   preceded by the category of the error.
3. **Compile after every commit.** Each commit is compiled before it is pushed. This
   matters most for José, who does not have a JDK installed: the leader compiles and runs
   every branch before approving it.
4. **Before and after comparison.** The same script of inputs is run on `develop` before
   the change and on the branch after it. It covers:
   - the ten operations of the workshop;
   - the accessory, promotion, warranty and return menus;
   - the full R5 demo (total $3,040,000, refund $2,815,000, net balance $225,000);
   - one error case of each type (missing customer, insufficient stock, empty title,
     damaged data file).

   The data files must be identical, and only the error messages may change.
5. **Evidence.** The Pull Request of each member describes the cases run. The final
   comparison is recorded in the README of the requirement.

The finder methods that return `null` (`findById`, `findCustomerById`...) keep doing so, so
that menus and other services do not change behaviour. The service throws
`ResourceNotFoundException` where a missing resource is an error.

---

## 5. How `ConsoleMenu` catches the four types

**One `try` with several specific `catch` blocks**, ordered from the most specific to the
most general, and written **once** in a helper method that every submenu uses:

```java
private void runSafely(Runnable action) {
    try {
        action.run();
    } catch (ResourceNotFoundException e) {
        System.out.println("No se encontró el recurso solicitado: " + e.getMessage());
    } catch (BusinessRuleException e) {
        System.out.println("No se puede completar la operación: " + e.getMessage());
    } catch (InvalidDataException e) {
        System.out.println("Los datos ingresados no son válidos: " + e.getMessage());
    } catch (PersistenceException e) {
        System.out.println("Ocurrió un problema al acceder a los datos del sistema. Contacte al administrador.");
    }
}
```

Why this option:

- **Separate catch blocks** give a different message for each type, which is the goal of
  the requirement. A single `catch (GameZoneException e)` would need `instanceof` checks
  inside it to choose the message. That is less clear and goes against using the
  hierarchy.
- **One helper instead of a cascade in every submenu.** There are seven submenus. Writing
  the four catches in each one would repeat 28 blocks. `runSafely` keeps the handling in
  one place, so a change to a message is made once.
- **The persistence message hides technical details.** It does not print the file path or
  the stack trace. The detail stays in the exception (`getCause()`) for the developers.

`Main` also catches `PersistenceException` when the services load their files at startup.
If a file cannot be read, the program shows a clear message instead of starting with
incomplete data.
