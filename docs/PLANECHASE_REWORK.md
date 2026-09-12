# Planechase Rework

NO matter what you do never download Maven dependancies. Outsource testing to GitHub.

## 1. Purpose

This document is the canonical forward-looking architecture and implementation plan for Planechase in this fork.

It assumes the **current `custom` branch Planechase implementation as the baseline**. It is not a migration diary and does not compare against abandoned or historical implementations. Future Planechase engine, server, client, deck-building, validation, UI, and card-content work should build from the architecture described here.

The current system already provides the core Planechase runtime: semantic planar die handling, a game-level planar-roll special action, ordered planar-deck infrastructure, a common `PlanarCard` runtime, multiple-face-up support, Phenomena infrastructure, stable planar-card registry IDs, shared-deck validation, and bootstrap shared-deck UI.

The remaining work is primarily about making that runtime a clean product surface:

* storing planar packages with normal player deck files;
* classifying pregame/supplemental cards generically;
* keeping Commander/Companion logic separate from supplemental decks;
* making **player-owned planar decks the target default mode** for this fork;
* retaining table-owned shared and merged-shared planar decks as alternative modes;
* making reveal/planeswalk operations explicitly carry the player whose action/effect caused the new planar card to be revealed;
* making the same infrastructure reusable for future Attraction decks and a Junkyard;
* and only after that adding more Plane and Phenomenon content.

Where this document says **must**, it describes either a rules requirement or a repository architecture decision.

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

Future player-owned decks, team variants, or Grand Melee must be able to provide additional deck/controller context without replacing the public semantics again.

### 2.5 Phenomena and multiple face-up cards

Phenomena use the same `PlanarCard` runtime boundary, with encounter triggers and the required follow-up state-based action.

Beginning-of-game Phenomena are skipped/bottomed without firing encounter triggers until a starting Plane is found.

Rules code must use collection-first face-up APIs. Do not reintroduce a singleton-current-Plane architecture.

### 2.6 Registry and bootstrap UI

`PlanarCardRegistry` is the stable catalog/factory boundary for implemented planar content. Network and deck-building layers should pass stable registry IDs rather than Java class names.

The current table-level shared-deck editor is a useful bootstrap, test, override, and fallback path. It is **not** the desired primary way for players to author planar packages long term.

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

## 3. Product planar-deck modes

The architecture must support three product modes without maintaining three separate Planechase rules engines.

### 3.1 Mode A — Player-owned planar decks (target default)

This is the **target default mode for this fork**.

Each player brings an independently built planar deck in that player's `.dck`:

```text
Player A -> Planar Deck A
Player B -> Planar Deck B
Player C -> Planar Deck C
Player D -> Planar Deck D
```

The decks remain separate throughout the game. They are not flattened or shuffled together.

The operation that reveals a new Plane or Phenomenon must know the **causing player**. The next planar card is taken from that player's planar deck.

Examples:

* Player A rolls the planeswalker symbol and the inherent trigger resolves -> reveal from Player A's planar deck.
* Player B resolves a spell/ability that instructs or causes a planeswalk -> reveal from the planar deck associated with Player B as the causing player for that instruction.
* A chaos ability causes a planeswalk or explicitly causes another planar card to be revealed -> propagate the player responsible for that resolving ability into the reveal operation.
* Merely causing `CHAOS_ENSUES` does not itself select a new deck unless the resulting rules text actually causes a new planar card to be revealed.
* A Phenomenon's automatic follow-up planeswalk must retain enough attribution from the reveal that encountered it to choose the intended player deck instead of guessing from the current active player or current Plane.

This requires the common planeswalk/reveal API to carry explicit context such as:

```text
PlanarRevealContext
├── causingPlayerId
├── sourceId / sourceAbilityId when applicable
├── planarDeckOwnerId
├── cause (PLANAR_DIE, SPELL_OR_ABILITY, PHENOMENON_SBA, OTHER)
└── previousRevealContext when attribution must survive a Phenomenon
```

