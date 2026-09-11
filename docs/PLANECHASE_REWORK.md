# Planechase Rework

NO matter what you do never download Maven dependancies. Outsource testing to GitHub.

## 1. Purpose

This document is the canonical forward-looking architecture and implementation plan for Planechase in this fork.

It assumes the **current `custom` branch Planechase implementation as the baseline**. It is not a migration diary and does not compare against abandoned or historical implementations. Future Planechase engine, server, client, deck-building, validation, UI, and card-content work should build from the architecture described here.

The current system already provides the core Planechase runtime: semantic planar die handling, a game-level planar-roll special action, an ordered shared planar deck, a common `PlanarCard` runtime, multiple-face-up support, Phenomena infrastructure, stable planar-card registry IDs, shared-deck validation, and bootstrap shared-deck UI.

The remaining work is primarily about making that runtime a clean product surface:

* storing planar packages with normal player deck files;
* classifying pregame/supplemental cards generically;
* keeping Commander/Companion logic separate from supplemental decks;
* supporting shared and later individual planar-deck modes;
* making the same infrastructure reusable for future Attraction decks and a Junkyard;
* and only after that adding more Plane and Phenomenon content.

Where this document says **must**, it describes either a rules requirement or a repository architecture decision.

## 2. Current baseline

### 2.1 Runtime planar cards

Planechase runtime objects use the common `PlanarCard` abstraction and remain command-zone runtime objects rather than ordinary castable cards.

`Plane` and `Phenomenon` therefore remain rules-engine objects. They are not converted into normal `CardImpl` objects merely so the deck editor can display them.

A deck-building representation is allowed to be a lightweight card/proxy object, but that object exists only to carry stable metadata and supplemental-deck identity through the deck editor and `.dck` serialization. At game initialization it is consumed and converted into the correct variant runtime object.

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

Plane and Phenomenon cards remain rules-wise associated with the command zone. The planar deck is an ordered supplemental structure over those runtime objects.

Do **not** introduce `Zone.PLANAR_DECK`.

The current shared implementation uses `SharedPlanarDeck`, stable object identity, face-up planar-card collections, real top/bottom traversal, and game-state copy/rollback support.

### 2.4 Planar controller

Planar control is resolved centrally. Source-controller effects and triggers on face-up planar cards must use that central model rather than storing permanent per-card assumptions about the active player.

The current shared-mode controller representation is sufficient for the supported shared mode, but future individual decks, team variants, or Grand Melee must be able to provide additional deck/controller context without replacing the public semantics again.

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
    └── Planar package
```

The planar package therefore travels with the player's normal deck automatically.

## 3. Rules invariants that future work must preserve

### 3.1 Planar card location

Plane and Phenomenon cards remain in the command zone throughout the game, whether face down in a planar deck or face up.

A planar deck is an ordering/association structure, not a separate Magic zone.

### 3.2 Shared and individual planar decks

The architecture must support both:

```text
SHARED
all applicable planar contributions -> one communal deck
```

and later:

```text
INDIVIDUAL
Player A -> planar deck A
Player B -> planar deck B
Player C -> planar deck C
Player D -> planar deck D
```

The current runtime may expose only shared mode in the product UI, but deck association and controller APIs must not make individual mode impossible.

### 3.3 Planar controller and ownership

Dynamic planar control and underlying deck association are separate concepts.

Shared mode may apply shared-deck ownership semantics at rules-query boundaries. Individual mode must retain the player/deck association from game start.

### 3.4 Voluntary roll

The inherent voluntary planar roll remains a special action available only under the correct timing conditions. Its escalating cost counts previous uses of that special action, not arbitrary planar die rolls caused by effects.

### 3.5 Chaos

A chaos result and card text that says chaos ensues must use the same semantic path.

The architecture must also retain the ability to represent chaos ensues for a particular planar object.

### 3.6 Planeswalking

The common planeswalk operation is responsible for:

1. identifying the correct planar/deck context;
2. snapshotting applicable face-up planar cards;
3. turning them face down and returning them to the proper deck bottoms;
4. turning up the actual applicable deck top;
5. preserving exact walked-away and walked-to identity;
6. emitting the correct lifecycle events;
7. allowing normal trigger/APNAP processing;
8. continuing through normal priority and state-based actions.

### 3.7 Hidden information

Planar deck order must remain hidden except where rules explicitly reveal information.

Player-facing gameplay must shuffle through the game RNG. Deterministic known order is a test/injection seam only.

## 4. Architecture boundaries

| Concern | Required architecture |
|---|---|
| Runtime Plane/Phenomenon | `PlanarCard` command-object runtime |
| Deck-building Plane/Phenomenon | Supplemental deck-building carrier/proxy with stable ID |
| Planar deck | Ordered supplemental structure, not a Magic zone |
| Shared/individual modes | Same runtime abstractions with different deck association |
| Commander deck file | Existing main + sideboard/pregame serialization |
| Main-deck placement | Supplemental cards rejected as illegal |
| Commander detection | Operates only on Commander/Companion partition |
| Real gameplay sideboard | Must not contain supplemental cards after game setup |
| Attraction compatibility | Same generic pregame classification/extraction, separate Attraction runtime handler |
| Junkyard | Attraction-specific runtime concept, not a generic supplemental zone |

## 5. Generic supplemental-deck model

The deck-building and pregame layer should not be Planechase-specific.

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

Exact names may differ. The important boundary is that every supplemental deck-building entry has:

* a supplemental type;
* a stable content identity;
* enough metadata for display/search/save/load;
* and a variant-specific runtime handler.

The generic layer must **not** assume that all supplemental cards become command-zone objects.

Conceptually:

```text
.dck sideboard / pregame staging
            │
            ├── Commander / Companion
            │
            └── Supplemental entries
                 ├── PLANAR
                 │    -> PlanechaseSupplementalHandler
                 │    -> PlanarCard runtime / planar deck / command-zone association
                 │
                 └── ATTRACTION
                      -> AttractionSupplementalHandler
                      -> AttractionDeck runtime
                      -> future Junkyard behavior
