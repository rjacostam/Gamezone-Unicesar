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

## Final verification

To be filled in at the end of Phase 2, with the same two scripts on the final `develop`.
