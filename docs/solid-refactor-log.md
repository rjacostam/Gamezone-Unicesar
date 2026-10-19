# SOLID Refactor Log — GameZone Unicesar

Requirement 8, Phase 2. This file records the no regression strategy, the reference run on
`develop`, every correction of the consolidated audit (`docs/solid-audit/consolidated-audit.md`)
and the final verification.

## No regression strategy

The correction is a refactor: the same inputs must give the same screen output and the same
data files. The team reuses the two input scripts created in Requirement 7, which drive the
console menu:

- `docs/regression/menu-script.txt`: one run through **every operation of the seven submenus**,
  with one error of each kind.
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

A run is accepted only if `out-run1.txt`, `out-run2.txt` and the eight files of `run/data/` are
**identical** to the reference run. Sales, returns and warranties use the date of the day, so
the reference and every check are run on the same day; if the day changes, the reference is run
again on `develop` before comparing. The `run/` folder is a working copy and is never committed.

The check is run on `develop` before the first correction (reference), after every correction
commit on the operations it touches (in practice the whole script, because it takes seconds),
in every pull request of the phase, and at the end of the phase on the final `develop`.

### Operations covered

| Submenu | Operations of the script | Expected result |
|---|---|---|
| Products | Register video game `VG-90` and console `C-90`, duplicate id, price 0, list inventory | Two registrations, a business rule message, an invalid data message, the catalogue |
| People | Register customer `123`, duplicate id, phone `30011a`, e-mail `caromail`, list customers and sellers | One registration, a business rule message, two invalid data messages, the lists |
| Accessories | Register `A-90`, `A-91`, `A-92`, compatible with `VG-01`, list all, by each type, compatible with `C-01` and `C-99` | Three registrations, a business rule message, the lists |
| Promotions | Register `P-90`, `P-91`, `P-92`, duplicate `P-01`, percentage 150, list all and active | Three registrations, a business rule and an invalid data message, seven and four promotions |
| Sales | Sale `S0001` (`C-01`, `A-01`, `VG-01`, extended warranty), sale `S0002` (2 × `VG-02`), missing customer, seller and product, not enough stock, empty sale, history, by customer, by seller, detail, missing detail | `S0001` total **$3,040,000**, `S0002` total **$512,000**, three resource messages, two business rule messages, the receipts |
| Warranties | Warranty of `C-01` and `VG-01` in `S0001`, list all, active, expiring in 30 and in 0 days | A certificate, "no tiene garantía", the lists, an invalid data message |
| Returns | Return `C-01` of `S0001`, too many units, missing sale, unit already returned, list all, by customer, by sale, balance of October 2026 and of month 13 | `R0001` with warranty refund $250,000, two business rule messages, net balance **$1,052,000**, "El mes debe estar entre 1 y 12" |
| Restart | Every list and the October balance | The same data as at the end of the first run |

## Reference run on `develop`