Exact class/field names are not mandatory. The important rule is: **deck selection must be explicit and deterministic, never inferred from whichever Plane is currently face up.**

### 3.2 Mode B — Table-configured shared planar deck

This preserves the current shared-deck product model as an alternative mode.

One legal shared planar deck is selected/configured for the table, shuffled once through the game RNG, and used by all players.

The current Phase-7 table editor may continue to provide this mode.

### 3.3 Mode C — Merged shared contributions

This is a third optional mode.

Each player's `.dck` may contribute planar cards, then all contributions are merged, validated as one communal deck, and shuffled into one shared runtime deck.

```text
Player A contribution ─┐
Player B contribution ─┤
Player C contribution ─┼-> merged communal deck -> validate -> shuffle
Player D contribution ─┘
```

This mode is useful, but it is **not the default architecture** and must not be used as the semantic basis for player-owned decks.

## 4. Pregame starting-Plane procedure for player-owned mode

The target default mode needs an explicit setup step because there is no single shared deck from which the starting Plane can simply be drawn.

Timing:

```text
players/decks accepted
        ↓
play / turn / draw order determined
        ↓
starting-planar-deck chooser selected
        ↓
chooser selects whose planar deck supplies the starting Plane
        ↓
starting Plane setup completes
        ↓
mulligans
```

This selection therefore happens **after play/draw/turn order has been determined but before mulligans**.

### 4.1 Choosing the chooser

Select one eligible player randomly.

The player who determined the play/draw/turn order for that game is excluded from this random chooser selection.

The implementation must represent this as a distinct setup role/event rather than overloading `startingPlayerId` or planar controller.

Suggested state naming:

```text
initialPlanarDeckChooserId
initialPlanarDeckOwnerId
```

### 4.2 Choosing the starting planar deck

The randomly selected chooser chooses one player's planar deck to supply the starting planar card.

The chosen deck is shuffled normally before use.

Beginning-of-game traversal then follows the established Planechase setup behavior for that chosen deck:

* turn up the top planar card;
* if it is a Phenomenon, bottom it without firing its encounter ability;
* continue until a Plane is found;
* make that Plane the starting face-up Plane.

The selected starting deck does **not** become a global shared deck. It only supplies the initial Plane. Future reveals use the causing-player context described in section 3.1.

## 5. Rules invariants that future work must preserve

### 5.1 Planar card location

Plane and Phenomenon cards remain in the command zone throughout the game, whether face down in a planar deck or face up.

A planar deck is an ordering/association structure, not a separate Magic zone.

### 5.2 Planar controller and deck ownership

Dynamic planar control and underlying planar-deck ownership are separate concepts.

In player-owned mode, every planar card retains the player/deck association established at game start. Bottom placement returns a walked-away card to its associated owner deck.

In shared modes, shared-deck ownership semantics are handled by the shared-mode rules/context rather than by destroying the runtime object's stable deck association model.

### 5.3 Voluntary roll

The inherent voluntary planar roll remains a special action available only under the correct timing conditions. Its escalating cost counts previous uses of that special action, not arbitrary planar die rolls caused by effects.

### 5.4 Chaos

A chaos result and card text that says chaos ensues use the same semantic path.

The architecture must retain the ability to represent chaos ensues for a particular planar object.

If resolving chaos behavior causes a planeswalk or another planar reveal, the reveal operation must receive the player responsible for that result.

### 5.5 Planeswalking and reveal attribution

The common planeswalk operation is responsible for:

1. receiving an explicit `PlanarRevealContext` or equivalent;
2. identifying the applicable planar deck from that context and the current mode;
3. snapshotting applicable face-up planar cards;
4. turning them face down and returning them to the proper associated deck bottoms;
5. turning up the actual top card of the selected deck;
6. preserving exact walked-away and walked-to identity;
7. preserving the causing-player attribution if the newly revealed object is a Phenomenon;
8. emitting the correct lifecycle events;
9. allowing normal trigger/APNAP processing;
10. continuing through normal priority and state-based actions.

