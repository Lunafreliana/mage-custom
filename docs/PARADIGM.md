# Paradigm Implementation Notes

Status: **known implementation issue, not fixed yet**  
Last reviewed: **2026-09-13**

This document records the rules requirements, current XMage implementation, known controller/owner problem, and recommended regression coverage for the **Paradigm** keyword from *Secrets of Strixhaven*.

It is intentionally kept separate from the general card implementation guide so future Paradigm work has one place to start.

## Official rules behavior

The current Comprehensive Rules define Paradigm in rule **702.192a**. In substance, Paradigm represents two spell abilities:

1. If this is the first time a spell you control with this spell's name has resolved this game, create a lasting delayed triggered ability for the rest of the game. At the beginning of each of your precombat main phases, that ability creates a copy of the object in exile and lets you cast the copy without paying its mana cost.
2. Exile the resolving spell.

The exact current wording must always be rechecked against the official rules page before changing the implementation:

- Wizards rules page: https://magic.wizards.com/en/rules
- Secrets of Strixhaven Release Notes: https://magic.wizards.com/en/news/feature/secrets-of-strixhaven-release-notes
- Secrets of Strixhaven mechanics article: https://magic.wizards.com/en/news/feature/secrets-of-strixhaven-mechanics

The *Secrets of Strixhaven* Release Notes also clarify several important properties:

- The delayed triggered ability triggers at the beginning of each of that player's first/precombat main phases for the rest of the game.
- The player may decline to cast a created copy. Declining does not remove the lasting delayed trigger.
- Once Paradigm has started, it no longer matters what happens to the original physical card in exile. The delayed trigger continues to function.
- A created copy that is not cast ceases to exist the next time state-based actions are checked.

## Controller and owner are not interchangeable

The important edge case is a player casting a Paradigm card they do **not** own, for example from an opponent's graveyard.

Comprehensive Rule **112.2** distinguishes these concepts:

- The owner of a noncopy spell is the owner of the card representing it.
- The controller of a spell is, by default, the player who put it on the stack.

Comprehensive Rule **603.7f** is also directly relevant. When a static ability generates a replacement effect that creates a delayed triggered ability, the controller of that delayed triggered ability is the controller of the object with the static ability at the time the replacement effect is applied.

For Paradigm, the relevant object is the **spell on the stack**.

Therefore, if Player A casts Player B's Paradigm card from Player B's graveyard:

- Player B remains the **owner** of the physical card.
- Player A is the **controller** of the spell on the stack.
- Player A is the player for whom the "first time a spell you control with this name has resolved" condition is evaluated.
- Player A must receive and control the lasting Paradigm delayed triggered ability.
- Future Paradigm copies are created for Player A and may be cast by Player A.
- Player B does not receive Paradigm merely because Player B owns the original card.

The "first resolution" state is consequently player-specific. Two different players can independently establish Paradigm for the same spell name if each player resolves a spell they control with that name for the first time.

## Current implementation

The shared implementation currently lives at:

`Mage/src/main/java/mage/abilities/keyword/ParadigmAbility.java`

Tests currently live at:

`Mage.Tests/src/test/java/org/mage/test/cards/abilities/keywords/ParadigmTest.java`

### Ability structure

`ParadigmAbility` is currently implemented as a static ability active on the stack:

```java
public ParadigmAbility() {
    super(Zone.STACK, new ParadigmReplacementEffect());
}
```

The replacement effect watches for a zone change from the stack to the graveyard, which is used to identify a normally resolving instant/sorcery before replacing that move with Paradigm's exile behavior.

The relevant current code is conceptually:

```java
Spell spell = game.getStack().getSpell(source.getSourceId());
Player controller = game.getPlayer(source.getControllerId());

String resolvedKey = "paradigmResolved_"
        + controller.getId() + '_'
        + spell.getName();

if (game.getState().getValue(resolvedKey) == null) {
    game.getState().setValue(resolvedKey, Boolean.TRUE);
    game.addDelayedTriggeredAbility(
            new ParadigmDelayedTriggeredAbility(spell.getCard()),
            source
    );
}
```

The physical card is then moved to exile for a noncopy spell.

