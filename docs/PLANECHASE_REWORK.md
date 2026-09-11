# Planechase Rework

NO matter what you do never download Maven dependancies. Outsource testing to GitHub

## 1. Purpose and authority

This document is the canonical architecture and migration plan for Planechase in this fork. Future Planechase engine, card, test, server, deck-building, and client changes must start here and keep this document current when an implementation decision changes.

The goals are to correct the rules core, incrementally add a real planar deck, multiple face-up planar cards, phenomena, deck-integrated supplemental-card construction, shared and individual planar-deck modes, and finally additional Plane/Phenomenon content. **Preserving playability of the old Planechase implementation during that migration is not a requirement.** Correct boundaries and a clean end state take priority over compatibility with the old simulation.

This plan is based on:

* the checked-out `custom`-branch implementation and the completed/in-progress phases recorded below;
* the official **Magic: The Gathering Comprehensive Rules effective August 7, 2026**, obtained from the current Wizards rules page during the original rework research, especially rules 103.7, 108.3a, 116.2i, 311, 312, 408.3, 701.31, 704.6f, and 901;
* upstream XMage PR #11316, inspected only as an unfinished design reference. It must not be cherry-picked, merged, or presumed correct;
* the existing XMage Commander deck model, where the serialized `.dck` sideboard already acts as a pregame staging container for Commanders and Companions rather than as a traditional competitive sideboard in Commander games.

Where this document says **must**, it describes either a rules requirement or a repository architecture decision. Temporary scaffolding is an implementation convenience, not a promise that legacy Planechase remains playable.

## 2. Current XMage architecture and migration baseline

### 2.1 Runtime representation

`mage.game.command.Plane` is a `CommandObjectImpl`, not a `CardImpl`. The Planechase runtime intentionally remains command-object-based. Plane and Phenomenon runtime objects should not be mass-converted to normal castable `CardImpl` objects just to make them visible to the deck editor.

The rework has evolved the runtime toward a common `PlanarCard` contract with stable identity, planar-card type, deck association, ownership metadata, and face state. `Plane` and `Phenomenon` are runtime command-zone objects; this remains the preferred rules-engine representation.

The deck-building representation introduced in later phases is deliberately separate from this runtime representation. A searchable/savable deck-building proxy may be a `CardImpl`, but it is only a carrier for a stable supplemental-card identity. At game initialization it is consumed and converted into the appropriate runtime object.

### 2.2 Rules core

The old implementation attached a voluntary planar-die activated ability to every Plane, counted all planar die rolls through `PlanarRollWatcher`, executed chaos effects directly from `RollPlanarDieEffect`, and immediately planeswalked on a planeswalker result.

The new rules core separates those responsibilities:

```text
RollPlanarDieSpecialAction ─┐
card RollPlanarDieEffect ───┼─> raw PlanarDieRollResult
other rules effects ────────┘           │
                                       v
                         PlanechasePlanarDieResultResolver
                         ├── BLANK: no Planechase consequence
                         ├── CHAOS: ChaosEnsuesEffect/event
                         └── PLANESWALKER: inherent trigger -> stack -> planeswalk
```

Existing Planes have been migrated away from owning roll actions. Chaos is semantic through `ChaosEnsuesTriggeredAbility`; the voluntary roll is a game-level special action; effect-generated planar rolls do not increase its escalating cost.

### 2.3 Shared planar deck runtime

`GameState` contains an ordered `SharedPlanarDeck`. Face-down planar cards are represented by that ordering while remaining rules-wise associated with the command zone. Face-up planar cards are absent from the face-down ordering and exposed through collection-first APIs.

Do **not** introduce `Zone.PLANAR_DECK`. Plane and Phenomenon cards remain in the command zone under the rules; the planar deck is an ordered supplemental structure, not a Magic zone.

### 2.4 Planar controller

The current shared mode has an authoritative planar-controller value managed centrally rather than by plane-local active-player hacks. Source-controller effects and triggers on face-up planar cards must resolve through the planar-controller model.

The current single shared UUID is an implementation for the supported shared mode, not a universal model for Grand Melee or all future team modes.

### 2.5 Phenomena and multiple face-up planar cards

The runtime supports the common `PlanarCard` abstraction and collection-first face-up APIs. Phenomena use encounter triggers and the required state-based-action flow rather than an unconditional tail effect. Beginning-of-game Phenomena are skipped/bottomed without triggering while the starting Plane is found.

New code must not restore a singleton `getCurrentPlane()` architecture.

### 2.6 Phase 7 registry/UI infrastructure

Phase 7 introduces `PlanarCardRegistry`, stable registry IDs, mixed Plane/Phenomenon metadata, `SharedPlanarDeckValidator`, public `PlanarCardView` data, and a minimal shared-deck editor in `CustomOptionsDialog`.