| Field | Value |
|---|---|
| Commit | `develop` at `94cfe02` (merge of the Phase 1 pull request #42) |
| Date | 2026-10-19 |
| Compilation | No errors |
| First run | 1,403 lines of output, no stack trace |
| Second run | 211 lines of output, same totals as the first run |
| Repeatability | Two runs on the same day gave identical outputs and identical data files |
| Key figures | `S0001` total $3,040,000; `S0002` total $512,000; return `R0001` with warranty refund $250,000; October balance: sales $3,552,000, returns $2,500,000, net $1,052,000 |

Compared with the reference of Requirement 7 (run on 2026-10-18), the only difference is the
date printed on the sales; every amount, message and data file is the same.

## Correction log

One row per change. Every violation of the consolidated audit must appear at least once.

| Class | Change made | Violation ID | Principle applied | Commit / PR | Responsible |
|---|---|---|---|---|---|
| `Submenu` | `runSafely` keeps its four catch blocks and their messages, and adds a final `catch (GameZoneException e)` with a general message. A new kind of error, added as a subclass of `GameZoneException`, is now shown to the user without editing `Submenu`. The new block is not reached by any current operation. No regression: both scripts identical. | V-04 | Open/Closed | `18411b3` | Technical Leader |
| `ConsoleMenu`, `Main` | `ConsoleMenu` no longer creates its collaborators: it receives the `ConsoleInput` and the seven submenus through its constructor and keeps them as `Submenu`. `Main`, the composition root, builds them. The order of the options and every message are the same. No regression: both scripts identical. | V-13 | Dependency Inversion | `875f724` | Technical Leader |
| `ProductService` | `findById` now follows the same contract as `AccessoryService.findById`: a null or blank identifier answers `null` without walking the catalogue, and surrounding blanks are removed before comparing. Both implementations of `InventoryCatalog` now answer the same question the same way, which is what Developer 2 needs before splitting the interface with a single contract. Checked the eight cases that matter: `VG-01`, `vg-01`, ` VG-01 ` and `VG-01 ` all answer `VG-01`, while `  `, the empty text, `ZZ-99`, `VG-02` and `null` all answer `null`. Nothing the user sees changes: `ConsoleInput.readLine` trims every line (line 137) and `AccessoryService` trims again before calling (line 309), so no caller ever sent an untrimmed identifier. No regression: both scripts identical. | V-07 | Liskov Substitution | `b475d18` | Developer 1 |
| `Console` | Added the public constant `Console.CATEGORY`, which holds `CONSOLE`. `getCategory()` now returns it, and `isConsole()` answers `CATEGORY.equals(getCategory())` instead of returning `true` by hand. The two answers to "is this a console" are now one: the predicate reads the category the product actually reports, so they cannot disagree and a change to the category moves both at once. Checked that the console still reports `CONSOLE` and answers true, that the video game still reports `VIDEOGAME` and answers false, and that the predicate agrees with the text comparison it replaces. Not changed, because they answer a different question and belong to other corrections: `ProductRepository` line 108 compares the marker it read from the file, `PromotionMenu` line 71 maps a menu option, and `PromotionValidator` line 22 holds its own constant for the category a promotion targets. `PromotionValidator` is ours and repeats the literal, but a promotion target is not the same question as the console predicate, so it was left for the correction of the promotions rather than smuggled in here. No regression: both scripts identical. | V-05 | Open/Closed | `a2e55a1` | Developer 1 |
| `AmountFormat`, `Accessory`, `Console`, `VideoGame`, `PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount`, `ReturnReceiptFormatter`, `WarrantyCertificateFormatter` | New `AmountFormat` in the new `com.gamezone.format` package, which the model can use without depending on `com.gamezone.ui`. It offers `money`, `negativeMoney`, `percent`, `moneyInComputerLocale` and `percentInComputerLocale`, so a call site can ask for the pinned `Locale.US` format or for the format of the computer. Twelve call sites of ours moved to it: ten that pinned `Locale.US` kept that variant, and the two descriptions of `VideoGame` and `Console` kept the locale of the computer, because moving them to `Locale.US` would change what the user reads and that difference is registered as OOS-02. The eleven `String.format` literals are gone from our code and the six unused `import java.util.Locale` were removed. `ReceiptGenerator`, with its five computer-locale sites, is the Technical Leader's and will move to the same component. Verified that nothing the user sees changes: the description of a console, of a video game and of an accessory, the description of the three promotions, the return receipt and both certificates were captured before and after the change on a machine whose locale is `es_CO`, and both captures are 1564 bytes with the same SHA-256. | V-02 | Single Responsibility | `5b4c12b` | Developer 1 |
| `ProductCatalogRow`, `AccessoryCatalogRow`, `PromotionCatalogRow`, `Product`, `Accessory`, `Promotion`, `VideoGame`, `Console`, `ProductRepository` | The single `getExtraFields()` contract that `Product` declared for two different files is gone, replaced by three narrow contracts, one per file: `ProductCatalogRow` for `products.csv`, answered by `VideoGame` and `Console`; `AccessoryCatalogRow` for `accessories.csv`, declared by `Accessory`; and `PromotionCatalogRow` for `promotions.csv`, declared by `Promotion`. `Product` no longer declares the method, so the accessories are no longer forced to answer a question about a file they are not stored in, and a caller can no longer read the columns of one file through a `Product`. `ProductRepository` reads them through `ProductCatalogRow`, with a documented cast that is safe because the catalogue only holds video games and consoles. `AccessoryRepository` and `PromotionRepository` were **not** touched and keep compiling and working unchanged, because `Accessory` and `Promotion` now declare the contracts those two repositories already use. **For Developer 2:** nothing to change. When the interfaces are split further in V-09, `AccessoryRepository` keeps reading `getExtraFields()` from a type of the accessory hierarchy and `PromotionRepository` from a type of the promotion hierarchy, so neither call site needs to change; if you prefer to name the type of the variable as the contract, `AccessoryCatalogRow` and `PromotionCatalogRow` are the types to use. Verified that no stored row changes: the three data files were read and written again with the code of `HEAD` and with the new code, and `products.csv` (311 bytes), `accessories.csv` (246 bytes) and `promotions.csv` (312 bytes) come out byte for byte identical, with the same registered products, accessories and promotions. | V-08 | Interface Segregation | `f298a15` | Developer 1 |

## Final verification

To be filled in at the end of Phase 2, with the same two scripts on the final `develop`.
