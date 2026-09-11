# Planechase Rework

## Upstream Reference

A previous unfinished Planechase refactor exists as
[magefree/mage PR #11316](https://github.com/magefree/mage/pull/11316).
Future Planechase tasks **must** inspect this PR when relevant.

Use:

```shell
gh pr view 11316 --repo magefree/mage
gh pr diff 11316 --repo magefree/mage
```

For complete source inspection:

```shell
git fetch https://github.com/magefree/mage.git \
  refs/pull/11316/head:refs/remotes/upstream-pr/11316
```

Do not merge or cherry-pick this PR. It is an unfinished architecture reference only.

Absolutely — here is a repo-friendly English version of the text you pasted:

---

I have now actually gone through **#11316 as an architecture proposal**, rather than just giving it a quick look. The most important conclusion first:

**I would absolutely not cherry-pick PR #11316. I would reuse its core ideas and rebuild the implementation properly.** The PR has been an open draft since 2023, consists of only two commits, changes 35 files, and was explicitly posted by its author as an unfinished attempt.

I also found an important clarification while checking the **current June 2026 Comprehensive Rules**: we should **not introduce a new `Zone.PLANAR_DECK`**. Plane and phenomenon cards remain rules-wise in the **command zone**, even while they are part of the face-down planar deck. The “planar deck” is therefore an additional **ordering of cards within the command zone**, not a separate Magic zone.

That actually makes the XMage design somewhat cleaner.

## What #11316 got right

The PR essentially tries to transform this:

```text
Plane
 ├─ actual Plane ability
 ├─ "Roll planar die" ActivatedAbility
 ├─ CHAOS effect
 ├─ PlanarRollWatcher
 └─ CostIncreasingEffect
```

into this:

```text
Planechase rules
 └─ RollPlanarDieSpecialAction
       ↓
     planar die
       ↓
 ┌─────┼──────────┐
blank chaos   planeswalker
       │
       ▼
 CHAOS_ENSUES
       │
       ▼
Plane chaos trigger
```

**That direction is absolutely correct.**

`SpecialAction` is already designed in XMage not to use the stack; the base class explicitly sets `usesStack = false`. That makes it much closer to rule 901.9 than the current activated ability attached to every individual Plane.

The other major idea is also correct: the PR introduces `ChaosEnsuesTriggeredAbility`. Instead of every Plane passing its chaos effect into `RollPlanarDieEffect`, code like this:

```java
new RollPlanarDieEffect(chaosEffects, chaosTargets)
```

becomes simply:

```java
new ChaosEnsuesTriggeredAbility(
    new DiscardHandControllerEffect()
)
```

on the Plane itself.

That can be seen, for example, in Academy at Tolaria West. This is roughly how modern Plane implementations should look. The current rules wording really is “Whenever chaos ensues,” and chaos can explicitly be caused by spells and abilities now, not only by rolling the planar die.

The attempt to replace Panopticon’s custom class:

```java
PanopticonTriggeredAbility
```

with a generic:

```java
PlaneswalkToSourceTriggeredAbility
```

is also conceptually excellent. It prevents 100 future Plane classes from reimplementing the same event checks.

## But #11316 is not usable as production code

These are the most important findings from the PR:

| Part of #11316                       | Assessment                             | Reason                                                                                |
| ------------------------------------ | -------------------------------------- | ------------------------------------------------------------------------------------- |
| `RollPlanarDieSpecialAction`         | **Keep the concept, rewrite the code** | correct mechanism, but timing and result handling are incomplete/incorrect            |
| `ChaosEnsuesTriggeredAbility`        | **Keep**                               | fundamentally sound abstraction                                                       |
| `PlaneswalkToSourceTriggeredAbility` | **Keep the concept**                   | implementation is unfinished                                                          |
| `ROLLED_PLANESWALK` event            | **Change the design**                  | the actual inherent triggered ability is missing                                      |
| `Player.rollPlanarDieResult()`       | **Partially keep**                     | separating raw rolling is good, but Planechase rule logic should not live in `Player` |
| `getActivatedThisTurnCount()`        | **Keep the concept**                   | correct direction for planar roll costs                                               |
| migration of existing Planes         | **Reuse later**                        | engine should be fixed first                                                          |
| `Plane.addAbility()`                 | **Fine**                               | convenience helper only                                                               |
| real planar deck                     | **Missing entirely**                   | PR does not solve the central structural issue                                        |
| Phenomena                            | **Missing entirely**                   | no encounter/SBA engine                                                               |
| planar controller                    | **Not solved**                         | still structurally incorrect                                                          |

The first concrete problem is significant: `SpecialAction` inherits from `ActivatedAbilityImpl`, whose default timing is:

```java
protected TimingRule timing = TimingRule.INSTANT;
```

The new `RollPlanarDieSpecialAction` never changes that timing.

The Comprehensive Rules instead require the action to be available only to the active player, while they have priority, during their main phase, while the stack is empty.

So the PR introduces the correct kind of special action, but with the wrong timing behavior.

## The new die-roll flow is also not fully connected

The PR sensibly separates raw rolling into:

```java
rollPlanarDieResult(...)
```

and another layer that is supposed to process the result.

However, `RollPlanarDieSpecialActionEffect` ultimately just calls:

```java
player.rollPlanarDieResult(...)
```

and discards the returned value.

That means the Special Action rolls the planar die but does not correctly cause either chaos or planeswalking afterward.

There is also a confirmed bug in the `Player` patch: `CHAOS_ROLL` and the planeswalker result were mapped to the wrong events. A reviewer pointed this out, and the author said it was probably caused by a merge conflict because the code had already become old.

That illustrates the state of the PR quite well:

**It is an architecture sketch, not an implementation that should be imported.**

## `ROLLED_PLANESWALK` does not actually planeswalk

This is the next important architectural point.

#11316 adds:

```java
GameEvent.EventType.ROLLED_PLANESWALK
GameEvent.EventType.CHAOS_ENSUES
```

and `WillOfThePlaneswalkersEffect` fires `ROLLED_PLANESWALK` when the relevant vote wins.

But the PR never finishes the ability that turns that event into actual planeswalking.

That matters because the real rules are more subtle than:

```text
roll planeswalker symbol
→ immediately change Plane
```

Under the current rules:

```text
Planar die shows planeswalker symbol
             ↓
source-less inherent planeswalking ability triggers
             ↓
ability goes onto the stack
             ↓
players may respond
             ↓
on resolution:
planeswalk
```

Rule 901.8 explicitly defines this source-less inherent triggered ability, and 901.9c explicitly says it uses the stack.

The current XMage implementation is therefore also not quite correct, because `RollPlanarDieEffect` directly executes:

```java
new PlaneswalkEffect(false).apply(...)
```

#11316 appears to recognize that an event should exist between the die result and the actual planeswalk, but it never finishes the second half.

## The roll-cost idea is surprisingly important

This is one area where the current code contains a subtle rules issue and #11316 moves in the correct direction.

Today XMage uses `PlanarRollWatcher`, which essentially counts:

```text
how many times this player
rolled the planar die this turn
```

But **that is not what the increasing Planechase cost is supposed to count**.

Rule 901.9 says the cost depends on how many times that player has taken **this special action** during the turn.

If a spell or ability causes an additional planar die roll, it does **not** increase the cost of the next voluntary Planechase roll.

For example:

```text
first voluntary Planechase roll → {0}
Fractured Powerstone rolls      → does not affect the counter
second voluntary roll           → {1}
third voluntary roll            → {2}
```

The old `PlanarRollWatcher` cannot correctly distinguish those cases.

#11316 instead bases the cost on the **activation count of the Special Action**.

Conceptually, that is exactly right.

I would not necessarily expose `getActivatedThisTurnCount()` publicly on the entire `ActivatedAbilityImpl` class, though. The `RollPlanarDieSpecialAction` subclass may be able to use the existing activation information internally, which would avoid touching more general engine code than necessary.

## The planar-controller problem becomes even more obvious

Take Panopticon.

#11316 simplifies its custom trigger into:

```java
new PlaneswalkToSourceTriggeredAbility(
    new DrawCardSourceControllerEffect(1)
)
```

That would be elegant — **if the Plane had the correct planar controller**.

Currently it does not.

The existing `Plane` object simply stores:

```java
private UUID controllerId;
```

and that controller is assigned when the Plane is created.

The rules instead say that the **planar controller is normally the active player**.

With the single planar deck option, that planar controller is also treated as the owner of the cards in the shared planar deck for relevant rules purposes.

That means many of the cleaner `SourceControllerEffect` conversions from #11316 would still affect the wrong player.

**Planar-controller handling should therefore be fixed before mass-migrating the Plane classes.**

# Proposed target architecture

I would avoid scattering Planechase logic across `Player`, `GameImpl`, and every individual Plane class.

Instead, there should be a small dedicated Planechase core:

```text
GameState
   │
   └── PlanechaseState
          │
          ├── PlanarDeck
          │      └── ordered PlanarCard IDs
          │
          ├── faceUpPlanarCards
          │
          ├── planarControllerId
          │
          └── mode: SHARED / INDIVIDUAL

PlanarCard
   ├── Plane
   └── Phenomenon

Planechase Rules
   ├── RollPlanarDieSpecialAction
   ├── PlanarDieRollResolver
   ├── ChaosEnsuesEffect
   ├── ChaosEnsuesTriggeredAbility
   ├── PlanarDiePlaneswalkTriggeredAbility
   ├── PlaneswalkEffect
   └── EncounterPhenomenonAbility
```

For now, I would keep `PlanarCard` on the existing `CommandObject` side of the engine rather than immediately converting every Plane into `CardImpl`.

That minimizes the impact on Mage.Sets, card repositories, serialization, views, and UI.

However, the runtime representation eventually needs information that `Plane` currently lacks, such as:

```java
CardType getPlanarCardType(); // PLANE / PHENOMENON

UUID ownerId;
UUID controllerId;

boolean faceUp;
boolean revealed;
```

and proper identity inside the planar deck.

The current behavior where:

```java
getCardType() -> Collections.emptyList()
```

cannot remain the final architecture.

## The planar deck should not be a Zone

This is the most important correction to my earlier architecture sketch.

Under the current rules:

```text
COMMAND ZONE
│
├─ face-down PlanarCard
├─ face-down PlanarCard
├─ face-down PlanarCard
├─ face-down PlanarCard
│
└─ FACE-UP Plane
```

The `PlanarDeck` merely defines an order:

```text
top
 ↓
[Agyrem]
[Tazeem]
[Phenomenon]
[Jund]
[...]
 ↑
bottom
```

All of those cards are still rules-wise associated with the command zone.

So `PlanarDeck` should be something closer to:

```java
class PlanarDeck {
    Deque<UUID> cardOrder;
}
```

and **not a new Zone**.

This is also a useful general pattern for other supplemental decks without inventing artificial Magic zones.

## The planar die flow should be cleanly separated

`Player` should only perform the actual random roll:

```java
PlanarDieRollResult result =
    player.rollPlanarDieResult(...);
```

Then Planechase rules should handle the result:

```java
planechase.resolvePlanarDieRoll(
    playerId,
    result,
    source,
    game
);
```

Conceptually:

```text
BLANK
  ↓
nothing


CHAOS
  ↓
ChaosEnsuesEffect
  ↓
CHAOS_ENSUES event
  ↓
ChaosEnsuesTriggeredAbility on the relevant Plane
  ↓
stack


PLANESWALKER
  ↓
PLANAR_DIE_PLANESWALK event
  ↓
inherent PlaneswalkingTriggeredAbility
  ↓
stack
  ↓
PlaneswalkEffect on resolution
```

This finally separates **rolling a die** from **Planechase rules processing**.

Cards can still simply use something like:

```java
new RollPlanarDieEffect()
```

without increasing the activation counter of the Planechase Special Action.

## Add `ChaosEnsuesEffect`

#11316 only introduces the trigger:

```java
ChaosEnsuesTriggeredAbility
```

The PR discussion itself suggests that a dedicated effect analogous to `PlaneswalkEffect` would be useful, especially for cards such as Missy.

So:

```java
new ChaosEnsuesEffect()
```

should fire something like:

```java
game.fireEvent(
    CHAOS_ENSUES
);
```

Then a card with text like:

```text
"... draw a card and chaos ensues."
```

could simply implement:

```java
ability.addEffect(new DrawCardSourceControllerEffect(1));
ability.addEffect(new ChaosEnsuesEffect());
```

That is exactly the kind of reusable primitive needed for WHO/MOC cards.

I would also design the effect so that it can later optionally refer to a **specific Plane object**.

The current rules support “chaos ensues for a particular object”; in that case the chaos ability of a revealed Plane in the planar deck may matter even if it is not the normal face-up Plane.

## Finish `PlaneswalkToSourceTriggeredAbility`

The underlying concept is straightforward.

XMage already has `PLANESWALK` and `PLANESWALKED`, and `GameImpl.addPlane()` currently fires `PLANESWALKED` after the new Plane has been added.

A generic trigger can therefore roughly check:

```java
event.getType() == PLANESWALKED
&& event.getTargetId().equals(source.getSourceId())
```

and then trigger using the correct planar controller.

#11316 creates this class, but the important methods effectively still contain:

```java
// TODO: implement
return false;
```

That means, for example, the converted Panopticon ability in that PR would not work.

So the class is useful as an **API/name proposal**, not as finished code.

## New code must not assume there is exactly one current Plane

This is another architectural issue worth fixing early.

XMage currently relies heavily on:

```java
game.getState().getCurrentPlane()
```

and `GameImpl.addPlane()` even rejects the presence of a second Plane.

That is insufficient for complete Planechase.

The current rules explicitly support situations where **multiple Plane cards are face up at the same time**, and Phenomena also create mechanics where this distinction matters.

Rule 901.11c even defines what happens when planeswalking while multiple Plane cards are face up.

Therefore, long term:

```java
Plane getCurrentPlane()
```

should become something like:

```java
Collection<Plane> getFaceUpPlanes()
Collection<PlanarCard> getFaceUpPlanarCards()
```

`getCurrentPlane()` may remain temporarily as a compatibility helper:

```java
@Deprecated
Plane getCurrentPlane()
```

but new engine code should not be built around it.

That avoids a second major refactor later.

# Phenomena require a real engine

This is where the current random-Plane model fundamentally stops being sufficient.

The rules work roughly like this:

```text
Top card of planar deck becomes face up
            ↓
is it a Plane?
      → normal Plane behavior

is it a Phenomenon?
      ↓
"When you encounter ..." triggers
      ↓
ability goes onto the stack
      ↓
after the Phenomenon ability leaves the stack
      ↓
state-based action
      ↓
planar controller planeswalks again
```

Rule 312.5 defines “encounter,” and 312.7 defines the relevant state-based action.

At the **start of the game**, the behavior is different again: if the top card is a Phenomenon, it is put on the bottom of the planar deck and the process continues. Its encounter ability does not trigger.

That cannot be modeled cleanly with `Plane.createRandomPlane()`.

# Recommended project breakdown

I would not implement this as one enormous 100-file commit.

I would split it into seven independently testable phases:

1. **Planechase Rules Core.** Rebuild `RollPlanarDieSpecialAction` correctly, enforce active-player/main-phase/stack-empty timing, use the Special Action activation count instead of `PlanarRollWatcher` for increasing costs, add `ChaosEnsuesEffect` + `CHAOS_ENSUES`, and implement the inherent planeswalking trigger so the planeswalk itself goes onto the stack. Do not mass-migrate all existing Planes yet.

2. **Planar Controller.** Introduce central `planarControllerId` behavior and update it correctly when turns change or players leave. Face-up Plane abilities must automatically operate under the correct controller. Remove the need for manual `source.setControllerId(activePlayer)` hacks.

3. **Migrate Existing Plane Abilities.** Convert the current Plane classes to `ChaosEnsuesTriggeredAbility`, `PlaneswalkToSourceTriggeredAbility`, and the new controller model. Remove the repeated `ActivateIfConditionActivatedAbility + PlanarRollWatcher + CostIncreasingEffect` boilerplate. This is the point where much of #11316 becomes useful as a migration reference.

4. **Real `PlanarDeck`.** Replace random Plane selection / `seenPlanes` simulation with a real ordered and shuffled supplemental planar deck. Do not introduce a new Magic zone; the planar cards remain command-zone objects. `PlaneswalkEffect` should put the relevant face-up cards on the bottom of their decks and reveal the actual next card.

5. **`PlanarCard` + Multiple Face-Up Planes.** Introduce a shared runtime abstraction for Plane and Phenomenon, properly model face-up/face-down state and ownership, and migrate the engine away from `getCurrentPlane()` toward collections.

6. **Phenomena.** Implement encounter triggers, beginning-of-game special handling, and the required state-based action. Only then start adding actual Phenomenon cards.

7. **Content and UI.** Add MOC, WHO, and the remaining Planechase Planes/Phenomena, then add shared planar-deck selection and eventually individual planar decks in the client. The existing XMage mode can initially remain compatible by automatically generating a shared deck from all implemented planar cards.

After **Phase 1**, the Planechase core is already significantly more correct without tearing apart the entire engine.

After Phase 3, the existing Plane classes are cleanly migrated.

Phases 4–6 then turn the existing simulation into an actual Planechase implementation.

## What I would specifically reuse from #11316

Not the PR as a whole.

I would extract these ideas:

```text
RollPlanarDieSpecialAction
        ✓ concept

ChaosEnsuesTriggeredAbility
        ✓ almost directly reusable

PlaneswalkToSourceTriggeredAbility
        ✓ API/name
        ✗ implementation must be rewritten
```

The mass edits to existing Plane classes can also serve as a useful **migration reference** later.

I would deliberately **not copy** these parts directly:

```text
Player.rollPlanarDie() containing Planechase event logic
ROLLED_PLANESWALK without a real triggered ability
the current RollPlanarDieSpecialAction implementation
the current Plane controller model
the unfinished PlaneswalkToSource trigger
```

Most importantly, I would **not try to make #11316 compile first and then build on top of it**.

That would preserve several of its architectural problems only for us to remove them again afterward.

## The first Codex task should be much smaller than “implement Planechase”

For the first implementation step, I would give Codex only this target:

```text
OLD
Plane owns planar die ability
        ↓
RollPlanarDieEffect directly runs chaos/planeswalk


NEW
Game owns RollPlanarDieSpecialAction
        ↓
raw planar die result
        ↓
Planechase result resolver

CHAOS
 → ChaosEnsuesEffect
 → CHAOS_ENSUES
 → plane trigger

PLANESWALKER
 → inherent planeswalking triggered ability
 → STACK
 → existing PlaneswalkEffect
```

**No real Planar Deck in the same task. No Phenomena. No mass migration of all 21 existing Planes.**

That is the key difference between a controlled refactor and one giant prompt: build the foundation first so we do not have to rewrite 100 Plane classes twice.

One detail I would also leave alone during Phase 1: the current XMage **9-sided planar die**.

First fix the engine architecture and get tests passing.

Changing the probabilities to the rules-correct:

```text
1 Chaos
1 Planeswalker
4 blank
```

can then be done as a very small isolated follow-up.

That makes regressions much easier to diagnose because we can distinguish architecture problems from probability changes.

---

In short: **PR #11316 is extremely useful as a design reference, but not as code to merge.** The strongest ideas are the Special Action, a dedicated chaos event/trigger, and reusable planeswalk-to-source handling. The missing pieces are the real planar deck, correct planar-controller semantics, the inherent planeswalking trigger, multiple face-up planar cards, and Phenomena. Those should be built incrementally rather than patched into the unfinished PR.