That table-level editor is a useful bootstrap and override/debug path, but it is **not the final deck-building UX**. The target product model is that planar cards are stored with the player's normal `.dck` file in the same pregame/supplemental area that already carries Commanders, and the match builds the runtime planar deck(s) from those submitted deck files.

The player-facing runtime must shuffle a user-configured shared planar deck before play. Deterministic known order (`setPlanes(..., false)` or equivalent) is for tests/injection seams only and must not accidentally make registry/list order the real gameplay order.

### 2.7 Current die compatibility setting

The fork currently retains XMage's historical nine-sided house-rule distribution: two chaos sides, two planeswalker sides, and five blanks (`2/2/9`). The rules die is six-sided with one chaos, one planeswalker, and four blanks (`1/1/4`). Probability correction remains an isolated follow-up so architecture changes are not mixed with probability changes.

## 3. Planechase rules model

### 3.1 Planar cards, decks, and command-zone association

* A default Planechase game gives each player a supplementary planar deck of at least ten Plane and/or Phenomenon cards. At most two may be Phenomena and English names are unique within that deck.
* Plane and Phenomenon cards remain in the command zone throughout the game, whether face down in a planar deck or face up.
* They are not permanents and cannot be cast.
* Only applicable face-up planar cards normally have functioning abilities.
* The optional single communal deck has at least forty cards or ten times the number of players, whichever is smaller; at most twice the number of players may be Phenomena; English names are unique.
* At game start, cards are turned up from the top until a Plane is found. Starting Phenomena are bottomed without encounter triggers.

The runtime data model must support both shared and individual deck association even when only one mode is currently exposed.

### 3.2 Planar controller and ownership

The planar controller is normally the active player. Under the single shared planar-deck option, the planar controller is considered the owner of the cards in that shared deck for relevant rules queries. Under individual decks, the owner is the player who began the game with the card in that planar deck.

Dynamic planar control and underlying deck association must therefore remain separate concepts.

### 3.3 Rolling the planar die

The inherent voluntary roll is a **special action**, not an activated ability. It does not use the stack. It is available only to the active player while that player has priority, during a main phase of their turn, with an empty stack.

Its generic mana cost equals the number of times that player previously took this special action that turn:

```text
first voluntary roll       {0}
effect-generated roll      no action-count change
second voluntary roll      {1}
third voluntary roll       {2}
```

A blank has no Planechase consequence. Chaos causes chaos to ensue. A planeswalker symbol creates the inherent source-less planeswalking triggered ability, which uses the stack before actual planeswalking occurs.

### 3.4 Chaos ensues

Chaos may be caused by the planar die or directly by card text. `ChaosEnsuesEffect` and `ChaosEnsuesTriggeredAbility` are the semantic shared path. The event design must retain an optional particular planar-card identity for rules text that causes chaos to ensue for a particular object.

### 3.5 Planeswalking

One common planeswalk operation must:

1. verify the acting player/context;
2. snapshot applicable face-up planar cards;
3. turn them face down and bottom them on their associated owner decks;
4. turn the actual applicable deck top face up;
5. preserve exact walked-away and walked-to object identities;
6. emit clearly contracted lifecycle events;
7. allow normal trigger/APNAP processing;
8. continue into normal priority/SBA processing.

A planar die planeswalker result does not directly perform this operation; it creates the inherent triggered ability first.

### 3.6 Phenomena

A Phenomenon is encountered when moved off a planar deck and turned face up. Its encounter ability triggers. Once the face-up Phenomenon is no longer the source of a triggered ability that has triggered but not yet left the stack, the required state-based action causes the planar controller to planeswalk.

Beginning-of-game Phenomenon handling is intentionally different and suppresses the encounter trigger.

### 3.7 Multiple face-up planar cards

New engine APIs must operate on collections, not a singleton current Plane. The model must retain exact object identity and associated deck/controller context so rules that create multiple face-up planar cards do not require another foundational refactor.

## 4. Required architecture boundaries

| Concern | Required target |
|---|---|
| Plane/Phenomenon runtime | `PlanarCard` command-object runtime, not ordinary castable cards |
| Deck-building representation | Searchable/savable supplemental-card carrier with stable ID |
| Planar deck | Ordered supplemental structure; not `Zone.PLANAR_DECK` |
| Voluntary roll | One rules-provided `SpecialAction` per eligible player/game |
| Roll cost | Prior uses of that special action, not all planar die rolls |
| Chaos | Semantic event/effect + reusable triggered ability |
| Planeswalker result | Source-less triggered ability on stack, then common planeswalk operation |
| Planar control | Central resolver/context |
| Face-up planar cards | Collection APIs with identity/deck association |
| Deck file storage | Existing `.dck` main + sideboard/pregame container; no third list required |
| Main-deck legality | Supplemental cards rejected server-side if stored in main deck |
| Commander sideboard parsing | Partition Commander/Companion from supplemental cards before Commander logic |
| Future Attractions | Same generic pregame/supplemental classification and extraction, different runtime handler |

