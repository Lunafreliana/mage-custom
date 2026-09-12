# Planechase Rework

NO matter what you do never download Maven dependancies. Outsource testing to GitHub.

## 1. Purpose

This document is the canonical forward-looking architecture and implementation plan for Planechase in this fork.

It assumes the **current `custom` branch Planechase implementation as the baseline**. It is not a migration diary and does not compare against abandoned or historical implementations. Future Planechase engine, server, client, deck-building, validation, UI, and card-content work should build from the architecture described here.

The current system already provides the core Planechase runtime: semantic planar die handling, a game-level planar-roll special action, ordered planar-deck infrastructure, a common `PlanarCard` runtime, multiple-face-up support, Phenomena infrastructure, stable planar-card registry IDs, shared-deck validation, and bootstrap shared-deck UI.

The remaining work is primarily about making that runtime a clean product surface:

* storing planar decks with normal player deck files;
* classifying pregame/supplemental cards generically;
* keeping Commander/Companion logic separate from supplemental decks;
* making the **rules-default individual planar-deck model** the normal Planechase mode;
* retaining the official single shared planar deck as an alternative mode;
* allowing multiple user-facing construction sources for that shared mode without creating multiple rules engines;
* making the same supplemental-deck infrastructure reusable for future Attraction decks and a Junkyard;
* and only after that adding more Plane and Phenomenon content.

Where this document says **must**, it describes either a rules requirement or a repository architecture decision.

Rules references in this document follow the current Planechase rules model, especially Comprehensive Rules 103.7, 701.31, and 901.3-901.15. In particular, rule 901.3 gives each player a supplementary planar deck by default; rule 901.5 establishes the starting Plane only after opening hands/mulligans/opening-hand actions are complete; rule 701.31b uses the planeswalking player's planar deck for the new planar card while returning walked-away planar cards to their owners' planar decks; and rule 901.15 defines the single communal planar deck as an alternative option.

## 2. Current baseline

### 2.1 Runtime planar cards

Planechase runtime objects use the common `PlanarCard` abstraction and remain command-zone runtime objects rather than ordinary castable cards.

`Plane` and `Phenomenon` therefore remain rules-engine objects. They are not converted into normal `CardImpl` objects merely so the deck editor can display them.

A deck-building representation may be a lightweight card/proxy object, but that object exists only to carry stable metadata and supplemental-deck identity through the deck editor and `.dck` serialization. At game initialization it is consumed and converted into the correct variant runtime object.

### 2.2 Planar die and Planechase rules core

The current rules flow is based on a game-level special action and semantic result handling:

```text
RollPlanarDieSpecialAction ─┐
card RollPlanarDieEffect ───┼─> PlanarDieRollResult
other Planechase effects ───┘           │
                                        v
                          PlanechasePlanarDieResultResolver
                          ├── BLANK: no Planechase consequence
                          ├── CHAOS: chaos ensues
                          └── PLANESWALKER: inherent trigger -> stack -> planeswalk
```

The voluntary roll is not owned by the current Plane. Effect-generated planar rolls use the same semantic resolver but do not increase the escalating cost of the voluntary special action.

Chaos is modeled semantically through `ChaosEnsuesEffect` / `ChaosEnsuesTriggeredAbility`, and a planeswalker result creates the inherent planeswalking trigger before the actual planeswalk occurs.

### 2.3 Planar decks and command-zone association

Plane and Phenomenon cards remain rules-wise associated with the command zone. A planar deck is an ordered supplemental structure over those runtime objects.

Do **not** introduce `Zone.PLANAR_DECK`.

The current implementation already supplies the ordered-deck mechanics, stable object identity, face-up planar-card collections, real top/bottom traversal, and game-state copy/rollback behavior that later deck modes should reuse.

### 2.4 Planar controller

Planar control is resolved centrally. Source-controller effects and triggers on face-up planar cards must use that central model rather than storing permanent per-card assumptions about the active player.

Future individual decks, team variants, or Grand Melee must be able to provide additional deck/controller context without replacing the public semantics again.

### 2.5 Phenomena and multiple face-up cards

Phenomena use the same `PlanarCard` runtime boundary, with encounter triggers and the required follow-up state-based action.