```

Planechase and Attractions may share serialization, deck-editor grouping, classification, and pregame extraction without sharing gameplay rules.

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

For Commander, this serialized sideboard should be treated as a **pregame staging container**, not as proof that every contained card belongs to the real in-game sideboard.

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

Conceptually:

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

Conceptually:

```text
submitted Deck
      │
      ├── classify pregame contents
      │
      ├── normal main -> Player.library
      ├── Commander/Companion -> Commander setup
      └── supplemental -> typed variant handlers
```

For `PLANAR`, the handler resolves stable IDs through `PlanarCardRegistry` and constructs/associates runtime `PlanarCard` objects.

For future `ATTRACTION`, the same generic layer hands identities/cards to the Attraction subsystem. That subsystem owns Attraction deck construction, Visit behavior, and future Junkyard transitions.

The generic staging layer must not know Planechase or Attraction gameplay details beyond dispatch type.

## 10. Deck editor UX

The normal deck editor should become the primary place to build the planar package associated with a Commander deck.

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

The table-level shared-planar editor may remain as an advanced override/fallback path.

## 11. Shared planar-deck assembly

For shared mode, player deck files contribute planar entries to the communal deck.

Example four-player flow:

```text
Player A .dck -> PLANAR contribution A ─┐
Player B .dck -> PLANAR contribution B ─┤
Player C .dck -> PLANAR contribution C ─┼-> merge contributions
Player D .dck -> PLANAR contribution D ─┘
                                             │
                                             ├-> validate communal deck
                                             ├-> construct runtime PlanarCards
                                             └-> shuffle -> SharedPlanarDeck
```

The final communal deck is validated as one shared deck. Per-player contributions are not individually required to satisfy shared-deck minimums.

Cross-player duplicate English names that violate shared-deck legality make the final deck illegal and should produce actionable diagnostics identifying the conflicting card/contributions.

A player may contribute zero planar cards if the final communal deck is legal.

Gameplay order is always shuffled. Contribution order, `.dck` iteration order, registry order, or merge order must never define gameplay order.

Suggested table UX:

```text
[x] Planechase

Planar deck mode:
(o) Shared
( ) Individual                 [disabled until supported]

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

If a table override is selected, it replaces the submitted-deck source. Do not silently merge both sources.

Server validation runs after all submitted decks are known and before game start.

## 12. Individual planar-deck mode

Individual mode uses each player's own `PLANAR` package as that player's planar deck.

Requirements:

* validate each player's planar deck independently;
* create one runtime planar deck per player;
* retain original deck/owner association on every runtime planar card;
* planeswalking resolves against the correct applicable deck context;
* bottom placement returns walked-away planar cards to the correct owner deck;
* player departure uses true owner/deck associations;
* hidden order, rollback, reconnect, and visibility operate independently per deck;
* controller APIs accept deck/context rather than assuming one global shared deck.