## 5. Lessons from upstream PR #11316

Useful ideas:

* move the voluntary roll to `RollPlanarDieSpecialAction`;
* derive escalating cost from special-action use/activation count;
* separate raw planar result from Planechase rules processing;
* use `ChaosEnsuesTriggeredAbility` instead of passing chaos effect lists into the die effect;
* use a reusable exact-object `PlaneswalkToSourceTriggeredAbility`;
* use the PR's Plane migrations only as a later migration reference.

Do not import the PR wholesale. Known problems include unfinished triggers, wrong/inverted event mappings in reviewed code, incomplete special-action timing, discarded result flow, missing inherent planeswalking trigger behavior, stale controller semantics, no real deck, no Phenomena, and no individual/shared data model.

## 6. Target architecture

### 6.1 Runtime Planechase responsibilities

```text
GameState / Planechase state
├── mode: SHARED / INDIVIDUAL
├── planar deck(s): ordered PlanarCard objects/IDs
├── face-up planar-card collection
├── planar-controller context
└── per-player voluntary-roll action state

PlanarCard runtime
├── Plane
└── Phenomenon

Planechase rules
├── RollPlanarDieSpecialAction
├── PlanarDieRollResult
├── PlanechasePlanarDieResultResolver
├── ChaosEnsuesEffect
├── ChaosEnsuesTriggeredAbility
├── PlaneswalkingTriggeredAbility
├── PlaneswalkEffect / common operation
├── PlaneswalkToSourceTriggeredAbility
└── phenomenon encounter + SBA support
```

Exact class names may change, but these boundaries must not collapse.

### 6.2 Identity and events

Events and operations must carry semantic identities rather than scanning for a name/current Plane:

* roller/player that caused a planar result;
* source ability when one exists;
* optional specific planar-card ID for object-specific chaos;
* IDs of walked-away objects;
* walked-to object ID;
* player who planeswalked;
* associated planar-deck identity.

### 6.3 Deck-building representation versus runtime representation

Plane/Phenomenon runtime objects must remain `PlanarCard` command objects. To make them selectable in the normal XMage deck editor, introduce a **deck-building carrier/proxy layer** rather than converting runtime Planes to `CardImpl`.

A conceptual API is:

```java
interface SupplementalDeckCard {
    SupplementalDeckType getSupplementalDeckType();
    String getSupplementalDeckCardId();
}

enum SupplementalDeckType {
    PLANAR,
    ATTRACTION
}
```

Exact names are not mandatory. The required invariant is that a deck entry can be classified by supplemental-deck type and stable identity without hard-coding Planechase in Commander logic.

For Planechase, a searchable proxy such as `PlanarDeckCardProxy extends CardImpl` may carry the `PlanarCardRegistry` stable ID and display/image metadata. It should be an extra-deck card for deck-count/rendering purposes, but `isExtraDeckCard()` alone is **not** sufficient legality enforcement.

Future Attractions must use the same generic pregame classification/extraction boundary. Attraction gameplay objects do **not** have to use the same runtime destination as Planes. The generic layer ends at a typed handoff:

```text
.dck sideboard/pregame storage
        │
        ├── PLANAR
        │     -> Planechase handler
        │     -> PlanarCard runtime / command-zone planar deck
        │
        └── ATTRACTION
              -> future Attraction handler
              -> AttractionDeck runtime / Junkyard rules
```

Therefore no generic supplemental-deck API may assume that every supplemental card is moved to the command zone. It classifies, validates, removes the entry from ordinary sideboard gameplay, and hands it to the correct variant-specific runtime builder.

## 7. Planar die flow

`RollPlanarDieSpecialAction` must explicitly verify Planechase mode, active player, priority player, active-player main phase, empty stack, and ability to pay `{N}`. The action itself does not use the stack.

Raw rolling and Planechase consequence resolution remain separate. Card-generated rolls use the same result resolver but never touch the voluntary special-action counter.

The historical `2/2/9` probability is retained until the isolated probability-correction patch.

## 8. Chaos ensues

`ChaosEnsuesEffect` must emit the same semantic event whether caused by a chaos die result or card text. `ChaosEnsuesTriggeredAbility` must work for the applicable face-up Plane and later support object-specific chaos on a particular revealed Plane.

Do not restore a compatibility bridge that makes old per-Plane roll wrappers respond to the new event.

## 9. Planeswalking flow

### 9.1 Inherent triggered ability

A planeswalker result creates a source-less `PlaneswalkingTriggeredAbility` controlled by the roller. It enters the normal triggered-ability queue/stack. Players may respond. On resolution it invokes the common planeswalk operation.

### 9.2 Common operation

The common operation is responsible for bottoming all applicable face-up planar cards, selecting the correct deck context, turning up the actual top card, preserving object identity, and generating the correct events/triggers.

### 9.3 Planeswalk-to-source trigger

