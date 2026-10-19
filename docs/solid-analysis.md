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
