# GRASP Audit — Analysis

> Requerimiento 7, Fase 1. This document answers the guiding question about how the
> corrections of the audit will be made without changing what the user sees.

## Answers by member

| Question | Who answers | Where |
|---|---|---|
| 1 | Developer 1 (Andrés) | `docs/grasp-analysis.md` |
| 2 | Technical Leader (Rafael) | `docs/grasp-analysis.md` |
| 3 | Developer 1 (Andrés) | `docs/grasp-analysis.md` |
| 4 | Developer 2 (José) | This document, below |
| 5 | Technical Leader (Rafael) | `docs/grasp-analysis.md` |

---

## Question 2 — Boundary findings

*Answered by the Technical Leader.*

The team identified six violations on the boundary between the code of several members:
V-01, V-03, V-04, V-05, V-07 and V-08. During the consolidation each of them was registered
only once, even when it appeared in two or three individual reports. The correction was split
by the classes that belong to each member, so that each person changes only their own part of
the code.

A concrete case is V-04, the rule that only consoles can have an extended warranty. It
appeared in my report, in Andrés's report and in José's report, but in the consolidation it
was registered as a single finding. Andrés is in charge of adding the behaviour to `Product`
and correcting his validators; I am in charge of correcting `ConsoleMenu` and `SaleService`;
and José is in charge of adjusting `ReturnService`. This way the responsibility is shared
without duplicating the violation.

---

## Question 4 — Preserving the external behaviour of the system

The audit found four violations in the code of Developer 2 and five more problems that
belong to the Technical Leader or to Developer 1. None of them is a change of behaviour,
but a refactor can break the system without ever failing to compile, so the team agreed on
a strategy before starting the correction phase.

### Strategy

**1. The ten operations of the console menu are the contract.**

The statement defines ten operations and the menu is the only way the user reaches them.
Those ten operations, and the receipts and reports they print, are what has to stay the
same. Everything under them is free to change: the way a validation is computed, the class
that holds a list, the exception type a rule throws.

**2. The data files are the golden master.**

The store keeps everything in `data/*.csv`. That makes the state of the system comparable
without any extra instrumentation: the same script is run before and after a correction, and
the resulting files are compared. This was already used for Requirement 6, and it is what
makes the claim of "nothing broke" verifiable instead of a matter of opinion.

**3. Corrections are additive wherever possible.**

Every proposed correction adds a method or changes where a method lives. No correction
deletes a menu option, changes a signature used by `ConsoleMenu`, renames a file column
that is already stored, or alters a receipt line. A correction that would have to change
one of those is out of scope of Phase 2 and has to be proposed as a new requirement.

**4. Each correction is a separate commit on its own branch.**

The Phase 2 flow already asks for one activity per violation, so each correction can be
reviewed, merged or reverted on its own. A correction that turns out to be wrong does not
take the whole audit with it.

### Evidence produced during the audit of Requirement 6

The strongest evidence available today comes from the previous requirement, because it
followed exactly this strategy and the Technical Leader verified it.

| Check | Result |
|---|---|
| `mvn clean compile` on the branch | Compiles, after the four corrections the Technical Leader found |
| Same test script run on `develop` and on the branch | The resulting files in `data/` are **identical**; only the wording of the error messages changed |
| The four error paths of the menu | Each one prints its own message and **no stack trace** reaches the user |
| A failed write while registering a customer | The customer is no longer left half-registered, which is a deliberate improvement and not a regression |

The second row is the one that matters: the observable state of the system after the same
script is byte for byte the same, so the refactor really was invisible from outside.

### What this strategy does not cover

Stating it plainly is part of the answer, because a guarantee that has a hole in it is not
a guarantee.

- **Compilation was verified on one machine only.** No automated build runs on every
  commit, so a correction is only as safe as the review that reads it.
- **The comparison is of the data files, not of the screen output.** A correction that
  printed a different receipt line while leaving the files identical would pass this check.
  For that reason the rule 3 above forbids touching the receipts.
- **There is no automated test suite.** The script is manual and is run by a person, so it
  depends on that person repeating it after every correction.

### How the evidence will be shown at the end

1. The script is run on `develop` before the corrections start, and the seven files in `data/`
   are kept as the reference.
2. After each correction is merged, the same script is run again and the files are compared
   against the reference.
3. The final run, after every correction is merged, is repeated from a clean `data/` folder
   to show that the system also starts empty.
4. The ten menu operations are executed once in front of the room during the oral defence.