`PlaneswalkToSourceTriggeredAbility` compares exact object identity with the walked-to event target. It must not compare names or ask for a singleton current Plane.

## 10. Planar controller

Planar controller is a rules-engine concept. One authoritative API is used by planar-card source-controller effects, trigger creation, planeswalk permission/deck choice, shared-deck owner queries, and departure/turn transitions.

The API must accept planar-card/deck context even while the current shared mode resolves to one UUID, so individual decks and later multiplayer variants do not require another semantic rewrite.

## 11. Planar deck representation

### 11.1 No separate Magic zone

Do **not** create `Zone.PLANAR_DECK`.

```text
command-zone-associated PlanarCards
   +
ordered planar deck metadata
   +
face-up collection
```

represent the rules state.

### 11.2 Invariants

* A planar runtime object is in one deck ordering or face up, except during an atomic transition.
* Order uses stable object identities, not class names/display names.
* Every object retains deck association and ownership metadata.
* Shared mode has one ordering; individual mode later has one per player.
* Player-facing game initialization shuffles through the game RNG path.
* Deterministic order injection is test-only.
* Copy/rollback/restart/reconnect/spectator views preserve state without leaking hidden order.

## 12. Multiple face-up planar cards

Authoritative APIs return immutable collections such as `getFaceUpPlanarCards()`, `getFaceUpPlanes()`, and `getFaceUpPhenomena()`. New rules code iterates those collections. A singleton current-Plane helper must not be reintroduced.

## 13. Phenomena design

Phenomena use the common runtime with distinct `PHENOMENON` type, stable identity, deck/owner association, face state, encounter trigger, setup suppression, source tracking, and the 704.6f state-based action.

Do not implement the follow-up planeswalk as a delayed trigger or unconditional tail effect.

## 14. Migration, deck-staging, and feature-gating strategy

1. Prefer a correctness-first cutover; do not preserve obsolete random-plane or per-Plane roll paths.
2. Add shared rules primitives before mass card/content work.
3. Maintain one semantic roll/chaos/planeswalk path.
4. Keep runtime Planes/Phenomena command-object-based.
5. Use collection-first face-up APIs.
6. Treat `.dck` sideboard storage in Commander as a **pregame staging container**, not proof that all its cards are real gameplay sideboard cards.
7. Partition that staging container before Commander selection or `Player.sideboard` is populated.
8. Supplemental cards must never become Wish-accessible ordinary sideboard cards merely because they were serialized under `[Sideboard]`.
9. Supplemental cards must never be interpreted as Commanders merely because Commander currently scans the sideboard.
10. The generic staging layer must be extensible to Attractions and other future supplemental-deck types without putting Planechase-specific `instanceof` checks into Commander code.
11. Variant-specific runtime handlers decide where extracted cards go. Planar cards become Planechase runtime command objects; future Attractions become Attraction-deck runtime cards and use their own Junkyard/deck rules.
12. Server validation is authoritative. UI prevention is convenience, not a security/legality boundary.

### 14.1 `.dck` storage model

Do not add a third planar-deck list to the deck-file format unless a later concrete blocker proves it necessary. Use the existing two physical lists:

```text
Deck file
├── main deck
└── sideboard / pregame staging
    ├── Commander(s)
    ├── optional Companion
    ├── Planar supplemental cards
    └── future Attraction supplemental cards
```

The file format remains compatible with XMage's existing Commander convention while allowing the planar package to travel with the Commander deck automatically.

Example:

```text
Doctor Who Commander.dck

MAIN
99 normal deck cards

SIDEBOARD / PREGAME
The Tenth Doctor                 [Commander]
Agyrem                           [Planar]
Akoum                            [Planar]
Bant                             [Planar]
Mutual Epiphany                  [Planar / Phenomenon]
...
```

No separate mapping such as “Commander deck -> external planar deck file” is required. Loading the `.dck` loads its associated supplemental package.

### 14.2 Main-deck legality

A Plane/Phenomenon deck-building carrier must not be legal in the normal main deck.

Preferred UX:

* double-click/Add from the card browser routes a supplemental card directly to the correct supplemental/pregame group;
* an explicit drag/drop into the main deck is rejected with a clear message such as `Plane and Phenomenon cards belong to the Planar Deck`;
* imported/manually edited `.dck` files that place supplemental cards in main are marked **illegal** rather than silently repaired;
* server validation rejects the submitted deck even if a modified client bypasses UI restrictions.

`Deck.getMaindeckCards()` already filters `isExtraDeckCard()`, but that behavior must not be used as the sole legality rule. Silently disappearing an illegal Plane from the starting library would hide deck errors.

### 14.3 Commander sideboard partitioning

`AbstractCommander.validate` and `GameCommanderImpl.init` currently assume that almost every sideboard card is Commander/Partner/Companion material. That assumption must be removed.

Introduce one authoritative pregame classification step conceptually like:

```text
PregameDeckContents
├── commanderCandidates
├── companion
└── supplementalByType
    ├── PLANAR
    └── ATTRACTION (future)
```

Only the Commander/Companion partition enters existing Commander validation and command-zone initialization.

For Commander deck size and Commander legality, planar supplemental cards do not count toward the 100-card deck, do not become Commander candidates, and do not participate in normal Commander singleton/banned/color-identity counting. Their legality is handled by the Planechase supplemental validator. Future supplemental types similarly dispatch to their own validator rather than inheriting Planechase rules automatically.

### 14.4 Game initialization handoff

Before `Player.useDeck` creates the real library/sideboard state, extract supplemental entries from the submitted deck or otherwise ensure they never enter ordinary `Player.sideboard` gameplay.

Conceptually:

```text
submitted Deck
    │
    ├── classify pregame sections
    │
    ├── normal main -> Player.library
    ├── Commander/Companion staging -> existing Commander handling
    └── supplemental entries -> typed variant handlers
```

For `PLANAR`, the handler resolves stable registry IDs through `PlanarCardRegistry` and constructs the runtime `PlanarCard` objects/deck association.

For future `ATTRACTION`, the same generic extraction step hands actual Attraction identities/cards to the Attraction subsystem. That subsystem may construct an `AttractionDeck` and later move visited Attractions to a dedicated Junkyard representation. The generic staging layer must not know those Attraction gameplay details.

### 14.5 Transitional Phase 7 table editor

The Phase 7 `CustomOptionsDialog` shared-deck editor may remain temporarily as:

* a development/debug path;
* a table-configured override;
* a fallback for games whose submitted deck files contain no planar package.

It is not the final primary deck-building experience. Once deck-integrated construction is complete, the normal path is `player .dck -> supplemental planar entries -> match assembly`.

If both a table override and deck-supplied planar entries are present, precedence must be explicit in the UI and protocol; do not silently merge both sources.

## 15. Implementation phases

### Phase 1 — Planechase Rules Core

Implement the game-level special action, exact timing/availability, action-use-count cost, semantic planar result, effect-generated roll separation, `ChaosEnsuesEffect`, `ChaosEnsuesTriggeredAbility`, inherent planeswalking trigger, and focused deterministic tests.

Do not implement the real deck, Phenomena, mass content, or die probability correction in this phase.

### Phase 2 — Planar Controller

Implement authoritative planar-controller resolution/update behavior, source-controller integration, turn/extra-turn/departure handling, and focused controller tests. Remove the architectural need for temporary controller mutation hacks.

### Phase 3 — Existing Plane Migration

Migrate all existing registered Plane classes away from Plane-owned roll activated abilities, `PlanarRollWatcher` cost behavior, old chaos effect lists, name/singleton planeswalk-to checks, and active-player controller hacks.

Phase 3 is complete when all existing Planes declare semantic chaos abilities, none owns the voluntary planar roll, and special trigger/controller regressions are covered.

### Phase 4 — Real Shared Planar Deck

Implement one ordered shared planar deck, deterministic test injection, gameplay shuffle, real top/bottom traversal, stable identity, copy/rollback/restart behavior, hidden-order safety, and shared ownership semantics.

Do not create `Zone.PLANAR_DECK`.

### Phase 5 — Planar Card Runtime / Multiple Face-Up Planes

Evolve the common `PlanarCard` runtime, expose collection-first face-up APIs, remove singleton rules dependencies, support multiple simultaneous face-up planar cards, and make planeswalking bottom all applicable face-up objects while preserving deck association and identity.

### Phase 6 — Phenomena

Implement Phenomenon runtime behavior, encounter triggers, beginning-of-game skip/no-trigger logic, triggered-source tracking, and the required state-based-action planeswalk after the encounter ability leaves the stack.

### Phase 7 — Content Registry and Bootstrap Shared-Deck UI

Provide stable planar-card registry IDs/metadata/factory construction, mixed Plane/Phenomenon validation, match/server transport, public planar views, and a minimal four-player Commander shared-deck editor.

The Phase 7 editor is a bootstrap/override UI, not the final deck-builder model. It must not lock future work into table-owned planar decks.

Known Phase 7 product constraints/debt:

* there are not yet enough implemented unique Planar cards to construct every desired legal communal deck;
* user-facing shared decks must be shuffled before play; deterministic list order is test-only;
* individual planar decks are intentionally deferred;
* deck-integrated persistence is intentionally deferred to Phase 8 onward.

### Phase 8 — Generic Supplemental Deck Infrastructure

Build the reusable deck/pregame boundary that allows Planechase cards to be stored inside normal `.dck` files without turning runtime Planes into normal cards.

Implement:

* a generic supplemental-card classification contract carrying `SupplementalDeckType` and a stable supplemental identity;
* a Planechase deck-building carrier/proxy backed by `PlanarCardRegistry` metadata and suitable for CardRepository/deck-editor display;
* `isExtraDeckCard()`/equivalent display-count integration where useful, while keeping explicit legality validation;
* authoritative classification of main deck versus Commander/Companion versus supplemental staging;
* server validation that rejects Planes/Phenomena stored in main;
* Commander validation changes so Planar supplemental entries are ignored by Commander candidate, 100-card, normal singleton, banned, and color-identity calculations and are validated separately;
* game-init extraction so Planar proxies never become ordinary `Player.sideboard` cards or Commander candidates;
* a variant-specific handoff that converts PLANAR stable IDs into runtime `PlanarCard` objects through `PlanarCardRegistry`;
* extension points for future `ATTRACTION` supplemental cards without implementing Attraction gameplay in this Planechase phase.

Attraction compatibility is a hard acceptance requirement for this architecture. Do not name or structure the generic layer so that it assumes every supplemental card is a Plane, every supplemental runtime is in command, or every future type uses `PlanarCardRegistry`.

A conceptual dispatch boundary is:

```text
SupplementalDeckClassifier
    ├── PLANAR -> PlanechaseSupplementalHandler
    └── ATTRACTION -> future AttractionSupplementalHandler
```

Phase 8 tests must cover malformed `.dck` main placement, Commander/Companion coexistence, many Planar entries in the sideboard/pregame area, no Planar entry leaking into `Player.sideboard`, and generic classification of a synthetic/future supplemental type without Planechase-specific casting assumptions.

### Phase 9 — Deck Editor UX and `.dck` Persistence

Make the normal deck editor the primary place to build the planar package that belongs to a Commander deck.

Required UX:

```text
DECK EDITOR

Main deck (99)
────────────────────────
normal cards...

Commander / Pregame
────────────────────────
COMMANDER
The Tenth Doctor

PLANAR DECK
Agyrem
Akoum
Bant
Mutual Epiphany
...

ATTRACTION DECK          [future type; same grouping framework]
...
```

Implement:

* Planes and Phenomena searchable/browsable in the normal card selector through deck-building metadata/proxies;
* supplemental cards automatically added to their supplemental/pregame group on normal Add/double-click actions;
* explicit drag/drop into the main deck rejected with a clear message rather than silently accepted;
* grouped/labelled rendering inside the Commander sideboard/pregame area so Commander, Companion, Planar Deck, and future Attraction Deck are visually distinct even though `.dck` still serializes them in the sideboard list;
* separate counts (`Main 99`, `Commander 1`, `Planar 10`, etc.) so supplemental cards do not look like an oversized Commander deck;
* per-group legality diagnostics in the deck editor;
* save/reopen/import/export round trips that preserve supplemental identity in the same `.dck` file;
* no external “assign planar deck to deck” mapping—the planar package travels with the deck by construction;
* no hidden automatic repair of malformed imported decks. Show legality errors and require the user to fix them.

The generic visual grouping API must accept future supplemental types. Do not hard-code the sideboard renderer to exactly `COMMANDER` and `PLANAR`.

The Phase 7 table-level editor may remain available as an advanced override/fallback, but it is no longer the normal way to author a player's planar package.

### Phase 10 — Shared Planar Deck Assembly from Submitted Decks

Make deck-integrated planar packages feed the existing shared runtime model.

Default four-player Commander flow:

```text
Player A .dck -> PLANAR contribution A ─┐
Player B .dck -> PLANAR contribution B ─┤
Player C .dck -> PLANAR contribution C ─┼-> merged communal list
Player D .dck -> PLANAR contribution D ─┘
                                             │
                                             ├-> validate final shared deck
                                             ├-> construct runtime PlanarCards
                                             └-> shuffle -> SharedPlanarDeck
```

The final communal list, not each contribution individually, is validated against shared-deck rules: required minimum for actual player count, unique English names, Phenomenon cap, known IDs, and at least one Plane.

A player may contribute zero cards if the final shared deck is still legal; the storage convention should not invent a per-player minimum for shared mode. Conversely, duplicates contributed by different players that violate communal English-name uniqueness make the final deck illegal and must produce actionable diagnostics identifying the conflicting name/contributions.

Player-facing shared deck order is always shuffled through the game RNG. `.dck` sideboard iteration order, registry order, or merge order must never become gameplay order. Deterministic order remains a test seam only.

Table UX after this phase should be approximately:

```text
[x] Planechase

Planar deck mode:
(o) Shared
( ) Individual                 [disabled until Phase 11]

Shared deck source:
(o) Combine planar cards from submitted decks
( ) Use table-configured shared deck override

Contributions:
Player A   10
Player B   10
Player C   10
Player D   10
----------------
Combined   40   ✓ legal
Phenomena   4   ✓
```

If the table override is selected, it replaces the submitted-deck source; do not silently combine it with player contributions.

The server must repeat validation after all submitted decks are known and before game start. Client-side summaries are UX only.

