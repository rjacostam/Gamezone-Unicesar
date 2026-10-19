# SOLID Audit — Analysis

> Requirement 8, Phase 1. This document answers the guiding question about how the corrections of
> the audit will be made, and how the SOLID audit relates to the GRASP audit of Requirement 7.

## Answers by member

| Question | Who answers | Where |
|---|---|---|
| 1 | Developer 1 (Andrés) | `docs/solid-analysis.md` |
| 2 | Technical Leader (Rafael) | `docs/solid-analysis.md` |
| 3 | Developer 1 (Andrés) | `docs/solid-analysis.md` |
| 4 | Developer 2 (José) | This document, below |
| 5 | Technical Leader (Rafael) | `docs/solid-analysis.md` |

---

## Question 1 — Criterion for a verdict

*Answered by Developer 1.*

### The criterion the three verdicts had to pass

The three verdicts are not three degrees of severity. They are three different questions, and the
rule that separates them is this one:

> **A change the store could reasonably need to make tomorrow: how many classes would I have to
> open, and is one of them the class that already owns the information or the decision? If the
> answer is more than one, it is a violation of the principle that names the missing owner. If the
> answer is one and it is the owner, it is a correct application. If the class never takes part in
> that decision, it is not applicable.**

Each principle had to be turned into one question of that shape before anything was audited,
because a principle written as a definition cannot be applied to a class:

| Principle | The question that was asked |
|---|---|
| Single Responsibility | For how many unrelated reasons does this class change? |
| Open/Closed | For how many existing places do I have to edit to add a new kind of thing? |
| Liskov Substitution | Can every subtype be used wherever its parent is used, and answer the same question the same way? |
| Interface Segregation | Is there a subtype that inherits a contract it cannot honour? |
| Dependency Inversion | Does a policy class know a detail that belongs to a mechanism underneath it? |

- **Violation.** The principle applies to the class, the class takes part in the decision the
  principle is about, and the decision sits somewhere the principle says it should not sit. It is
  not "the class could be cleaner": it is that a foreseeable change would have to be written in
  more than one place.
- **Correct.** The principle is **present and used well**, never merely absent. A class that does
  not take part in the decision the principle governs cannot be correct; saying "Single
  Responsibility is correct" because no class does much would make the verdict meaningless. A
  correct verdict needs a place, a class and a line where the principle is doing its job.
- **Not applicable.** A precondition of the principle is not met by the class. It is not a
  compliment and it is not "nothing was found": the concept the principle describes does not exist
  in that code, so there is nothing to judge.

### The minimum evidence each verdict had to carry

| Verdict | Minimum evidence | Why that evidence |
|---|---|---|
| Violation | Class, method, exact line numbers, the literal expression that decides it, the foreseeable change that makes it expensive, the consequence, a proposed solution, and whether it touches another member's code | A violation without lines is an opinion. The literal expression is included so the reader can verify it without opening the file, and so two members who see the same lines register the same defect instead of two defects. |
| Correct | Class and method, with the line, and the reason the principle is applied well there | It is what separates *correct* from *absent*, and it is what lets the consolidation check that the same class was not called correct in one report and a violation in another. |
| Not applicable | The precondition that is not met, written in one sentence, and a pointer to where the principle does apply in the system if it applies anywhere | It turns "I did not find anything" into a checkable statement. Without the pointer, *not applicable* is indistinguishable from an audit that was not done. |

### Five rules we applied to keep the verdicts honest

1. **No finding without a nameable change.** If the modification the defect would make expensive
   could not be described, it was not a violation. This is why the three reports together raised
   seventeen findings and thirteen survived: most code is not wrong, it is simply not connected to
   a foreseeable change.
2. **The evidence was read, not remembered.** Every line number was taken from the files after
   `git pull`, on the audited commit `675e61c`, and was verified against the source before the
   finding was written.
3. **A design the class diagrams mandated is still audited.** Some classes had the shape the
   diagrams asked for. That does not exempt them: if a mandated shape produces a defect with a real
   cost, it is reported and the conflict with the diagram is stated.
4. **A defect in another member's code is reported, not audited.** It goes to the consolidation
   instead. The report says where it is and what the evidence is, and the consolidation decides
   whose finding it becomes.
5. **A problem already removed by a previous requirement is not registered, and the reason is
   written down.** This one matters for Question 3: several classes were audited as *correct*
   only because Requirement 7 had already fixed them, and a correct verdict that does not say why
   looks like an opinion. Those cases are recorded in the answer to Question 3.

### What the criterion produced, and how it was checked

All five principles are a violation for the team, so the criterion did not separate them by
accident; it separated the *members* inside each one, and that separation is what made the
consolidation useful. The clearest case is Liskov Substitution: the Technical Leader answered
*correct* and Developer 2 answered *violation* for the same class, `ProductService.findById` and
`AccessoryService.findById`, which answer differently for the same identifier. The team verdict is
*violation*, and the two answers were not averaged: they were resolved by reading both
implementations against the single question of the principle, which is a substitution one.

Thirenteen findings became thirteen after four merges. A violation that breaks several principles
was registered once, under the principle that dictates its correction, and the other principles
were named inside it, so no defect was counted twice and no defect was fixed twice.

## Question 3 — How this audit relates to the GRASP audit of Requirement 7

*Answered by Developer 1.*

The short answer is that the two requirements are related in both directions, and that neither
direction is visible from inside a single requirement. A GRASP correction can create a SOLID
problem, and a GRASP correction can also close a SOLID problem without anyone naming SOLID. Both
happened in this project, in my own code.

The awkward part of saying this is that **the SOLID audit could not see either one.** It ran on
`develop` at `675e61c`, which is already after all the Requirement 7 corrections. The auditors saw
only the result of those corrections, not the trade-off each one made. That is the main reason the
relation is worth writing down at all.