Suggested UI once supported:

```text
[x] Planechase

Planar deck mode:
( ) Shared
(o) Individual

Player A   planar deck 10   ✓
Player B   planar deck 10   ✓
Player C   planar deck 10   ✓
Player D   planar deck 10   ✓
```

Shared and individual mode must use the same `PlanarCard` content registry and the same Planechase rules primitives. Only deck association/selection differs.

## 13. Future multiplayer Planechase variants

Do not bake four-player Commander assumptions into the Planechase engine.

Future modes may require:

* team-aware planar controller selection;
* several planar controllers at once;
* several simultaneously applicable planar decks;
* different ownership/departure contexts;
* additional UI for deck/controller association.

The rules-engine interfaces should therefore pass planar-card/deck context where needed instead of relying on a singleton global deck/controller forever.

Product UI may continue supporting only a narrow initial mode until those variants are implemented correctly.

## 14. Attraction/Junkyard compatibility

The supplemental-deck infrastructure introduced for Planechase must intentionally support future Attractions.

Shared infrastructure:

* deck editor discovery/search;
* `.dck` pregame serialization;
* supplemental type classification;
* main-deck rejection;
* per-group counts and validation diagnostics;
* Commander/Companion partitioning;
* pregame extraction;
* typed handoff to a variant runtime handler.

Attraction-specific infrastructure remains separate:

* Attraction deck legality;
* Attraction deck shuffling/order;
* Visit rules;
* Attraction ownership/control;
* Junkyard lifecycle and visibility;
* any command-zone interaction required by the rules.

Do not model Junkyard as a generic destination for all supplemental cards. It belongs to the Attraction subsystem.

Likewise, do not force Attractions to use `PlanarCard`, `PlanarCardRegistry`, `SharedPlanarDeck`, or Planechase controller semantics.

## 15. Implementation roadmap

The current `custom` branch is the baseline. Phases below describe remaining forward work only.

### Phase 8 — Generic Supplemental Deck Infrastructure

Build the reusable pregame/deck boundary for Planechase and future supplemental-deck types.

Implement:

* a generic supplemental-card classification contract;
* `SupplementalDeckType` or equivalent;
* a Planechase deck-building carrier/proxy backed by `PlanarCardRegistry` metadata;
* authoritative main-vs-pregame classification;
* server-side rejection of Planes/Phenomena in main;
* Commander validation partitioning so supplemental entries are not treated as Commanders or normal sideboard cards;
* game-init extraction before real `Player.sideboard` is finalized;
* typed handoff from `PLANAR` entries into the Planechase runtime;
* explicit extension points for future `ATTRACTION` entries.

Acceptance tests must cover:

* Plane/Phenomenon in main -> illegal;
* Commander + Companion + many planar entries -> Commander classification still correct;
* planar entries do not count toward the normal Commander deck size;
* planar entries never become Wish-accessible real sideboard cards;
* PLANAR IDs construct the expected runtime planar content;
* generic classification works without assuming every supplemental type is Planechase.

### Phase 9 — Deck Editor UX and `.dck` Persistence

Make deck-integrated supplemental construction the primary authoring flow.

Implement:

* searchable Plane/Phenomenon deck-building entries;
* automatic routing to the Planar group;
* main-deck drag/drop rejection;
* generic pregame grouping in the deck editor;
* separate Main/Commander/Planar counts;
* per-group legality diagnostics;
* save/load/import/export round-trip coverage;
* preservation of stable supplemental identity;
* support for future supplemental groups without redesigning the editor again.

The table-level editor remains an optional advanced override/fallback.

### Phase 10 — Shared Deck Assembly from Submitted Decks

Connect deck-integrated planar packages to shared Planechase.

Implement:

* collecting PLANAR contributions from submitted decks;
* merging contributions into one candidate communal deck;
* authoritative shared-deck validation after all players are known;
* duplicate-name diagnostics across contributors;
* construction through `PlanarCardRegistry`;
* mandatory gameplay shuffle;
* clear source selection between submitted-deck contributions and a table override;
* shared-mode contribution/status UI.

Do not require every player to contribute the same number of cards unless an explicit product rule is later introduced.

### Phase 11 — Individual Planar Deck Mode and Variant-Ready Context

Add individual planar decks using the same deck-integrated PLANAR package.

Implement:

* one planar deck per player;
* independent legality validation;
* owner/deck association throughout planeswalking;
* correct bottom/top selection per deck;
* player-departure behavior;
* independent hidden-state/rollback/reconnect support;
* mode UI for `Shared` vs `Individual`;
* context-aware controller/deck APIs suitable for later team or multi-controller Planechase variants.

