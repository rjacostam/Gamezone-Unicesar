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

## Question 1 — Criterion for a verdict

*Answered by Developer 1.*

### The criterion the three verdicts had to pass

The three verdicts were not three degrees of severity. They are three different questions, and
the rule to choose between them is this one:

> **A change the store could reasonably need to make tomorrow: how many classes would I have to
> open, and is one of them the class that already owns the information? If the answer is more
> than one, it is a violation of the pattern that names the missing owner. If the answer is
> one and it is the owner, it is a correct application. If the class never takes part in that
> decision, it is not applicable.**

- **Violation.** The pattern exists, the class takes part in the decision the pattern is about,
  and the responsibility sits somewhere other than where the pattern says it should sit. It is
  not "the class could be cleaner": it is that the change the pattern protects against would
  now have to be written in more than one place. GRASP patterns are not about elegance, they
  are predictions about the cost of future changes, so a violation only exists if it predicts
  a cost.
- **Correct.** The pattern is **present and used well**, never merely absent. A class that does
  not participate in the decision the pattern governs cannot be correct: saying "Creator is
  correct" because no class creates anything would make the verdict meaningless. A correct
  verdict needs a place, a class and a method where the pattern is doing its job, and a reason
  why it is well applied there.
- **Not applicable.** A precondition of the pattern is not met by the class. This verdict is not
  a compliment and it is not a "nothing was found": it means the concept the pattern describes
  does not exist in that code, so there is nothing to judge. It has to be justified in writing,
  because the difference between *not applicable* and *correct* is the difference between "this
  class does not do this" and "this class does this and does it well".

### The minimum evidence each verdict had to carry

| Verdict | Minimum evidence | Why that evidence |
|---|---|---|
| Violation | Class, method, exact line numbers, the literal expression that decides, why the responsibility is in the wrong place, the consequence for a concrete change, a proposed solution, and whether it touches another member's code | A violation without lines is an opinion. The literal expression is included so the reader can verify the finding without opening the file, and so two members who see the same lines register the same defect instead of two defects. |
| Correct | Class and method, with the line, and the reason the pattern is applied well there | It is what separates *correct* from *absent*, and it is what lets the consolidation check that the same pattern was not called correct in one report and violated in another for the same class. |
| Not applicable | The precondition that is not met, written in one sentence, and a pointer to where the pattern does apply in the system if it applies somewhere | It turns "I did not find anything" into a checkable statement. Without the pointer, *not applicable* is indistinguishable from an audit that was not done. |

### Three rules we applied to keep the verdicts honest

1. **No finding without a nameable change.** If I could not describe the modification of the
   store that the defect would make expensive, it was not a violation. This is why my report
   has five findings and not thirty: most code is not wrong, it is simply not connected to a
   foreseeable change.
2. **The evidence was read, not remembered.** Every line number was taken from the file after
   `git pull`, on the audited commit `6c10ce2`, and was verified against the source before the
   finding was written. When the Technical Leader found that one of my line ranges pointed at a
   filtered loop instead of a full one, the finding was corrected instead of defended, because
   the criterion is the code and not the report.
3. **A mandated design is still audited.** Some classes had the shape the class diagrams of the
   requirements asked for. That does not exempt them: if a mandated shape produces a defect with
   a real cost, it is reported and the conflict with the diagram is stated. That is how my
   finding of High Cohesion (my V-03, consolidated as V-05) reached classes whose text was
   written on purpose.

### What the criterion produced

Thirteen violations were reported by the three reports and nine survived the consolidation. The
three verdicts were not decoration: `Creator`, `Pure Fabrication` and `Indirection` are correct
for the whole team because a place was found where each one is applied, and `Controller` is
**not applicable** in my report but **violation** in the team verdict, because the same pattern
is absent in my classes and present in `ConsoleMenu`. A criterion that cannot produce different
verdicts for different members would not be a criterion.

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

## Question 3 — Code that breaks more than one pattern

*Answered by Developer 1.*

In this answer `dev1 V-nn` is the identifier in my own report,
`docs/grasp-audit/developer1-audit.md`, and `V-nn` alone is the identifier of the same defect
in `docs/grasp-audit/consolidated-audit.md`. The consolidated audit renumbered everything, so
both names are given where they differ.

### Yes, and four of them do

The audit did find fragments that break more than one pattern at the same time. They were not
rare and they were not a coincidence: the patterns of GRASP describe the same decisions seen
from different sides, so a piece of code that gets one decision wrong usually gets the
neighbouring decisions wrong too.

| Defect | Patterns it breaks | Reported as | Consolidated as |
|---|---|---|---|
| `ProductRepository` decides by `instanceof` and derives the type marker itself | Polymorphism, Low Coupling, Protected Variations | dev1 V-02, also raised by Developer 2 | V-08 |
| `AccessoryRepository`, `PromotionRepository` and `WarrantyRepository` do the same with the other three hierarchies | Polymorphism, Low Coupling, Protected Variations | dev2 V-04, dev2 R-05 | V-08 |
| The services decide who owns the stock of an item by checking the type of the object | Polymorphism, Low Coupling | dev2 V-02 | V-07 |
| The rule "only consoles can have an extended warranty" is decided by comparing the category as text in five classes | Controller, Protected Variations | dev1 V-05 and the Technical Leader's V-02 | V-04 |
| The model classes build the Spanish text of the receipts and the certificates | High Cohesion, and a responsibility that belongs to the presentation layer | dev1 V-03 and the Technical Leader's V-03 | V-05 |

### How they were registered without duplicating them

The rule was one line: **one defect, one identifier.** The other three mechanisms exist only to
make that one line usable.