### Direction one: a GRASP correction created a SOLID violation

The example is my own finding, registered as V-01 in `docs/solid-audit/developer1-audit.md` and as
**V-08 in the consolidated audit**.

GRASP violation **V-08 of Requirement 7** was a Polymorphism violation: the repositories restated
the class hierarchies as chains of `instanceof` tests, so adding a product type meant editing four
repositories. The agreed solution was that each hierarchy publishes its own type marker and the
repositories write it without asking for the type. The correction added `Product.getCategory()` as
the marker where it already existed and created `getTypeCode()` on `Promotion` and `Warranty`, and
in this project `getExtraFields()` on `Product`, `Accessory`, `Promotion` and the accessories.

It removed every `instanceof` from the four repositories. It also moved the **column layout of the
data files into the model**, because the class that publishes the marker also has to say which
columns are its own. That produced a SOLID violation with four faces:

- **Interface Segregation**, consolidated V-08: `Product.getExtraFields` (line 54) is one contract
  about the columns of a file, and `Accessory extends Product` (line 14), so the accessories are
  forced to answer it. `VideoGame` and `Console` answer with the columns of `products.csv`;
  `Controller`, `Cable` and `Memory` answer with the columns of `accessories.csv`. A caller that
  holds a `Product` cannot tell which file the returned columns belong to.
- **Liskov Substitution**: the signature is satisfied and the meaning is not, so a substitutable
  type quietly hands back the columns of the wrong file.
- **Single Responsibility**: `Product` and `Promotion` now change when the layout of a CSV file
  changes, which is a reason that has nothing to do with a rule of the store.
- **Dependency Inversion**: the domain, which is the high level, is the part that knows the
  details of the storage format.

The consolidated audit registered it as a new violation and not as a part of GRASP V-08, with the
reason written in the finding itself: the principle is different, but its cause is the GRASP
correction. That is the honest classification, and it is worth being explicit about it because the
tempting thing to do is to say the GRASP correction "broke" something. It did not. It solved the
problem it was asked to solve and it moved knowledge to a place that does not belong there, and
both of those are true at the same time.

The practical lesson is a question to ask on every correction that moves a decision: **where does
the knowledge go, and does the class that receives it already own the data it needs to answer?**
Moving a type test out of a repository and into the model is not free; the decision does not
disappear, it lands somewhere, and the audit has to look at where it landed.

### Direction two: a GRASP correction closed a SOLID violation in passing

Two cases, both in my code, both verified against the commits that changed them. In both, the
Requirement 7 finding was a GRASP Information Expert violation, and in both the SOLID violation that
was also present was never registered, because by the time the SOLID audit started the code was
already correct on that point.

#### The stock increase, GRASP V-01

GRASP violation **V-01** was Information Expert: `ProductService.restoreStock` (lines 162–168)
increased the stock with `product.setStock(product.getStock() + quantity)`, while `Product` already
decreased it in its own `decreaseStock`. The agreed solution was to add `Product.increaseStock` and
make the service call it.

The SOLID problem that was there at the same time was never named by anyone: the **rule of the
stock was split between two classes**. `Product` enforced "a product never goes below zero units"
for selling and nothing at all for receiving, and `ProductService` carried the second half of the
same rule inline. So there were two reasons to change `ProductService`, the catalogue operations
and the way a returned unit joins the stock, and any new stock rule had to be written in two
places and checked in both.

The GRASP correction removed that. After it, `Product` owns the increase (line 103) and the
decrease (line 84), `restoreStock` calls the product, and `ProductService` has one reason to
change. That is a Single Responsibility violation closed by an edit that was argued and approved as
an Information Expert correction, and the SOLID audit could only record `ProductService` as a
correct application of Single Responsibility, because on the audited commit the violation was
already gone.

#### The base of the discount, GRASP V-02

GRASP violation **V-02** was Information Expert: `PercentageDiscount.calculateDiscount` and
`BulkPurchaseDiscount.calculateDiscount` each walked `sale.getProducts()` and added
`product.getPrice()`, rebuilding a number that `Sale.calculateSubtotal` already owns and publishes
through `getSubtotal`. The agreed solution was to read `sale.getSubtotal()` instead.

The SOLID problem there was the same kind. Each of the two promotions carried its own copy of **how
a sale is totalled**, so each had two reasons to change: its own discount rule, and the way the
store adds up a sale. If the store ever added a shipping line or a tax to the total, three classes
would have to change, when only one of them owns the sum. After the correction, only `Sale` knows
how a sale is totalled, and the two promotions know only their own percentage. Again a Single
Responsibility violation closed by a correction argued under a different principle, and again
nothing for the SOLID audit to register.

`CategoryDiscount` was deliberately left out of that correction and it shows why the distinction
matters: its loop filters by category, so its base is not the sale subtotal. Replacing it would
have changed the promotion. A correction that is right under Information Expert can still be wrong
under other readings, which is the same lesson as the one above, seen from the other side.

### What the team should take from the two directions

1. **A correction that moves a decision has to be checked in both requirements, not only in the one
   that ordered it.** Requirement 7 moved type markers and column layouts into the model, and
   Requirement 8 found it there.
2. **A violation that a previous requirement already fixed must be recorded as fixed, with the
   commit that fixed it.** Two of my correct verdicts in the SOLID audit are only correct because
   of Requirement 7, and neither of them said so in the report, which is the gap this answer
   closes.
3. **The audit has to run on a known commit, and the reports have to say which one.** All of this
   was invisible because `675e61c` already contained the corrections of `675e61c`'s predecessor.
   An audit that runs after the corrections can only see the corrections, never their cost.