This phase should leave the engine ready to extend to additional Planechase formats without replacing the core runtime again.

### Phase 12 — Incremental Plane and Phenomenon Content

Only after the deck-building, shared/individual mode, and supplemental infrastructure are stable should broad new planar content be added.

Implement additional Planes and Phenomena one card at a time.

Each card must:

* follow `docs/CARD_IMPLEMENTATION_LLM.md`;
* verify current Oracle text, rules, release notes, and rulings where relevant;
* use existing Planechase primitives rather than introducing card-local architecture;
* register stable metadata in the planar registry;
* include focused behavioral tests;
* include deck-editor/registry construction coverage;
* work in both shared and individual deck contexts where rules permit.

Mechanically similar cards may share reusable tested infrastructure, but broad batch conversion should not replace card-by-card rules review.

Phase 12 is intentionally the final roadmap phase so content growth does not drive foundational architecture.

## 16. Testing strategy

### 16.1 General

* Tests must be deterministic.
* GitHub Actions is the authoritative Maven/JDK validation environment.
* Do not download Maven dependencies in Codex Cloud solely for validation.
* Hidden deck order must not leak through views, logs, reconnects, or serialization.
* New state must support copy/rollback/restart where applicable.
* Server-side legality validation is authoritative; client validation is UX.

### 16.2 Supplemental/deck tests

At minimum cover:

* supplemental card in main -> illegal;
* supplemental card in pregame -> accepted by the correct handler;
* Commander and Companion recognition unaffected by supplemental entries;
* supplemental entries excluded from normal Commander size/color/banned/singleton calculations;
* supplemental entries absent from real `Player.sideboard` after setup;
* `.dck` round trip preserves supplemental type and stable ID;
* synthetic/future supplemental type can pass generic classification without Planechase casts;
* table override precedence is explicit;
* shared deck always shuffles for gameplay.

### 16.3 Shared-mode tests

Cover communal minimum/uniqueness/Phenomenon cap, cross-player duplicate diagnostics, zero-card contributors, registry construction, shuffle, hidden order, rollback, reconnect, and departure behavior.

### 16.4 Individual-mode tests

Cover independent validation, per-player order, owner/deck association, planeswalk-to-correct-deck behavior, bottom placement, player departure, copy/rollback/reconnect, and controller-context correctness.

### 16.5 Attraction-compatibility tests

The generic supplemental layer should have at least one synthetic/non-Planechase test proving that classification, grouping, extraction, and dispatch work without assuming `PlanarCard`, command-zone destination, or Planechase registry types.

## 17. Non-goals and guardrails

Do not:

* introduce `Zone.PLANAR_DECK`;
* convert runtime Plane/Phenomenon objects into ordinary castable cards just for deck editing;
* use `.dck` sideboard presence as proof that a card is a real gameplay sideboard card;
* let supplemental entries become Commander candidates;
* silently discard illegal supplemental cards from main;
* expose deterministic registry/list order as gameplay planar deck order;
* hard-code the generic supplemental layer to Planechase;
* make every supplemental runtime use the command zone;
* model Attraction Junkyard as a generic supplemental destination;
* add broad Plane/Phenomenon content before the remaining infrastructure phases are complete.

## 18. Open design questions

The responsible implementation phase should resolve these before code lands:

1. Exact Java shape of `SupplementalDeckCard` / `SupplementalDeckType`.
2. Whether Planechase deck-building carriers are generated proxy cards, repository-backed mockable entries, or another lightweight searchable representation.
3. The best point in the submitted-deck pipeline to partition pregame contents before `Player.useDeck` creates gameplay sideboard state.
4. How deck validation APIs expose separate Commander, Planar, and future Attraction error groups without duplicating parsing.
5. Exact serialization needed to preserve stable supplemental IDs while remaining compatible with existing `.dck` files.
6. How table overrides are represented and how override-vs-submitted-deck precedence is communicated in protocol/UI.
7. How shared-mode contribution diagnostics identify duplicate cards from different players without exposing hidden data that should remain private.
8. Exact context object/API used by future individual/team/Grand-Melee planar controller resolution.
9. Whether the historical planar die probability remains a labeled house rule or is replaced by the rules-correct distribution in a separate focused patch.

When resolving these questions, prefer the narrowest generic boundary that serves both Planechase and future supplemental systems without coupling their gameplay engines.