Beginning-of-game Phenomena are skipped/bottomed without firing encounter triggers until a starting Plane is found.

Rules code must use collection-first face-up APIs. Do not reintroduce a singleton-current-Plane architecture.

### 2.6 Registry and bootstrap UI

`PlanarCardRegistry` is the stable catalog/factory boundary for implemented planar content. Network and deck-building layers should pass stable registry IDs rather than Java class names.

The current table-level shared-deck editor is a useful bootstrap, test, override, and fallback path. It is **not** the desired primary way for players to author planar decks long term.

The desired primary model is:

```text
normal player .dck
├── main deck
└── pregame / supplemental area
    ├── Commander(s)
    ├── optional Companion
    └── that player's Planar Deck
```

The planar deck therefore travels with the player's normal deck automatically.

## 3. Rules-default Planechase model

### 3.1 Each player has their own planar deck

The normal Planechase rules give each player a supplementary planar deck. The decks remain separate throughout the game.

```text
Player A -> Planar Deck A
Player B -> Planar Deck B
Player C -> Planar Deck C
Player D -> Planar Deck D
```

For normal individual Planechase, each player's planar deck must satisfy the applicable rules independently, including minimum size, Phenomenon limit, and unique English names.

The single communal planar deck is an official alternative described separately in section 12. It is not the baseline used to define individual-deck behavior.

### 3.2 Starting Plane

The starting Plane procedure is rules-defined and must not introduce an additional random chooser.

The pregame sequence relevant to Planechase is:

```text
starting player / turn order determined
        ↓
normal decks and planar decks shuffled
        ↓
opening hands drawn
        ↓
mulligans completed
        ↓
opening-hand actions completed
        ↓
starting player reveals from THEIR OWN planar deck
        ↓
starting Plane established
        ↓
first turn begins
```

After all players have kept their opening hands and completed applicable opening-hand actions, the **starting player** moves the top card of their own planar deck off that deck and turns it face up.

If that card is a Phenomenon, it is put on the bottom of that same planar deck and the process repeats until a Plane is turned face up. No abilities of cards turned face up during this beginning-of-game process trigger. The resulting Plane becomes the starting Plane.

There is no separate `initialPlanarDeckChooserId` in the rules-default mode.

### 3.3 Who supplies the next planar card when a player planeswalks

The engine must distinguish these concepts:

* planar controller;
* planeswalking player;
* source/controller of the spell or ability that instructed the planeswalk;
* owner/associated planar deck of each currently face-up Plane or Phenomenon.

They are related, but they are not interchangeable.

Rule-correct individual-deck planeswalking is:

```text
all applicable face-up Planes/Phenomena
        ↓
face down and bottomed on THEIR OWNERS' planar decks
        ↓
planeswalking player
        ↓
reveals top card of THEIR OWN planar deck
```

Therefore the normal deck-selection key for a planeswalk is the **planeswalking player**, not a generic "causing player" and not the owner of the Plane currently face up.

Examples:

* Player A rolls the planeswalker symbol; the inherent planeswalking trigger they control resolves; Player A planeswalks; the next planar card comes from Player A's planar deck.
* An effect instructs Player B to planeswalk; Player B is the planeswalking player; the next planar card comes from Player B's planar deck.
* The current face-up Plane came from Player C's planar deck. When Player A planeswalks away from it, that Plane returns to the bottom of Player C's planar deck, then Player A turns up the top card of Player A's planar deck.

### 3.4 Phenomena do not preserve an arbitrary previous reveal owner

A Phenomenon's follow-up transition must use the rules-defined player for that planeswalk.

When the Phenomenon state-based action instructs the **planar controller** to planeswalk, that planar controller is the planeswalking player. In individual mode, the next planar card therefore comes from that player's own planar deck.

Do not preserve a previous reveal's deck owner merely to force the next reveal from the same deck. That is not the rules model.

### 3.5 Chaos does not choose a planar deck by itself

Chaos ensuing does not itself reveal a new Plane or Phenomenon.

If a chaos ability or another spell/ability later instructs a player to planeswalk, use the player that the instruction says planeswalks. If card text performs some other explicit planar-card operation, implement that instruction according to its own text rather than pretending every new planar-card event is a planeswalk.