### 5.6 Hidden information

Every planar deck order remains hidden except where rules explicitly reveal information.

Player-facing gameplay shuffles through the game RNG. Deterministic known order is a test/injection seam only.

## 6. Generic supplemental-deck architecture

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

## 7. `.dck` storage and pregame staging

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

## 8. Pregame classification and Commander integration

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

## 9. Main-deck legality

Supplemental cards may be serialized in the pregame/sideboard section, but they are illegal in the normal main deck.

Required behavior:

* normal Add/double-click in the deck editor routes a Plane or Phenomenon directly to its supplemental group;
* drag/drop into main is rejected with a clear message;
* manually edited/imported `.dck` files with supplemental cards in main are marked illegal;
* server validation is authoritative even if a modified client bypasses UI restrictions;
* invalid cards are never silently removed from the library to make a malformed deck appear valid.

`isExtraDeckCard()` may assist counts/rendering, but it is not the sole legality rule.

## 10. Game initialization handoff

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

## 11. Deck editor UX

The normal deck editor becomes the primary place to build the planar package associated with a Commander deck.

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

## 12. Table UX and mode selection

Once player-owned mode is implemented, the normal table UX should make the modes explicit:

```text
[x] Planechase

Planar deck mode:
(o) Player-owned decks          [default]
( ) Table shared deck
( ) Merge player contributions into one shared deck
```

For player-owned mode, the lobby/table can show only legality/status information:

```text
Player A   planar deck 10   ✓
Player B   planar deck 10   ✓
Player C   planar deck 10   ✓
Player D   planar deck 10   ✓
```

The starting planar-deck chooser is selected only after game order is finalized, not in the lobby.

For table-shared mode, expose the existing shared-deck editor/override.

For merged-shared mode, show contribution totals and final communal legality. Do not silently merge table overrides with player contributions.

## 13. Future multiplayer Planechase variants

Do not bake four-player Commander assumptions into the Planechase engine.

Future modes may require:

* team-aware planar-controller selection;
* several planar controllers at once;
* several simultaneously applicable planar decks;
* different ownership/departure rules;
* different definitions of the player responsible for a reveal;
* different starting-planar-deck selection procedures.

Therefore public Planechase operations should take a context object or resolver input rather than depending on a single global shared-deck UUID.

The player-owned default in this document is a product mode, not a justification to hard-code `causingPlayerId == activePlayerId` or `deckOwnerId == planarControllerId` throughout the engine.

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

### Phase 9 — Deck Editor UX and `.dck` Persistence

Make the normal deck editor the primary authoring surface.

Implement searchable Planes/Phenomena, automatic routing to Planar Deck, grouped pregame rendering, separate counts, legality diagnostics, save/load/import/export round trips, explicit main-deck rejection, and generic supplemental grouping that can later display Attraction Deck.

Do not require a separate planar-deck file or assignment mapping.

### Phase 10 — Player-Owned Planar Deck Gameplay (target default)

Implement the target default mode described in sections 3.1 and 4.

Required work:

* retain one independently shuffled planar deck per player;
* determine normal play/draw/turn order first;
* before mulligans, randomly select the starting-planar-deck chooser from eligible players while excluding the player that determined the play/draw/turn order;
* let that chooser select which player's planar deck supplies the starting Plane;
* perform beginning-of-game Phenomenon skipping/bottoming on that selected deck until a Plane is found;
* store explicit causing-player/deck-owner context for every later planeswalk or planar reveal;
* reveal from the planar deck of the player who caused that reveal/planeswalk instruction;
* propagate reveal attribution through a Phenomenon so its automatic follow-up planeswalk does not lose the intended deck source;
* return walked-away cards to their proper associated owner deck bottoms;
* preserve independent hidden order, rollback, reconnect, serialization, and player-departure behavior for every player deck;
* add focused tests for planar-die planeswalks by each player and spell/ability-caused reveals by different players.