The delayed triggered ability lasts until the end of the game, watches `PRECOMBAT_MAIN_PHASE_PRE`, and checks that the beginning phase belongs to its controller. Its effect creates a card copy and offers that controller the option to cast it for no mana.

### Existing test coverage

The current `ParadigmTest` covers:

- a normal owner-cast Paradigm spell being exiled and repeated on later first main phases;
- declining one Paradigm copy without removing the lasting trigger;
- resolving two same-name Paradigm spells controlled by the same player and creating only one lasting Paradigm trigger;
- a countered Paradigm spell not starting Paradigm.

The tests do **not** currently cover the owner/controller split where a player casts another player's Paradigm card.

## Known controller bug / implementation risk

The implementation currently derives Paradigm ownership/state from:

```java
source.getControllerId()
```

rather than directly from:

```java
spell.getControllerId()
```

That distinction matters because the `source` here is the card's static `ParadigmAbility`, while `spell` is the actual spell object on the stack.

XMage cards are initialized with an owner, and card abilities can carry controller information derived from that owner. Separately, the `Spell` object is constructed with the player who actually cast the spell as its controller. Those IDs are normally identical for a spell cast from its owner's hand, which is why the existing tests do not expose the problem.

They can differ when a player casts a card owned by somebody else.

The current implementation therefore has a likely rules bug in the cross-owner case:

- the `paradigmResolved_<player>_<name>` key may be associated with the card owner instead of the actual spell controller;
- the lasting delayed triggered ability may be assigned to the card owner instead of the actual spell controller;
- future copies may consequently be offered to the wrong player;
- the exile movement path also uses the `Player controller` object derived from `source.getControllerId()`, so that call should be audited at the same time for cards the caster does not own.

This should be treated as an engine/keyword implementation bug, not worked around in individual Paradigm card classes.

## Important implementation detail when fixing it

Simply changing this line:

```java
Player controller = game.getPlayer(source.getControllerId());
```

to use `spell.getControllerId()` is **not necessarily sufficient**.

`GameImpl.addDelayedTriggeredAbility(delayedAbility, source)` copies both the source ID and the **controller ID from the supplied source ability** onto the delayed triggered ability. In other words, even if the resolved-key calculation uses the correct spell controller, passing the old `source` unchanged may still overwrite the delayed trigger's controller with the wrong ID.

A correct fix must make the actual spell controller authoritative for all of the following:

1. the first-resolution tracking key;
2. the lasting delayed triggered ability's controller;
3. the player who receives future Paradigm copy/cast choices;
4. any controller-sensitive bookkeeping involved in the exile replacement.

Do not solve this by globally changing card ownership/controller semantics. The distinction between card owner and spell controller is valid and required elsewhere in XMage.

Prefer a focused Paradigm fix unless investigation demonstrates a reusable engine bug affecting other static abilities on spells.

## Recommended fix investigation

Before editing code, inspect the complete live paths for:

- `ParadigmAbility` / `ParadigmReplacementEffect`;
- `GameImpl.addDelayedTriggeredAbility`;
- `Spell` construction and controller assignment;
- static ability registration for cards/spells on the stack;
- `Player.moveCardsToExile` and whether the calling player is required to own/control the moved card;
- any modern keyword implemented as a static spell ability that creates a delayed trigger or replacement effect.

The primary source of truth for the Paradigm player should be the controller of the resolving `Spell` on the stack, not the owner of its backing card.

Be careful about mutating the original card ability's controller as a shortcut. The physical card can later change zones and its ownership remains unchanged. A local, explicit controller assignment for the Paradigm delayed trigger is safer than accidentally changing persistent card ability state, unless engine inspection proves otherwise.

## Required regression tests for the eventual fix

At minimum, add a focused test for the exact ownership edge case.

### 1. Cast an opponent-owned Paradigm spell

Set up:

- Player B owns a Paradigm card.
- The card is in Player B's graveyard.
- An effect allows Player A to cast that card from Player B's graveyard.
- Player A casts and resolves it.

Expected:

- the physical card is exiled;
- Player A gets the lasting Paradigm behavior;
- Player B does not get it;
- on Player A's next precombat main phase, Player A is offered the Paradigm copy;
- on Player B's precombat main phase, no Paradigm copy from this resolution is offered to Player B.

This is the primary regression test for the known issue.

### 2. First-resolution tracking is per controller

After Player A has established Paradigm for a spell name, allow Player B to resolve a Paradigm spell they control with the same name.

Expected:

- Player A keeps their Paradigm delayed trigger;
- Player B independently establishes their own Paradigm delayed trigger;
- each player's trigger occurs only on that player's own precombat main phase.

This verifies that the state key must be effectively `(spell controller, spell name)`, not `(card owner, spell name)` and not merely `(spell name)`.

### 3. A second resolution for the same player does not duplicate Paradigm

Keep the existing same-controller/same-name regression coverage.

Expected: only the first resolution controlled by that player creates the lasting delayed trigger.

### 4. Countered/fizzled spells do not establish Paradigm

Keep the existing countered-spell test and add an all-targets-illegal case if the implementation changes the resolution detection path.

### 5. The original card may leave exile

After Paradigm is established, move the original physical card out of exile before a later precombat main phase.

Expected: the lasting delayed trigger still creates a copy and offers it for casting, consistent with the official Release Notes.

### 6. Paradigm copies do not create duplicate lasting triggers

A Paradigm copy cast from the delayed trigger must not establish another independent Paradigm loop for the same player/name.

The current implementation already attempts to prevent this through the per-player/name state key, but the behavior should remain covered after controller handling is changed.

## Expected cross-owner example

For future debugging, this is the simplest mental model:

```text
Player B owns Germination Practicum.
Germination Practicum is in Player B's graveyard.
Player A is allowed to cast it from that graveyard.
Player A casts it.

Owner of card:       Player B
Controller of spell: Player A
Paradigm player:     Player A
Physical card exile: still the card owned by Player B
Future copies:       created/offered to Player A
```

If XMage gives the lasting Paradigm trigger to Player B in this scenario, the implementation is incorrect.

## Useful code facts

When debugging this later, keep these engine facts separate:

- `CardImpl.setOwnerId(...)` also initializes controller information on the card's abilities.
- `Spell` has its own `controllerId`, supplied by the player who casts it.
- `Spell.getAbilities()` delegates to the backing card's abilities.
- XMage already contains special handling in triggered-ability processing to use a `Spell` object's controller for triggered abilities on spells. Paradigm is different because it is currently implemented as a **static ability with a replacement effect**, so that triggered-ability correction path does not automatically prove Paradigm is safe.
- `GameImpl.addDelayedTriggeredAbility(delayedAbility, source)` assigns the delayed ability's controller from `source.getControllerId()` when a source is supplied.

These facts are why a normal owner-cast test can pass while a cross-owner cast can still be wrong.

## Fix checklist

When this issue is picked up:

- [ ] Re-read `AGENTS.md` and `docs/CARD_IMPLEMENTATION_LLM.md`.
- [ ] Re-check current CR 702.192a, 112.2, and 603.7f from Wizards' current rules page.
- [ ] Re-check the *Secrets of Strixhaven* Release Notes for Paradigm.
- [ ] Fetch the live `custom` versions of `ParadigmAbility.java` and `ParadigmTest.java` before editing.
- [ ] Add the opponent-owned-card regression test first or alongside the fix.
- [ ] Make `Spell.getControllerId()` (or an equivalent verified stack-spell controller source) authoritative for Paradigm's player-specific state.
- [ ] Ensure `addDelayedTriggeredAbility` does not overwrite the correct controller with stale card-owner ability state.
- [ ] Audit the exile movement call for non-owner casters.
- [ ] Preserve the existing behavior for countered spells, repeated same-name resolutions, optional copy casting, and persistence for the rest of the game.
- [ ] Do not run or bootstrap Maven in Codex Cloud; let GitHub Actions run the authoritative suite.

## Current status

As of 2026-09-13, the cross-owner scenario is documented as a **known implementation issue to verify and fix**. No dedicated cross-owner Paradigm regression test exists in `ParadigmTest` yet, and this document does not claim that the bug has been repaired.
