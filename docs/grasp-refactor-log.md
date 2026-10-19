# GRASP Refactor Log — GameZone Unicesar

Requirement 7, Phase 2. This file records the no regression strategy, the reference run on
`develop`, every correction of the consolidated audit (`docs/grasp-audit/consolidated-audit.md`)
and the final verification.

## No regression strategy

The correction is a refactor: the same inputs must give the same screen output and the same
data files. The team checks it with two fixed input scripts that drive the console menu:

- `docs/regression/menu-script.txt`: one run that goes through **every operation of the seven
  submenus**, including one error of each kind.
- `docs/regression/restart-script.txt`: a second run on the data left by the first one, to show
  that every change was saved and is loaded again.

### How to run it

From the root of the project, on a clean copy of the preloaded data:

```bash
mvn clean compile
mkdir -p run && cp -r data run/
cd run
java -cp ../target/classes com.gamezone.Main < ../docs/regression/menu-script.txt > out-run1.txt
java -cp ../target/classes com.gamezone.Main < ../docs/regression/restart-script.txt > out-run2.txt
```

The same steps are run on `develop` (reference) and on the branch with the correction. The
result is accepted only if:

1. `out-run1.txt` and `out-run2.txt` are **identical** in both runs;
2. the eight files of `run/data/` are **identical** in both runs.

Sales, returns and warranties use the date of the day, so the reference and the check are
always run on the same day. The `run/` folder is only a working copy and is deleted after the
check; it is never committed.

### Operations covered

| Submenu | Operation | Input of the script | Expected result |
|---|---|---|---|
| Products | Register video game | `VG-90`, price 150000, stock 5 | "Videojuego registrado" |
| Products | Register console | `C-90`, price 1800000, stock 3 | "Consola registrada" |
| Products | Duplicate id | `VG-90` again | Business rule message |
| Products | Invalid price | `VG-91`, price 0 | Invalid data message |
| Products | List inventory | — | Catalogue with `VG-90` and `C-90` |
| People | Register customer | `123`, Ana Prueba | "Cliente registrado" |
| People | Duplicate id | `123` again | Business rule message |
| People | Invalid phone | `30011a` | Invalid data message |
| People | Invalid e-mail | `caromail` | Invalid data message |
| People | List customers and sellers | — | Ana Prueba; the three sellers |
| Accessories | Register controller, cable and memory | `A-90`, `A-91`, `A-92` | One "registrado/registrada" message each |
| Accessories | Compatible with a non console | `A-93` with `VG-01` | Business rule message |
| Accessories | List all, by each of the three types, compatible with `C-01` and with `C-99` | — | Lists; empty list for `C-99` |
| Promotions | Register percentage, category and bulk | `P-90`, `P-91`, `P-92` (expired in 2020) | One "Promoción registrada" each |
| Promotions | Duplicate id | `P-01` | Business rule message |
| Promotions | Invalid percentage | 150 | Invalid data message |
| Promotions | List all and active | — | Seven promotions; the four active ones |
| Sales | Register sale with extended warranty | `123`, `EMP-001`, `C-01`, `A-01`, `VG-01`, warranty "s" | Sale `S0001`: subtotal $3,100,000, discount "Lleva 3 o más" −$310,000, warranties $250,000, total **$3,040,000** |
| Sales | Register second sale | `123`, `EMP-002`, 2 × `VG-02` | Sale `S0002`: discount "Videojuegos al 20%" −$128,000, total **$512,000** |
| Sales | Missing customer, missing seller, missing product | `999`, `EMP-999`, `ZZ-99` | Resource not found message each |
| Sales | Not enough stock | 50 × `VG-02` | Business rule message |
| Sales | Sale without products | — | Business rule message |
| Sales | History, by customer, by seller, detail, missing detail | `123`, `EMP-001`, `S0001`, `S9999` | Receipts of `S0001` and `S0002`; "No existe una venta" for `S9999` |
| Warranties | Warranty of a product | `S0001`/`C-01`, `S0001`/`VG-01` | Certificate; "no tiene garantía" |
| Warranties | List all, active, expiring in 30 days, expiring in 0 days | — | Lists; invalid data message for 0 |
| Returns | Register return of a console | `S0001`, `C-01`, "No funciona" | Return `R0001` with warranty refund $250,000 |
| Returns | Too many units | `S0002`, 5 × `VG-02` | Business rule message |
| Returns | Missing sale | `S9999` | "No existe una venta" |
| Returns | Unit already returned | `S0001`, `C-01` again | Business rule message |
| Returns | All, by customer, by sale | `123`, `S0001` | `R0001` |
| Returns | Monthly balance and invalid month | October 2026; month 13 | Sales $3,552,000, returns $2,500,000, net **$1,052,000**; "El mes debe estar entre 1 y 12" |
| Restart | Products, customers, accessories, promotions, sales, warranties, returns, balance | `restart-script.txt` | The same data as at the end of the first run |

## Reference run on `develop`