This phase is complete only when the game no longer needs to merge player planar packages to provide the normal/default experience.

### Phase 11 — Alternative Shared Modes and Variant-Ready Context

Add/finish the two shared alternatives without changing the player-owned semantics.

#### 11A — Table shared deck

Use one table-configured legal shared deck for all reveals. Reuse the existing bootstrap shared-deck UI and ordered runtime.

#### 11B — Merged player contributions

Merge PLANAR entries from submitted player decks, validate the final communal list, construct runtime PlanarCards, shuffle once, and use one shared deck.

Cross-player duplicate-name conflicts and communal Phenomenon limits must produce actionable validation errors.

Also harden the context APIs required by later Planechase formats so deck selection, planar control, causing-player attribution, ownership, and departure are not represented by one overloaded global UUID.

### Phase 12 — Incremental Plane and Phenomenon Content

Only after the deck-building, player-owned gameplay, alternative modes, and supplemental infrastructure are stable should broad content implementation become the focus.

Add additional Planes and Phenomena one card at a time.

Each card must:

* verify current Oracle text, Comprehensive Rules, official rulings/release notes where relevant, and current XMage mechanic patterns;
* use the common `PlanarCard` runtime and registry;
* have focused behavioral tests;
* have registry/metadata/deck-editor coverage;
* work in player-owned mode and both shared alternatives unless the card rules genuinely require a mode-specific restriction;
* preserve exact source/controller/causing-player semantics;
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

### 16.3 Player-owned mode

Test:

* all player planar decks are shuffled independently;
* order is determined before the starting-planar chooser step;
* chooser selection occurs before mulligans;
* excluded order-determining player cannot be selected as chooser;
* chooser can select any legal player's planar deck;
* starting Phenomena are bottomed without encounter triggers;
* starting setup stops on the first Plane from the selected deck;
* Player A's planeswalker roll reveals from A's deck;
* Player B's planeswalker roll reveals from B's deck;
* a spell/ability-caused planeswalk uses the explicitly attributed causing player's deck;
* a chaos ability that causes a reveal propagates the correct causing player;
* ordinary chaos with no reveal does not consume a planar card;
* a revealed Phenomenon's automatic follow-up preserves the reveal attribution needed for the next deck selection;
* walked-away cards return to their associated owner decks;
* one player's empty/invalid deck cannot silently fall back to another player's deck;
* rollback/reconnect preserves all independent orders and attribution state;
* player departure has deterministic documented behavior.

### 16.4 Shared alternatives

Test table-shared and merged-shared modes separately. Neither mode may mutate or flatten the player-owned mode's runtime state model.

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
* make merged-shared contributions the default player experience;
* infer the next planar deck from the current face-up Plane when a causing-player context is available;
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
7. Exact engine source for the player that "determined play/draw/turn order" and therefore must be excluded from initial planar chooser selection.
8. Exact chooser UI/event and disconnect fallback before mulligans.
9. Exact rules/engine mapping from a resolving spell or ability to the player considered responsible for a planar reveal.
10. How reveal attribution is persisted on/through a Phenomenon so the 704.6f follow-up uses the intended deck.
11. Player-departure behavior when face-up planar cards originated in the departing player's deck.
12. Whether empty planar decks are rejected before game start or need an explicit runtime failure policy.
13. How shared-mode ownership queries coexist with stable underlying player/deck association metadata.
14. Exact protocol enum/naming for the three modes: player-owned, table-shared, and merged-shared.
15. How future team/Grand Melee contexts provide causing-player and deck selection without overloading one UUID.
16. How future Attraction deck legality and Junkyard views plug into the generic supplemental UI without Planechase assumptions.

Until a question is resolved, choose the narrowest reversible design consistent with the boundaries above.