### Phase 11 — Individual Planar Deck Mode

Add the rules-default model in which each player uses the Planar supplemental package stored in that player's own `.dck` as their individual planar deck.

For individual mode:

* each player's Planar package is validated independently using the individual planar-deck requirements (minimum size, Phenomenon cap, unique English names, and applicable type requirements);
* runtime state owns one planar deck per player rather than flattening contributions;
* each PlanarCard retains its originating deck/owner association;
* planeswalking selects the correct applicable player's deck under the rules;
* bottom placement returns walked-away planar cards to the correct owner deck;
* player-departure handling uses real owner/deck associations;
* hidden order, copy/rollback/reconnect, and public view rules apply independently to each deck;
* the planar-controller API uses deck/context parameters rather than assuming one shared global deck.

Table UX becomes:

```text
[x] Planechase

Planar deck mode:
(o) Shared
( ) Individual
```

In Individual mode, each submitted `.dck` is already associated with its Planar package, so no manual deck-to-planar-deck assignment dialog is needed.

Do not generalize Attraction gameplay into this phase. The value of the Phase 8/9 supplemental architecture is that a future Attraction subsystem can consume the same `.dck` staging/classification without sharing Planechase runtime semantics.

### Phase 12 — Incremental Plane and Phenomenon Implementation (FINAL PHASE)

Only after the rules engine, runtime deck models, generic supplemental staging, normal deck-editor UX, shared deck assembly, and individual deck mode are structurally sound should broad Plane/Phenomenon content expansion become the active phase.

Implement additional planar cards one Plane or Phenomenon at a time. Each card must follow `docs/CARD_IMPLEMENTATION_LLM.md`: verify current Oracle text, Comprehensive Rules, official release notes/rulings where available, and modern XMage mechanic implementations before coding.

Each card lands with:

* focused behavior tests;
* controller/source identity coverage;
* registry/metadata coverage;
* deck-editor proxy/discovery coverage;
* `.dck` save/load coverage where relevant;
* shared-mode construction/integration coverage;
* individual-mode construction/integration coverage where relevant.

Add cards in small reviewable patches. Mechanically similar cards may share tested infrastructure but must not be batch-converted without individual rules review. MOC, WHO, and other content are evaluated card by card in this final phase.

The former “Phase 8 — Incremental Plane and Phenomenon Implementation” is intentionally moved here so missing content does not drive the architecture before deck construction and mode semantics are stable.

### Separate follow-up — Planar die probability correction

In an isolated patch after the rules core is stable, change the historical `2/2/9` house-rule distribution to the rules-correct `1/1/4` six-sided distribution unless the product explicitly retains the old behavior as a clearly labelled configurable house rule. Update constants, semantic test helpers, UI wording, and regressions together.

## 16. Testing strategy

### 16.1 General principles

* Tests are deterministic; never retry randomness until the desired result occurs.
* Prefer narrow engine tests plus end-to-end player/deck tests.
* Verify negative availability, stack windows, exact event count, controller/source identity, object identity, and post-resolution state.
* New state components require copy/rollback/restart/reconnect coverage.
* Hidden ordered data requires no-leak view/serialization coverage.
* GitHub Actions is the authoritative Maven/JDK 17 validator. Do not download Maven dependencies in Codex Cloud solely for validation.

### 16.2 Core Planechase matrix

Retain coverage for:

1. voluntary special action only at correct timing/priority;
2. `{0}`, `{1}`, `{2}` escalating action-use cost;
3. effect-generated rolls not incrementing that counter;
4. blank/chaos/planeswalker semantic outcomes;
5. chaos triggered exactly once;
6. direct `ChaosEnsuesEffect` using the same path;
7. inherent planeswalking trigger using the stack and being answerable;
8. ordinary die triggers observing planar rolls where required;
9. numerical die-result effects ignoring planar results;
10. copy/rollback action-count state;
11. ordered deck traversal and bottoming;
12. zero/one/multiple face-up planar cards;
13. Phenomenon setup/encounter/SBA behavior;
14. controller and departure transitions.

### 16.3 Later-phase minimums

* **Phase 7:** registry discovery/construction; mixed metadata; communal validator; client/server override round-trip; public-view hidden-order safety; explicit shuffle in real player-facing use.
* **Phase 8:** supplemental classification; Planar proxy identity; main-deck rejection; Commander/Companion partition; Planar entries excluded from ordinary Player sideboard; generic typed handoff that does not assume command-zone destination; Attraction-compatible extension seam.
* **Phase 9:** card-browser discoverability; Add routing; main-deck drag rejection; grouped Commander/Pregame rendering; independent counts; `.dck` save/reopen/import/export; malformed import remains visibly illegal.
* **Phase 10:** merge contributions from multiple submitted decks; actual-player-count communal validation; duplicate-name diagnostics across players; explicit table override precedence; runtime shuffle; server revalidation; no contribution-order leak into gameplay order.
* **Phase 11:** per-player individual validation; one runtime planar deck per player; correct deck selection when planeswalking; owner-specific bottoming; player departure; rollback/reconnect/hidden-order safety; shared/individual mode selection.
* **Phase 12:** every newly implemented Plane/Phenomenon receives rules behavior, controller/source identity, registry, deck-editor, persistence, and shared/individual integration coverage as applicable.