1. **The defect is registered once, under the pattern that guides its correction.** The other
   patterns it breaks are not dropped, they are named inside the same finding, in its
   Explanation. `V-08` says that the repositories "restate the class hierarchy as a chain of
   tests" and, in the same cell, that "it also breaks Low Coupling and Protected Variations,
   because the persistence layer depends on every concrete type". One entry, three patterns.
2. **The summary table lists the identifier in every pattern it breaks**, so a reader who starts
   from any pattern finds the defect without reading the other reports. That is why `V-08`
   appears in the Low Coupling row and in the Polymorphism row of the team verdict, and `V-04`
   appears in the Controller row and in the Protected Variations row.
3. **Each finding records its origin.** The `Origin` cell of `V-08` reads
   `dev1 V-02, dev2 V-04, dev2 R-05`, which is how the three reports were merged without
   discarding the work or inventing a new finding. Thirteen violations were reported; the nine
   that remain are those nine.

### How we decided which pattern guides the correction

Four criteria, applied in this order. The first one decided almost every case.

1. **The pattern that says where the responsibility should live is the one that guides it.**
   This is the decisive criterion, because a guiding pattern has to dictate the edit.
   Polymorphism guided `V-08`, since its correction is "each hierarchy publishes its own marker
   and the repository writes it without asking": polymorphism is the pattern that explains why
   asking is wrong. Controller guided `V-04`, since its correction is "one single place decides
   the rule and everyone else delegates". Information Expert guided the finding of my report on
   the subtotal (dev1 V-01, consolidated as V-02), since its correction is "read the number from
   the class that owns it".
2. **Among the remaining patterns, the one closest to the class that owns the information wins**,
   because that keeps the correction inside the domain instead of pushing it to every caller.
   For the warranty coverage (dev1 V-04, consolidated as V-09) the candidates were Polymorphism
   and High Cohesion; Polymorphism won, and the agreed solution is to make the coverage an
   abstract method of each subclass, which is a change in the hierarchy and nothing else.
3. **If two patterns are equally good guides, the one that fixes the order of the work wins.**
   Phase 2 uses a single shared branch with several members, so a guiding pattern that forces a
   dependency has to be named explicitly. That is why `V-08` is not registered under Protected
   Variations, even though it breaks it: the marker methods on `Product`, `Accessory`,
   `Promotion` and `Warranty` have to exist before any repository can use them, and that
   sequence is what the correction needs to state. Protected Variations is kept as the pattern
   of `V-04`, whose correction really does start with one place deciding the rule.
4. **A second pattern never produces a second correction.** If the edit that the guiding pattern
   demands already removes the problem that another pattern reports, that other pattern is
   named in the finding and not registered. This is what keeps a single defect from becoming
   two activities in Phase 2 and two chances to correct the same lines twice.

### The example worked through

`ProductRepository` is the clearest case, because one method breaks three patterns at once.
`saveAll` asks what type each product is with `if (product instanceof VideoGame)` (line 50) and
`else if (product instanceof Console)` (line 60), casts the object at lines 51 and 61, and
derives the `VIDEOGAME` or `CONSOLE` marker itself; `loadAll` compares the marker it wrote
before (lines 116 and 119). The same twelve lines are three different findings depending on the
lens:

- **Polymorphism**, because the repository restates the hierarchy instead of letting each class
  answer for itself.
- **Low Coupling**, because the persistence layer now imports `Console` and `VideoGame`
  (lines 4 and 6) and cannot be read without knowing the concrete types of the catalogue.
- **Protected Variations**, because a new product type cannot be added without editing this
  class, and there is nothing in `Product` for the compiler to point at.

Following the first criterion, it is registered once as **V-08, Polymorphism**, because
Polymorphism is the pattern that dictates the edit: let `Product` publish its own type marker.
Low Coupling and Protected Variations are named inside the same finding, and their rows in the
team verdict table point to `V-08`. Following the fourth criterion, no second correction is
planned for it: once each model publishes its own marker, the import disappears and a new
product type stops being an edit of the repository, so both secondary problems are removed by
the edit that Polymorphism already demands. One defect, one identifier, one activity in
Phase 2, and the two other patterns are already satisfied by it.

### What this achieved

Six of the nine consolidated violations are boundary findings that belong to more than one
member. Because each of them has exactly one identifier and one guiding pattern, the
consolidation could assign an order of correction per violation instead of per report: for
`V-08`, Developer 1 adds the marker methods on the four hierarchies and corrects
`ProductRepository`, and Developer 2 then corrects the other three repositories. Without the
rule, `V-08` would have been three findings in three reports, and the same twelve lines would
have been corrected three times by two people in an order nobody had agreed on.

---

## Question 4 — Preserving the external behaviour of the system

*Answered by Developer 2.*

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

---

## Question 5 — Order of the corrections

*Answered by the Technical Leader.*

The violations are corrected following their dependencies. The member who has to add a new
abstraction, interface or method works first, and the members who need to use it work after.
This order avoids pushing changes that do not compile or leaving the shared branch broken for
a while.

For example, to correct how the stock is restored, Andrés first adds
`Product.increaseStock(quantity)`. Once that change is available, I can modify
`ProductService.restoreStock` so it delegates the increase of the stock to the new method.

The team works on the same branch, `feature/grasp-refactor`. Before starting a change, each
member runs `git pull` to get the latest changes. After finishing and checking their part,
they commit and run `git push`. Since each member mainly changes the classes assigned to them
and the order between dependent changes is respected, conflicts are reduced and the shared
branch stays compilable during the whole process.