## 4. Rules invariants future work must preserve

### 4.1 Planar card location

Plane and Phenomenon cards remain in the command zone throughout the game, whether face down in a planar deck or face up.

A planar deck is an ordering/association structure, not a separate Magic zone.

### 4.2 Ownership and deck association

In individual mode, the owner of a Plane or Phenomenon is the player who started the game with it in their planar deck.

Every runtime planar card must retain that underlying owner/deck association even while another player is the planar controller.

Bottom placement during planeswalking returns each walked-away planar card to the bottom of **its owner's** planar deck.

### 4.3 Planar controller

The planar controller is normally the active player, subject to the Planechase multiplayer rules.

Planar controller is not the same thing as planar-card owner and is not automatically the same thing as an arbitrary source controller that caused a spell or ability to resolve.

### 4.4 Voluntary roll

The inherent voluntary planar roll remains a special action available only under the correct timing conditions. Its escalating cost counts previous uses of that special action, not arbitrary planar die rolls caused by effects.

### 4.5 Chaos

A chaos result and card text that says chaos ensues use the same semantic path.

The architecture must retain the ability to represent chaos ensues for a particular planar object.

### 4.6 Planeswalking

The common planeswalk operation must receive the **planeswalking player** explicitly.

In individual mode it must:

1. verify that the player is allowed to planeswalk in the current context;
2. snapshot all applicable face-up planar cards;
3. turn each walked-away Plane/Phenomenon face down;
4. bottom each walked-away card on its own associated owner's planar deck;
5. select the planeswalking player's planar deck;
6. move the actual top card of that deck off the deck and turn it face up;
7. preserve exact walked-away and walked-to object identity;
8. emit the correct lifecycle events;
9. allow normal trigger/APNAP processing;
10. continue through normal priority and state-based actions.

For shared mode, the same operation resolves the applicable deck through shared-mode context instead of the player's individual deck.

A conceptual context object may be useful:

```text
PlaneswalkContext
├── planeswalkingPlayerId
├── cause (PLANAR_DIE, SPELL_OR_ABILITY, PHENOMENON_SBA, DEPARTURE, OTHER)
├── sourceId / sourceAbilityId when applicable
└── mode/deck context
```

Do not overload this with a generic `causingPlayerId` and then use that field as a substitute for the rules-defined planeswalking player.

### 4.7 Hidden information

Every planar deck order remains hidden except where rules explicitly reveal information.

Player-facing gameplay shuffles through the game RNG. Deterministic known order is a test/injection seam only.

## 5. Generic supplemental-deck architecture

The deck-building and pregame layer must not be Planechase-specific.

A conceptual contract is:

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

Exact names may differ. Every supplemental deck-building entry needs:

* a supplemental type;
* a stable content identity;
* enough metadata for display/search/save/load;
* and a variant-specific runtime handler.

The generic layer must **not** assume that all supplemental cards become command-zone objects.

```text
.dck sideboard / pregame staging
            │
            ├── Commander / Companion
            │
            └── Supplemental entries
                 ├── PLANAR
                 │    -> PlanechaseSupplementalHandler
                 │    -> player planar deck / PlanarCard runtime
                 │    -> command-zone association
                 │
                 └── ATTRACTION
                      -> AttractionSupplementalHandler
                      -> AttractionDeck runtime
                      -> future Junkyard behavior
```

Planechase and Attractions may share serialization, deck-editor grouping, classification, and pregame extraction without sharing gameplay rules.

The generic layer ends at the typed handoff. Planechase decides command-zone/deck behavior. Attraction decides Attraction deck/Visit/Junkyard behavior.

## 6. `.dck` storage and pregame staging

Do not add a third planar-deck list to the deck file unless a concrete future blocker requires it.

Use the existing deck-file structure:

```text
Deck file
├── main deck
└── sideboard / pregame staging
    ├── Commander(s)
    ├── optional Companion
    ├── Planar supplemental cards
    └── future Attraction supplemental cards
```

For Commander, this serialized sideboard is a **pregame staging container**, not proof that every contained card belongs to the real in-game sideboard.

Example:

```text
Doctor Who Commander.dck

MAIN
99 normal cards

SIDEBOARD / PREGAME
The Tenth Doctor             [Commander]
Agyrem                       [Planar]
Akoum                        [Planar]
Bant                         [Planar]
Mutual Epiphany              [Planar / Phenomenon]
...
```

No external mapping such as `Commander deck -> planar deck file` is required. The planar package travels with the `.dck`.

## 7. Pregame classification and Commander integration

Before Commander validation or game initialization interprets sideboard contents, partition the serialized pregame area.

```text
PregameDeckContents
├── commanderCandidates
├── companion
└── supplementalByType
    ├── PLANAR
    └── ATTRACTION
```

Only the Commander/Companion partition enters Commander candidate logic.

Planar supplemental entries must not:

* count toward the 100-card Commander deck size;
* become Commander candidates;
* participate in normal Commander singleton counting;
* participate in Commander banned-list validation;
* participate in Commander color-identity validation;
* remain available to Wish-style sideboard access;
* or accidentally become normal `Player.sideboard` cards.

Their legality is handled by the Planechase supplemental validator.

Future Attraction entries follow the same classification boundary, but use Attraction-specific validation and runtime construction.

## 8. Main-deck legality

Supplemental cards may be serialized in the pregame/sideboard section, but they are illegal in the normal main deck.

Required behavior:

* normal Add/double-click in the deck editor routes a Plane or Phenomenon directly to its supplemental group;
* drag/drop into main is rejected with a clear message;
* manually edited/imported `.dck` files with supplemental cards in main are marked illegal;
* server validation is authoritative even if a modified client bypasses UI restrictions;
* invalid cards are never silently removed from the library to make a malformed deck appear valid.

`isExtraDeckCard()` may assist counts/rendering, but it is not the sole legality rule.

## 9. Game initialization handoff

Supplemental cards must be extracted before the real gameplay library/sideboard state is finalized.

```text
submitted Deck
      │
      ├── classify pregame contents
      │
      ├── normal main -> Player.library
      ├── Commander/Companion -> Commander setup
      └── supplemental -> typed variant handlers
```

For `PLANAR`, the handler resolves stable IDs through `PlanarCardRegistry`, constructs runtime `PlanarCard` objects, and associates them with the owning player's planar deck.

For future `ATTRACTION`, the same generic layer hands identities/cards to the Attraction subsystem. That subsystem owns Attraction deck construction, Visit behavior, and future Junkyard transitions.

The generic staging layer must not know Planechase or Attraction gameplay details beyond dispatch type.

## 10. Deck editor UX

The normal deck editor becomes the primary place to build the planar deck associated with a Commander deck.

Target UX:

```text
DECK EDITOR

Main deck (99)
────────────────────────
normal cards...

Pregame / Supplemental
────────────────────────
COMMANDER
The Tenth Doctor

PLANAR DECK
Agyrem
Akoum
Bant
Mutual Epiphany
...

ATTRACTION DECK          [future]
...
```

Required UX behavior:

* Planes and Phenomena are searchable/browsable through deck-building metadata/proxies;
* Add/double-click routes them to `PLANAR DECK` automatically;
* explicit move to main is blocked;
* Commander, Companion, Planar, and future Attraction groups are visually distinct even though they serialize in the same pregame/sideboard list;
* counts are shown separately, e.g. `Main 99`, `Commander 1`, `Planar 10`;
* legality errors are reported per group;
* save/reopen/import/export preserves supplemental identity;
* malformed imported decks remain visibly invalid until the user fixes them;
* the grouping API is generic and does not hard-code exactly `COMMANDER` + `PLANAR`.

## 11. Product mode selection

The rules engine needs only two Planechase deck modes:

```text
INDIVIDUAL   <- rules-default
SHARED       <- official single communal deck option
```

The UI may expose several ways to construct/configure the shared deck, but those are **shared-deck sources**, not separate Planechase rules modes.

Suggested table UX:

```text
[x] Planechase

Planar deck mode:
(o) Individual planar decks       [default]
( ) Shared planar deck

Shared deck source:               [only when Shared is selected]
(o) Table-configured deck
( ) Merge player contributions
```

For individual mode, the lobby can show legality/status information:

```text
Player A   planar deck 10   ✓
Player B   planar deck 10   ✓
Player C   planar deck 10   ✓
Player D   planar deck 10   ✓
```

No starting-planar-deck chooser is shown. The starting player's own planar deck supplies the starting Plane after mulligans/opening-hand actions.

## 12. Shared planar deck alternative

Planechase officially permits one single communal planar deck instead of individual planar decks.

The runtime semantics are one shared ordered deck used by all players. The shared deck obeys the communal size, Phenomenon-limit, and unique-English-name requirements.

This fork may support two user-facing construction sources for that same shared runtime mode:

### 12.1 Table-configured shared deck

Use one legal planar deck selected/configured for the table. Reuse the current bootstrap shared-deck editor where practical.

### 12.2 Merged player contributions

Collect PLANAR entries from submitted player `.dck` files, merge them, validate the final list as one communal deck, construct runtime `PlanarCard` objects, and shuffle once.

```text
Player A contribution ─┐
Player B contribution ─┤
Player C contribution ─┼-> merge -> validate -> shuffle -> SharedPlanarDeck
Player D contribution ─┘
```

Cross-player duplicate-name conflicts and communal Phenomenon limits must produce actionable validation errors.

Contribution order, `.dck` order, and registry order must never become gameplay order.

Table-configured and merged-contribution sources must not be silently combined.

## 13. Future multiplayer Planechase variants

Do not bake four-player Commander assumptions into the Planechase engine.

Future modes may require:

* team-aware planar-controller selection;
* several planar controllers at once;
* several simultaneously applicable planar decks;
* different ownership/departure behavior;
* Grand Melee-specific context;
* Two-Headed Giant primary-player semantics.

Therefore public Planechase operations should take explicit planeswalking/deck/controller context rather than depending on one overloaded global UUID.

Do not generalize away the rules-defined identities. In particular, keep distinct concepts for:

```text
planar-card owner
planar controller
planeswalking player
source controller
active player
```

They may often be the same player in ordinary games, but the architecture must not rely on that coincidence.

## 14. Attraction / Junkyard compatibility

The supplemental-deck infrastructure is intentionally reusable for Attractions.

Shared pieces:

```text
Card/deck-editor metadata
.dck pregame serialization
Supplemental type classification
Main-deck rejection
Commander-sideboard partitioning
Pregame extraction
Typed variant dispatch
Per-group UI/counts/validation
```

Not shared:

```text
PLANAR
-> PlanarCard runtime
-> command-zone association
-> planar-controller rules
-> planeswalking

ATTRACTION
-> Attraction card/runtime model
-> Attraction deck
-> Visit rules
-> Junkyard
```

Do not create a generic zone such as `SUPPLEMENTAL` or assume every supplemental card belongs in command.

The future Attraction implementation should be able to plug into the same `.dck`/deck-editor/pregame framework without modifying Commander parsing again.

## 15. Implementation phases

Phases 1-7 describe the Planechase foundation already implemented or represented by the current baseline. New work should build forward from that baseline rather than recreating migration steps.

### Phase 8 — Generic Supplemental Deck Infrastructure

Build the reusable deck/pregame boundary.

Implement:

* `SupplementalDeckType` / equivalent typed classification;
* stable supplemental identity contract;
* Planechase deck-building proxy/carrier backed by `PlanarCardRegistry`;
* explicit main-deck rejection;
* pregame partitioning before Commander validation;
* Commander/Companion validation on only their partition;
* supplemental entries excluded from real `Player.sideboard`;
* typed runtime handler dispatch;
* PLANAR handler construction of **per-player planar decks**;
* extension seam for future ATTRACTION without implementing Attraction gameplay.

Acceptance tests must include malformed main placement, Commander + Companion + Planar coexistence, many Planar entries in pregame storage, no Planar cards leaking into ordinary sideboard gameplay, and a synthetic second supplemental type proving the generic layer is not Planechase-cast-specific.

Implementation note: the generic boundary uses `SupplementalDeckCard` for the
stable type/id contract, `SupplementalDeckPartition` to separate supplemental
entries before format-specific pregame validation, and typed
`SupplementalDeckRuntimeHandler` dispatch. `PlanarDeckCard` is the non-castable
registry-backed carrier. Runtime PLANAR dispatch constructs a separately
shuffled planar deck for each contributing player; `ATTRACTION` is reserved as
an extension type and has no gameplay handler until Attraction support is
implemented.