## 17. Explicit non-goals

For the foundational Planechase work, do not:

* create `Zone.PLANAR_DECK`;
* convert runtime Plane/Phenomenon classes wholesale to `CardImpl` merely for deck-editor support;
* restore Plane-owned roll abilities;
* conflate card-generated planar rolls with voluntary action count;
* use a singleton current Plane in new rules code;
* silently accept supplemental cards in the normal main deck;
* let Planar proxies remain in ordinary `Player.sideboard` where Wish-like effects could access them;
* treat every non-Companion sideboard card as a Commander;
* create a separate `.dck` planar-deck file or manual assignment mapping unless a demonstrated blocker requires it;
* hard-code generic supplemental infrastructure so only Planechase can use it;
* implement Attraction gameplay, stickers, tickets, or Junkyard rules as part of the Planechase phases—the requirement here is only that the shared staging/classification architecture can hand future Attraction entries to their own subsystem;
* cherry-pick/merge upstream PR #11316 wholesale;
* download Maven dependencies in Codex Cloud for validation.

## 18. Open questions and required investigations

The responsible phase must resolve and document these before code lands when still applicable:

1. **Planechase state shape:** dedicated serializable state object versus smaller structures on `GameState`.
2. **Action-count storage:** protected activation bookkeeping versus a Planechase-specific counter while preserving copy/rollback behavior.
3. **Special-action installation:** persistent per-player action versus generated action, including reconnect/departure/AI discovery.
4. **Source-less trigger pipeline:** clean XMage representation and testing/countering without a fabricated source.
5. **Planeswalk event contract:** exact payload, replacement semantics, batch walked-away identity, and event ordering.
6. **Shared ownership:** rules-query implementation without losing stable deck association.
7. **Multiple-bottom order:** required choices/order when several face-up planar cards are bottomed at once.
8. **Face-down new-object semantics:** object IDs/change counters without converting runtime Planes to normal cards.
9. **Object-specific chaos/reveals:** visibility and identity for revealed Planes still inside a deck.
10. **Planar-controller scope:** shared first while retaining individual/team/Grand-Melee context.
11. **Player departure:** ordered-deck ownership and inherent-trigger disappearance behavior.
12. **Semantic test API:** dedicated planar-result injection independent of physical die distribution.
13. **Deck-building carrier design:** one generic proxy class, per-variant proxies, or a default method/marker on `Card`; whichever is chosen must preserve mock cards, CardInfo/CardRepository metadata, stable supplemental type, and stable registry identity.
14. **CardRepository integration:** how nontraditional planar proxies are indexed, searched, rendered, and assigned art/rules metadata without making runtime `Plane`/`Phenomenon` normal castable cards.
15. **Pregame classification owner:** central `Deck`/validation utility, Commander validator helper, or match-preparation object. There must be one authoritative classification path rather than duplicate client/server heuristics.
16. **`isExtraDeckCard` semantics:** reuse it for rendering/counts while ensuring explicit main-deck illegality and supplemental type are not inferred from that boolean alone.
17. **Shared contribution policy:** diagnostics for duplicate English names from different players and whether any product mode requires minimum contribution per player (the rules validator must still validate the final communal deck, not invent per-player shared requirements).
18. **Override precedence:** exact protocol/UI precedence between deck-supplied Planar packages and the Phase 7 table-configured shared deck.
19. **Individual deck transport:** whether per-player supplemental identities travel with existing deck submission objects or require a normalized prepared-deck payload after classification.
20. **Attraction compatibility:** define the typed handoff so future Attraction entries can share `.dck` storage, deck-editor grouping, main-deck rejection, and Commander partitioning without sharing Planechase command-zone/runtime behavior.
21. **Future supplemental types:** avoid enums/switches that require invasive Commander changes for every new variant; prefer registered/type-dispatched handlers where practical.
22. **UI terminology:** Commander mode should present the serialized sideboard as `Commander / Pregame` or equivalent so users understand that Planar/Attraction cards stored there are not a traditional sideboard.
23. **Hidden information:** shared/individual planar deck size/public face-up data versus hidden face-down identities for players, spectators, logs, and reconnects.
24. **House-rule compatibility:** remove the historical nine-sided distribution or retain it only as a clearly labelled option.

Until an open question is resolved, choose the narrowest reversible implementation consistent with the rules and boundaries above. Do not use uncertainty as justification to restore singleton, random-selection, Plane-owned-roll, stale-controller, or Commander-sideboard-assumption architecture.
