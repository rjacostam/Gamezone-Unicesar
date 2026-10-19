# GRASP Audit — Analysis

> Requerimiento 7, Fase 1. This document answers the guiding question about how the
> corrections of the audit will be made without changing what the user sees.

## Answers by member

| Question | Answer |
|---|---|
| 1 | Assigned in each individual report: `docs/grasp-audit/leader-audit.md`, `developer1-audit.md`, `developer2-audit.md` |
| 2 | Assigned in each individual report |
| 3 | Assigned in each individual report |
| 4 | This document |
| 5 | Answered together once the consolidated audit is merged, in `docs/grasp-analysis.md` |

---

## Question 4 — Preserving the external behaviour of the system

The audit found five violations in the code of Developer 2 and five more problems that
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

### Order of the corrections

The order is decided once the consolidated audit exists, because the order depends on which
violations are assigned to each member. The criterion the team proposes is: correct the
violations that other members depend on first, then the ones that remove duplicated logic,
then the ones that only move code. The two violations that touch code of another member
(V-01 and V-02, which would add methods to `Sale`) need the agreement of the Technical
Leader before Phase 2 starts, and they are the ones most likely to need a shared branch.