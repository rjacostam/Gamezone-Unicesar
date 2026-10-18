# Accessory Module Analysis — GameZone Unicesar

Answers to the five guiding questions of Requirement 1. Every class and method named here
appears in `docs/accessory-class-diagram.md`.

---

## 1. Should accessories extend `Product` or form a separate hierarchy?

They extend `Product`. The new abstract class `Accessory` is a subclass of `Product`, and
`Controller`, `Cable` and `Memory` extend `Accessory`.

- **Reuse.** An accessory is sold exactly like a video game or a console: it has an `id`, a
  `title`, a `price` and a `stock`, and it uses the same inventory rules (`hasStock` and
  `decreaseStock`). By extending `Product`, none of that is written again.
- **Coherence of the model.** "An accessory is a product" is true in the business, so
  inheritance describes the real relationship.
- **Sales work unchanged.** `Sale` already keeps a `List<Product>`. Since an accessory *is*
  a `Product`, a sale can mix video games, consoles and accessories without changing `Sale`
  or `SaleRepository`.

A separate hierarchy would duplicate the four common attributes and the stock rules, and
`Sale` would need a second list for accessories.

---

## 2. Common and specific attributes

| Class | Attributes |
|---|---|
| `Product` (inherited) | `id`, `title`, `price`, `stock` |
| `Accessory` | `compatibleConsoleIds` |
| `Controller` | `connectionType` (inalámbrico or alámbrico) |
| `Cable` | `lengthInMeters`, `connectorType` (HDMI, USB, óptico...) |
| `Memory` | `capacityInGb`, `memoryType` (SD, microSD, tarjeta interna) |

The hierarchy reflects this split:

- `Accessory` adds only what every accessory shares: the list of compatible consoles.
- `Accessory` declares the abstract method `getAccessoryType()`, which each subclass
  implements (`"CONTROLLER"`, `"CABLE"`, `"MEMORY"`). It is used to filter by type and to
  write the type column of the file.
- `Accessory` overrides `getDescription()` to describe the common accessory data, and each
  subclass overrides it again, calling `super.getDescription()` and adding its own
  attributes.

---

## 3. How the compatibility with consoles is represented

**In the design:** it is an attribute of the accessory. `Accessory` keeps
`compatibleConsoleIds`, a list with the ids of the compatible consoles, and offers
`isCompatibleWith(consoleId)`, `addCompatibleConsole(consoleId)`,
`removeCompatibleConsole(consoleId)` and `getCompatibleConsoleIds()`.

It belongs to the accessory and not to the console because the accessory is the one that
declares which consoles it works with. `Console` does not change, so the products module
is not affected. To know which accessories work with a console,
`AccessoryService.findAccessoriesCompatibleWith(consoleId)` goes through the accessories
and asks each one `isCompatibleWith(consoleId)`.

The accessory stores **ids**, not `Console` objects. That keeps `Accessory` independent
from the product list and makes it easy to save.

**In the persistence:** the ids go in one column of `data/accessories.csv`, separated by
commas:

```
type;id;title;price;stock;compatibleConsoles;extra1;extra2
CONTROLLER;A-01;Control DualSense;350000;8;C-01;Inalámbrico;
CABLE;A-02;Cable HDMI 2.1;45000;15;C-01,C-02;2.0;HDMI
MEMORY;A-03;Tarjeta microSD 256 GB;120000;10;C-02;256;microSD
```

When an accessory is registered, `AccessoryService` checks that every compatible id
belongs to a console of the inventory, using `ProductService.findById`.

---

## 4. Changes needed in `SaleService`

The changes are additive. The existing flow for video games and consoles keeps working the
same way.

1. **New dependency.** The constructor also receives `AccessoryService`.
2. **Unified catalog.** A new `getCatalog()` returns the products and the accessories
   together. It is used to load the stored sales, because a sale can now reference
   accessories, and by the menu to show what can be sold.
3. **Resolving items.** For each id of the sale, `registerSale` looks first in
   `ProductService.findById` and, if it is not there, in `AccessoryService.findById`. An id
   that is in neither is rejected.
4. **Same rules for everything.** Since every item is a `Product`, the minimum of one item,
   the stock check with `hasStock` and the total computed by `Sale` work the same for
   accessories, without new code.
5. **Inventory update by type.** When the sale is confirmed, the stock of each item is
   updated through the service that owns it: `AccessoryService.updateStock` for an
   `Accessory`, `ProductService.updateStock` for the rest.

The signature of `registerSale(customerId, sellerId, productIds)` does not change, so the
menu keeps calling it the same way. Accessory ids start with `A-`, which keeps them
distinct from video games (`VG-`) and consoles (`C-`). `AccessoryService` also rejects an
id already used by a product.

---

## 5. Layer of each new class

| Class | Layer | Reason |
|---|---|---|
| `Accessory`, `Controller`, `Cable`, `Memory` | Model (`com.gamezone.model`) | Business concepts with their data and rules. They do not read files or talk to the user. |
| `AccessoryRepository` | Persistence (`com.gamezone.persistence`) | Only reads and writes `data/accessories.csv`. It depends only on the model. |
| `AccessoryService` | Service (`com.gamezone.service`) | Registration rules, queries by type and compatibility, and stock updates. It is the only class that calls `AccessoryRepository`. |

The changed classes keep their layer:

- `SaleService` stays in services and uses `AccessoryService`, never the repository.
- `ConsoleMenu` stays in the UI and adds the accessories submenu, calling only services.
- `Main` creates `AccessoryRepository` and `AccessoryService` and injects them.

The dependency rule `ui → service → persistence → model` is still respected.