### Phase 9 — Deck Editor UX and `.dck` Persistence

Make the normal deck editor the primary authoring surface.

Implement searchable Planes/Phenomena, automatic routing to Planar Deck, grouped pregame rendering, separate counts, legality diagnostics, save/load/import/export round trips, explicit main-deck rejection, and generic supplemental grouping that can later display Attraction Deck.

Do not require a separate planar-deck file or assignment mapping.

### Phase 10 — Rules-Default Individual Planar Deck Gameplay

Implement the normal Planechase model described in sections 3 and 4.

Required work:

* retain one independently shuffled planar deck per player;
* validate each player's planar deck independently;
* use the normal game starting-player/turn-order procedure;
* complete opening-hand draws, mulligans, and opening-hand actions first;
* then have the starting player reveal from **their own planar deck** until the starting Plane is found;
* suppress encounter/other triggers from beginning-of-game Phenomena while finding that starting Plane;
* make the common planeswalk operation receive an explicit `planeswalkingPlayerId`;
* in individual mode, reveal the next planar card from the planeswalking player's planar deck;
* return every walked-away Plane/Phenomenon to the bottom of its own associated owner's planar deck;
* ensure a Phenomenon SBA uses the planar controller as the planeswalking player rather than preserving an arbitrary previous reveal/deck attribution;
* keep planar-card owner, planar controller, active player, source controller, and planeswalking player distinct in APIs/state;
* preserve independent hidden order, rollback, reconnect, serialization, and player-departure behavior for every planar deck;
* add focused tests for planar-die planeswalks and spell/ability-instructed planeswalks by different players.

This phase is complete when individual planar decks are the normal supported Planechase flow and no merged/shared deck is required to start an ordinary game.

### Phase 11 — Shared Mode and Variant-Ready Context

Finish/harden the official single communal planar-deck alternative without changing individual-mode semantics.

Implement/support both shared-deck construction sources:

* table-configured shared deck;
* merged player contributions.

Both sources feed the same `SHARED` runtime rules mode.

Also harden context APIs required by later Planechase formats so planar control, ownership, planeswalking player, deck selection, and departure are not represented by one overloaded global UUID.

### Phase 12 — Incremental Plane and Phenomenon Content

Only after the deck-building, individual gameplay, shared alternative, and supplemental infrastructure are stable should broad content implementation become the focus.

Add additional Planes and Phenomena one card at a time.

Each card must:

* verify current Oracle text, Comprehensive Rules, official rulings/release notes where relevant, and current XMage mechanic patterns;
* use the common `PlanarCard` runtime and registry;
* have focused behavioral tests;
* have registry/metadata/deck-editor coverage;
* work in individual mode and shared mode unless the card rules genuinely require a mode-specific restriction;
* preserve exact source/controller/planeswalking-player semantics;
* avoid introducing card-local substitutes for common Planechase operations.

Mechanically similar cards may share reusable tested infrastructure, but individual rules review remains required.

## 16. Testing requirements

### 16.1 General

* Tests are deterministic.
* Known deck order is a test seam only; gameplay paths shuffle.
* Test server-side validation independently of UI prevention.
* Every new state component supports copy/rollback/restart/reconnect where applicable.
* Hidden planar order must not leak through views/logs.
* Ordinary non-Planechase games remain unaffected.
* GitHub Actions is the authoritative Maven/JDK validation environment. Do not download Maven dependencies in Codex Cloud solely for validation.

### 16.2 Supplemental infrastructure

Test:

* Plane in main -> illegal;
* Plane in pregame -> classified PLANAR;
* Commander remains Commander;
* Companion remains Companion;
* Planar cards do not inflate Commander deck count;
* Planar cards are absent from real gameplay sideboard;
* Attraction-like synthetic type follows the same generic classifier without Planechase casting;
* `.dck` save/load preserves stable supplemental identity.

### 16.3 Individual mode

Test:

* each player's planar deck is validated independently;
* all player planar decks are shuffled independently;
* mulligans and opening-hand actions complete before the starting Plane is revealed;
* the starting player supplies the starting Plane from their own planar deck;
* beginning-of-game Phenomena are bottomed on that same deck without encounter triggers;
* starting setup stops on the first Plane from the starting player's deck;
* Player A's planeswalker roll causes Player A to planeswalk and reveal from A's deck;
* Player B's planeswalker roll causes Player B to planeswalk and reveal from B's deck;
* a spell/ability instructing Player B to planeswalk uses B's planar deck regardless of the effect's source controller when those differ;
* a walked-away Plane originally owned by Player C returns to C's deck even when Player A is the planeswalking player;
* ordinary chaos with no planeswalk/reveal instruction does not consume a planar card;
* a Phenomenon SBA makes the planar controller planeswalk and uses that player's deck in individual mode;
* one player's empty/invalid deck cannot silently fall back to another player's deck;
* rollback/reconnect preserves all independent deck orders and ownership associations;
* player departure has deterministic rules-correct behavior.

### 16.4 Shared mode

Test table-configured and merged-contribution construction separately, then test that both enter the same shared runtime semantics.

Shared mode must not mutate or flatten individual-mode state when individual mode is selected.

### 16.5 Attraction compatibility

Before the generic supplemental layer is considered stable, prove that a future Attraction implementation can provide:

```text
ATTRACTION entry
-> same .dck pregame storage
-> same generic classification/extraction
-> Attraction-specific handler
-> AttractionDeck
-> future Junkyard
```

without adding Attraction-specific branches to Commander parsing.

## 17. Non-goals

Do not:

* create `Zone.PLANAR_DECK`;
* convert runtime Plane/Phenomenon objects into ordinary castable cards solely for deck-editor support;
* treat all pregame/sideboard entries as real gameplay sideboard cards;
* make Planar supplemental cards Commander candidates;
* silently remove illegal Planar cards from main deck;
* make shared or merged contributions the default player experience;
* add a random starting-planar-deck chooser to rules-default individual Planechase;
* reveal the starting Plane before mulligans/opening-hand actions;
* choose the next individual planar deck using a generic causing-player field when the rules identify a planeswalking player;
* force a Phenomenon follow-up planeswalk to continue from the deck that revealed that Phenomenon;
* infer the next planar deck from the current face-up Plane;
* assume every supplemental deck uses the command zone;
* implement Attraction gameplay inside Planechase classes;
* use broad Plane/Phenomenon content work to bypass missing generic infrastructure.

## 18. Open implementation questions

The responsible phase must resolve these concretely before code lands:

1. Exact Java shape of `SupplementalDeckCard` / `SupplementalDeckType`.
2. How deck-building Planar proxies are registered in `CardRepository` without becoming normal spells.
3. Whether pregame partitioning returns a value object or exposes typed deck views.
4. The cleanest point to remove supplemental entries before `Player.sideboard` is populated.
5. Exact legality-error representation for supplemental cards found in main.
6. How grouped pregame sections serialize layout metadata while `.dck` remains compatible.
7. Exact initialization hook for constructing/shuffling all player planar decks before opening-hand processing while delaying the starting-Plane reveal until after mulligans/opening-hand actions.
8. Exact `PlaneswalkContext` API and how it records the planeswalking player without conflating that player with source controller, planar controller, or card owner.
9. Exact handling of spells/abilities that directly instruct a player to planeswalk versus effects that perform some other planar-card manipulation.
10. Player-departure behavior when face-up planar cards originated in the departing player's planar deck.
11. Whether invalid/empty individual planar decks are always rejected before game start or require any runtime defensive path.
12. How shared-mode ownership queries coexist with stable individual owner/deck association metadata in common runtime classes.
13. Exact protocol enum/naming for `INDIVIDUAL` versus `SHARED` and for shared-deck construction source.
14. How future Two-Headed Giant/Grand Melee contexts provide the correct planar controller and planeswalking player without overloading one UUID.
15. How future Attraction deck legality and Junkyard views plug into the generic supplemental UI without Planechase assumptions.

Until a question is resolved, choose the narrowest reversible design consistent with the rules and boundaries above.