| Field | Value |
|---|---|
| Commit | `develop` at `ae12127` (merge of the Phase 1 pull request #35) |
| Date | 2026-10-18 |
| Compilation | No errors |
| First run | 1,403 lines of output, 111 menu prompts answered, no stack trace |
| Second run | 211 lines of output, same totals as the first run |
| Repeatability | Two runs on the same day gave identical outputs and identical data files |
| `data/sales.csv` | `S0001` total $3,040,000 and `S0002` total $512,000 |
| `data/returns.csv` | `R0001` for `C-01` of `S0001`, warranty refund $250,000 |
| `data/warranties.csv` | Empty after the return: the two warranties of `C-01` were cancelled |

## Correction log

One row per change. Every violation of the consolidated audit must appear at least once.

| Class | Change made | Violation ID | Pattern applied | Commit / PR | Responsible |
|---|---|---|---|---|---|
| `Sale` | Added `countUnitsOf(String productId)` and `findProductById(String productId)`, so the sale answers how many units of a product it has and which unit carries an identifier. Both compare ignoring case; `ReturnRepository.findProduct` compares with `equals`, but the stored ids are always written from `Product.getId()`, so the result is the same. No caller changed yet: Developer 2 moves `ReturnService` and `ReturnRepository` to these methods. No regression: both scripts identical. | V-03 | Information Expert | `b0fd770` | Technical Leader |
| `Product` | Added `increaseStock(int quantity)`, the opposite of `decreaseStock(int)`: it rejects zero and negative quantities with the same rule `ProductService.restoreStock` applies today, and adds the units to the stock the product owns. Purely additive, no existing caller changed, so nothing the user sees changes yet. No caller changed yet: the Technical Leader makes `restoreStock` call it. No regression: both scripts identical. | V-01 | Information Expert | `85481b1` | Developer 1 |
| `Product`, `Console` | Added `Product.isConsole()`, which answers false, and `Console.isConsole()`, which overrides it and answers true. Checked that the new method agrees with the text comparison it replaces on both products, so the rule "only consoles" gets the same answer with or without the change. Purely additive: `getCategory()` is untouched, so no comparison the user sees changed yet. No caller changed yet: Developer 1 moves `SaleValidator` and `AccessoryValidator`, and the others move `ConsoleMenu` and `SaleService`. No regression: both scripts identical. | V-04 | Polymorphism | `4d10125` | Developer 1 |
| `Promotion`, `PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount`, `Warranty`, `BasicWarranty`, `ExtendedWarranty` | Added `getTypeCode()` as an abstract method of `Promotion` and of `Warranty`, answered by each subclass with the exact text the repositories write today: `PERCENTAGE`, `CATEGORY`, `BULK`, `BASIC` and `EXTENDED`. Checked the five against the current repository logic and all five match, so no stored file changes. `Product` and `Accessory` already publish theirs (`getCategory()` and `getAccessoryType()`), so the four hierarchies are now complete. Purely additive: `getWarrantyType()`, the name the user reads, is untouched. No caller changed yet: Developer 1 corrects `ProductRepository` and Developer 2 the other three repositories. No regression: both scripts identical. | V-08 | Polymorphism | `1d1724d` | Developer 1 |
| `InventoryCatalog` | New interface in `com.gamezone.service` with `findById(String id)`, `updateStock(String id, int quantity)` and `restoreStock(String id, int quantity)`, so a service can ask a catalogue to change its own stock instead of testing the concrete type of the item. `findById` returns `Product`, which both catalogues satisfy covariantly because `Accessory extends Product`. No existing caller changed and no body was modified, so nothing the user sees changes. No regression: compiled and both scripts identical, checked by the Technical Leader. | V-07 | Polymorphism, Low Coupling | `7ce95e4` | Developer 2 |
| `AccessoryService` | Now `implements InventoryCatalog`, with `@Override` on `findById`, `updateStock` and `restoreStock`. `findById` keeps returning `Accessory`, which is a covariant return of the `Product` declared by the interface, so no import or signature change was needed. No line of logic was touched: the bodies, the validators and the exceptions are the same. `ReturnService` was not changed, it moves to the list of catalogues in turn 2. No regression: compiled and both scripts identical, checked by the Technical Leader. | V-07 | Polymorphism, Low Coupling | `5e9442e` | Developer 2 |
| `ProductService` | `restoreStock` now calls `product.increaseStock(quantity)` instead of `setStock(getStock() + quantity)`. The quantity is still checked first by `ProductValidator.validateQuantity`, so the message on a wrong quantity is the same. No regression: both scripts identical. | V-01 | Information Expert | `f376cf7` | Technical Leader |
| `SaleService` | `assignWarranties` asks `product.isConsole()` instead of `product instanceof Console`, and the unused import of `Console` was removed. No regression: both scripts identical. | V-04 | Polymorphism, Controller | `5fcc236` | Technical Leader |
| `ConsoleMenu` | `registerSale` asks `product != null && product.isConsole()` before offering the extended warranty, instead of `product instanceof Console`. The `null` check keeps the old behaviour for accessories, which `ProductService.findById` does not find. No regression: both scripts identical. | V-04 | Polymorphism, Controller | `eb440e0` | Technical Leader |

## Final verification

To be filled in at the end of Phase 2, with the same two scripts on the final `develop`.
