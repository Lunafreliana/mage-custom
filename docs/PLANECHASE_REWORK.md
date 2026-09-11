# Planechase Rework

NO matter what you do never download Maven dependancies. Outsource testing to GitHub

## 1. Purpose and authority

This document is the canonical architecture and migration plan for Planechase in this fork. Future Planechase engine, card, test, server, and client changes must start here and keep this document current when an implementation decision changes.

The goals are to correct the rules core, then incrementally add a real planar deck, multiple face-up planar cards, phenomena, content, and UI. **Preserving playability of the current Planechase implementation during that migration is not a requirement.** Correct boundaries and a clean end state take priority over compatibility with the existing simulation. It is acceptable to disable or temporarily leave Planechase incomplete between phases, provided ordinary non-Planechase games remain unaffected and each phase is explicit about what is not yet available. The first implementation patch must remain deliberately small; it must not combine rules-core repair with every later data-model and content change.

This plan is based on:

* the checked-out `custom`-branch implementation listed in section 2;
* the official **Magic: The Gathering Comprehensive Rules effective August 7, 2026**, obtained from the current [Wizards rules page](https://magic.wizards.com/en/rules) (research performed September 11, 2026), especially rules 103.7, 108.3a, 116.2i, 311, 312, 408.3, 701.31, 704.6f, and 901;
* upstream XMage [PR #11316](https://github.com/magefree/mage/pull/11316), inspected only as an unfinished design reference. It must not be cherry-picked, merged, or presumed correct.

Where this document says **must**, it describes either a rules requirement or a repository architecture decision. Any temporary scaffolding is an implementation convenience, not a promise that legacy Planechase remains playable.

## 2. Current XMage architecture

### 2.1 Runtime representation and registry

`mage.game.command.Plane` (`Mage/src/main/java/mage/game/command/Plane.java`) is a `CommandObjectImpl`, not a `CardImpl`. It owns a `Planes planeType`, a mutable `controllerId`, an ability collection, token-repository image metadata, and copy/source fields. It currently:

* reports no `CardType`, subtype, or supertype;
* treats its zone-change counter as permanently `1`;
* has no owner, face-up state, revealed state, or planar-deck identity/order;
* reflectively constructs implementations from `mage.game.command.planes`;
* provides `createRandomPlane()`, which uniformly selects an enum value with `RandomUtil`.

`mage.constants.Planes` (`Mage/src/main/java/mage/constants/Planes.java`) is a fixed registry of 21 implemented planes. It maps an enum value to a Java class name and display name. It is neither a deck list nor a complete registry of printed planar cards, and it has no phenomenon representation.

Keeping `Plane` command-object-based during the early phases is intentional. Converting every plane to `CardImpl` in Phase 1 would unnecessarily couple rules repair to card repositories, deck construction, serialization, images, views, and client work. The runtime abstraction must nevertheless evolve so it can eventually describe both planes and phenomena correctly.

### 2.2 Game initialization and planeswalking

In `mage.game.GameImpl` (`Mage/src/main/java/mage/game/GameImpl.java`):

1. When `GameOptions.planeChase` is true, `init` calls `Plane.createRandomPlane()`.
2. It assigns the starting player as controller, calls `addPlane`, and then marks `GameState` as Planechase.
3. `addPlane` rejects the new object if any `Plane` already exists in the command collection, explicitly enforcing a one-plane model.
4. Otherwise it copies the plane, assigns the supplied player as controller, gives the copy and its abilities new IDs, adds it to command, announces it, offers a replaceable `PLANESWALK` event, and fires `PLANESWALKED` if it was not replaced.

`mage.abilities.effects.common.PlaneswalkEffect` removes the one current plane, records its name in `seenPlanes`, repeatedly calls `Plane.createRandomPlane()` until it finds an unseen name (clearing `seenPlanes` after all enum values have appeared), and calls `Game.addPlane`. This approximates “do not repeat until all have appeared”; it does not put cards on the bottom or navigate an ordered deck.

The existing `PLANESWALK`/`PLANESWALKED` event pair may provide useful replacement/before-and-after boundaries. Its payload, controller, target identity, cancellation semantics, and behavior with several face-up planar cards must be reviewed before reuse. Names alone are not proof that the events model rules 701.31 and 901.11 correctly.

### 2.3 Game state

`mage.game.GameState` (`Mage/src/main/java/mage/game/GameState.java`) stores:

* `boolean isPlaneChase`;
* `List<String> seenPlanes`;
* planes indirectly in the general `Command` collection.

`getCurrentPlane()` scans command objects and returns the first `Plane`; it therefore assumes exactly one. The Planechase fields participate in copy, restore, restart, and state-value behavior only to the extent implemented there. There is no ordered planar deck, collection explicitly representing face-up planar cards, face-down planar-card state, deck owner/mode, or central planar controller.

`seenPlanes` is simulation bookkeeping, not a planar deck. It cannot express a known top card, bottom placement, ownership, reveals, individual decks, phenomena, or deterministic deck traversal.

### 2.4 Current planar roll ability, result handling, and cost

Every implemented `Plane` repeats essentially the same voluntary-roll infrastructure:

```text
ActivateIfConditionActivatedAbility (Zone.COMMAND)
  + MainPhaseStackEmptyCondition
  + GenericManaCost(0)
  + RollPlanarDieEffect(plane-specific chaos effects/targets)
  + mayActivate(ANY)
  + PlanarRollWatcher
  + SimpleStaticAbility(PlanarDieRollCostIncreasingEffect)
```

This makes the rules-provided roll look like an activated ability of the current plane. The repeated ability is available through each plane rather than once through the Planechase game rules.

`mage.abilities.effects.common.RollPlanarDieEffect` both rolls and interprets the result. It asks the controller to roll, then:

* applies the plane-specific effect list immediately for chaos; or
* immediately applies `PlaneswalkEffect` for a planeswalker result.

Consequences:

* chaos is coupled to a die-roll effect rather than modeled as “chaos ensues”;
* a spell or ability can only obtain the current plane's chaos behavior if that behavior is passed into the roll effect;
* the planeswalking inherent ability never triggers and never gives players a response window;
* voluntary and effect-generated rolls share too much machinery.

`mage.watchers.common.PlanarRollWatcher` counts `ROLL_DIE` events with a null numerical result per player each turn. `mage.abilities.effects.common.cost.PlanarDieRollCostIncreasingEffect` reads that count to increase the repeated plane ability's mana cost. This counts planar die rolls, not uses of the rules special action. It therefore cannot implement rules 116.2i/901.9 when a card effect also rolls the planar die.

The watcher is registered among `GameImpl`'s default watchers with a comment that the Planechase code needs improvement. It also supports current dice regression tests, so removal must be caller-audited rather than assumed safe.

### 2.5 Representative plane implementations

The following classes expose distinct migration risks:

* `PanopticonPlane`: defines a bespoke `PanopticonTriggeredAbility` for `PLANESWALKED`, examines `getCurrentPlane()`, and targets the active player. It demonstrates the need for a source-bound “planeswalk to this” trigger and correct planar control.
* `AcademyAtTolariaWestPlane`: implements active-player end-step behavior and packages `DiscardHandControllerEffect` into its roll effect. It demonstrates ambiguity between the active player and a stale source controller.
* `EdgeOfMalacolPlane`: includes the repeated roll/cost boilerplate and manually changes an ability's controller to `game.getActivePlayerId()` while applying an effect, then restores it. This is a direct planar-controller workaround that the central model must eliminate.
* `TazeemPlane`: uses a custom restriction with `getCurrentPlane()` and counts
  lands through a source-controller filter. It demonstrates why applicability
  and dynamic values must use the central controller/runtime rather than a
  singleton current-plane lookup.
* `AgyremPlane`: creates next-end-step delayed triggered abilities for cards that
  died. It demonstrates why delayed effects and source identity must survive
  migration, not merely why roll boilerplate should be deleted.

Migration must inspect each plane's functional behavior rather than mechanically replacing imports. Existing implementations are useful evidence, not a rules authority.

### 2.6 Game options, server, client, and content surface

`mage.game.GameOptions` has a `planeChase` boolean and deliberately defines a nine-sided historical die: one chaos side, one planeswalker side, and seven blanks. The rules die is six-sided (901.3a), but changing probabilities is explicitly deferred to a separate correction after the architecture is stable.

The match/server path carries one boolean from `MatchOptions` through `TableController` into `GameOptions`. `Mage.Client`'s `CustomOptionsDialog` exposes a single “Planechase” checkbox. There is no planar-deck editor, shared/individual deck choice, list validation, ordering display, phenomenon configuration, or deck selection protocol.

The test framework already offers deterministic hooks:

* `CardTestPlayerAPIImpl.addPlane(Player, Planes)` places a named test plane;
* `CardTestPlayerAPIImpl.setDieRollResult(TestPlayer, int)` queues a die result;
* `TestPlayer.rollDieResult` consumes that queued value in strict mode;
* `RollDiceTest` and `FracturedPowerstoneTest` contain existing planar/non-planar dice coverage.

Those hooks should be extended or wrapped for semantic planar results rather than tests relying on random retries or hard-coding the eventual physical die distribution.

### 2.7 `WillOfThePlaneswalkersEffect`

`mage.abilities.effects.common.WillOfThePlaneswalkersEffect` resolves a vote and directly invokes `PlaneswalkEffect` when planeswalking wins. It contains an explicit TODO to refactor once Planechase support is improved. The future implementation must decide whether its current Oracle action directly instructs a planeswalk or causes a symbol-equivalent event; it must not blindly route all non-die instructions through the die-result path. This caller belongs in the audit for the new reusable rules primitives.

## 3. Current rules model

This section records the rules constraints the architecture must preserve. Rule numbers refer to the Comprehensive Rules effective August 7, 2026.

### 3.1 Planar cards, decks, and the command zone

* A default Planechase game gives each player a supplementary planar deck of at least ten plane and/or phenomenon cards. At most two may be phenomena, and English names must be unique within a deck (901.3).
* The planar die is rules-wise six-sided: one planeswalker symbol, one chaos symbol, and four blanks (901.3a).
* Plane and phenomenon cards remain in the command zone throughout the game, whether in a planar deck or face up (311.2, 312.2, 901.4). A planar deck is therefore an ordered supplemental structure, not a Magic zone.
* Plane and phenomenon cards are not permanents, cannot be cast, and remain in command if an effect would move them out of it (311.2, 312.2).
* Only face-up planar cards normally have functioning abilities (311.4, 901.7); turning a face-up plane or phenomenon face down makes it a new object (311.6, 312.6, 901.7a).
* The optional single communal deck has at least forty cards or ten times the number of players, whichever is smaller; it has a phenomenon cap of twice the number of players and unique English names (901.15a). References to a player's deck use the communal deck (901.15c).
* Nontraditional/specially designated cards start in command (408.3). At game start, the starting player turns over cards from the top until finding a plane; encountered phenomena go to the bottom, and no abilities of cards turned face up during this setup trigger (103.7, 901.5).

The initial fork architecture will continue the current **single shared planar deck mode**, but implement it as a real ordered deck. The data model must not prevent later default individual-deck support.

### 3.2 Planar controller and ownership

The controller of every face-up plane or phenomenon is the designated planar controller. Normally that is the active player (311.5, 312.4, 901.6). The rules also specify continuity when that player would leave the game (901.6), a primary-player/team variation for Two-Headed Giant (901.12b-c), and potentially several planar controllers in Grand Melee (901.14).

Under the single planar deck option, the planar controller is considered the owner of every card in that deck (108.3a, 901.15b). Under individual decks, the owner is the player who began with the card in their deck (901.6). The engine must therefore distinguish underlying deck association/owner data from dynamic planar control, while applying the shared-deck ownership rule at rules-query boundaries.

The current fixed `Plane.controllerId` assigned on creation does not satisfy these rules. “You” and source-controller effects on face-up planar cards must observe the current planar controller without per-plane controller mutation hacks.

### 3.3 Rolling the planar die

Rolling under the inherent Planechase rule is a **special action**, not an activated ability. It does not use the stack. Only the active player may take it, while that player has priority, during a main phase of their turn, and only while the stack is empty (116.2i, 901.9).

Its cost is generic mana equal to the number of times that player previously took **this special action** that turn (116.2i, 901.9). Effect-generated rolls do not increment that count. Thus:

```text
first voluntary special-action roll    {0}
effect-generated roll                  no counter change
second voluntary special-action roll   {1}
third voluntary special-action roll    {2}
```

A blank has no Planechase consequence (901.9a). Chaos causes chaos to ensue (901.9b). A planeswalker symbol triggers the inherent planeswalking ability, which is put on the stack (901.8, 901.9c). A planar roll still causes general “roll one or more dice” triggers, but numerical-result effects ignore it (901.9d).

### 3.4 Chaos ensues

Every plane has an inherent chaos ability, “Whenever chaos ensues” (311.7). Chaos ensues when:

1. a player rolls chaos on the planar die;
2. a resolving spell or ability says chaos ensues; or
3. a resolving spell or ability says chaos ensues for a particular object.

In the third case, the relevant plane's chaos ability may trigger while that plane is revealed but still in the planar deck. The chaos ability is controlled by the current planar controller (311.7). Consequently, the event must be able to represent ordinary global chaos and, later, chaos for a particular planar object. A reusable direct effect is required; a die roll cannot be the only event producer.

### 3.5 Planeswalking

Planechase supplies a source-less inherent triggered ability: “Whenever you roll the Planeswalker symbol on the planar die, planeswalk.” It is controlled by the player whose roll caused it to trigger (901.8). A special-action planeswalker result triggers that ability and puts it on the stack, after which the active player gets priority (901.9c). The die action must not directly planeswalk.

Only the planar controller may planeswalk (701.31a). To planeswalk, put **each** face-up plane and phenomenon face down on the bottom of its owner's planar deck, then move the top card of the planeswalking player's deck off that deck and turn it face up (701.31b). After game start, doing so is planeswalking; relevant durations end and triggers fire (901.11). The turned-up plane is the plane walked to, and cards turned down/removed are those walked away from (701.31d, 901.11b). With multiple face-up planes, the player walks away from all of them (901.11c).

Planeswalking can result from the inherent trigger, a planar-card owner's departure, the phenomenon state-based action, or an instruction from a spell/ability (701.31c, 901.10-901.11a). These causes must share one operation while preserving different trigger/source/controller semantics.

### 3.6 Phenomena

A phenomenon is a nontraditional card type used only in Planechase (312.1). It remains in command, is not a permanent, cannot be cast, has no subtype, uses the planar controller, and becomes a new object when turned face down (312.2-312.6).

“To encounter” a phenomenon means moving it off a planar deck and turning it face up. Each phenomenon has an encounter trigger (312.5). Beginning-of-game handling is deliberately different: phenomena turned up while finding the starting plane are put on the bottom and no ability triggers (103.7, 901.5).

If a phenomenon is face up and is not the source of a triggered ability that has triggered but not yet left the stack, the planar controller planeswalks the next time a player would receive priority. This is a state-based action (312.7, 704.6f). The architecture must track the phenomenon object and its encounter ability on the stack; a timer or immediate second planeswalk is not equivalent.

### 3.7 Multiple face-up planar cards and multiplayer variants

The rules explicitly define planeswalking with more than one face-up plane (901.11c), and Grand Melee can have multiple face-up planes/phenomena and multiple planar controllers (901.14). Therefore no new engine API may assume a singleton current plane. Even if Grand Melee and individual decks are deferred, state must expose collections and associate each face-up object with its owning planar deck and relevant planar controller context.

## 4. Rules/implementation differences and known gaps

| Concern | Current implementation | Required/target behavior |
|---|---|---|
| Card location | A `Plane` is in command, but has no ordered deck membership or face state | All planar cards remain command-zone-associated; deck structure supplies order and face-up/down runtime state (311.2, 312.2, 901.4) |
| Starting plane | Random enum selection | Move the actual top card off the ordered deck; skip starting phenomena without triggers (103.7, 901.5) |
| Planar deck | `seenPlanes` plus repeated random selection | Ordered, shuffled supplemental deck with top/bottom operations |
| Content | 21 enum-backed plane implementations | Registry/runtime eventually covers Plane and Phenomenon; content added incrementally |
| Phenomena | No proper engine | Encounter triggers, setup handling, object tracking, and 704.6f SBA |
| Voluntary roll | Activated ability embedded in every plane | One rules-provided `SpecialAction` with exact availability (116.2i, 901.9) |
| Roll cost | Counts planar `ROLL_DIE` events | Counts prior uses of this special action by that player this turn |
| Effect-generated rolls | Indistinguishable for cost purposes | Process result identically but never increment voluntary-action count |
| Chaos | Plane-specific effects passed into `RollPlanarDieEffect` | `ChaosEnsuesEffect` emits a semantic event; reusable trigger listens |
| Planeswalker result | Immediately applies `PlaneswalkEffect` | Creates the source-less inherent trigger on the stack (901.8, 901.9c) |
| Planeswalking | Removes one plane, randomly creates another | Bottom all applicable face-up planar cards and turn up actual deck top (701.31b) |
| Planar control | Controller fixed when plane is added | Central, normally tracks active player; handles departure and variants (901.6) |
| “You” | Some planes target active player or mutate ability controller manually | Source-controller semantics naturally resolve to planar controller |
| Face-up planes | `getCurrentPlane()` returns one and `addPlane` rejects another | Collections of face-up planar cards/planes with identity/deck association |
| Events | `PLANESWALK`/`PLANESWALKED` exist | Audit/reuse or replace with documented payload and ordering contract |
| UI/options | Boolean checkbox only | Later shared deck selection, validation, visibility, and additional modes |
| Die distribution | Historical 9-sided 1/1/7 behavior | Rules say 6-sided 1/1/4 (901.3a); correct only in a later isolated task |
| Will of the Planeswalkers | Direct effect with refactor TODO | Audit Oracle semantics and route through the appropriate shared operation |

## 5. Lessons from upstream PR #11316

### 5.1 What is useful

PR #11316 is a two-commit, 35-file WIP opened in 2023. Its strongest architectural ideas are:

* **`RollPlanarDieSpecialAction`:** the voluntary roll belongs in game-level special actions rather than every plane.
* **Activation-count cost:** cost should derive from activations/uses of that special action, not all planar rolls.
* **Raw result separation:** renaming toward `rollPlanarDieResult` recognizes that random rolling and Planechase consequence processing are different responsibilities.
* **`ChaosEnsuesTriggeredAbility`:** plane implementations should declare semantic chaos triggers instead of supplying effect/target lists to a die effect.
* **`PlaneswalkToSourceTriggeredAbility`:** a shared source-bound trigger is the right replacement for bespoke plane-name/current-plane checks such as Panopticon's.
* **Plane migrations:** its edits are a useful inventory and migration sketch after the engine primitives and planar controller are correct.
* **Event vocabulary:** adding a semantic `CHAOS_ENSUES` event is directionally correct, and considering a planeswalker-result boundary is useful even though the proposed implementation is incomplete.

### 5.2 What must be rejected or rewritten

The PR is not production-ready and must not be merged wholesale:

* `PlaneswalkToSourceTriggeredAbility.checkEventType` and `checkTrigger` are TODOs returning false.
* Review identified an event/result inversion: chaos and planeswalker results were fired as the opposite event. The author attributed it to stale merge-conflict code. This confirms that the patch is a sketch, not trusted behavior.
* Its `RollPlanarDieSpecialAction` does not establish the complete active-player/priority/main-phase/empty-stack availability contract. `SpecialAction` inherits activated-ability machinery whose default timing is not sufficient by itself.
* Its placeholder rule string and effect/result handling are unfinished. The effect calls `rollPlanarDieResult` and discards the returned value, so neither semantic result is reliably completed.
* A `ROLLED_PLANESWALK` event alone does not implement rule 901.8. The source-less inherent triggered ability must be created, controlled by the roller, put on the stack, and resolve into planeswalking.
* Its `ChaosEnsuesTriggeredAbility` always accepts the event and does not solve “chaos ensues for a particular object” identity/filtering.
* Moving Plane effects to `SourceControllerEffect` remains wrong while plane controller IDs are stale.
* Broadly exposing activation counters on `ActivatedAbilityImpl` may be unnecessary. Prefer the narrowest stable action-use counter or API after inspecting copy/rollback/turn-reset behavior.
* It does not implement a real planar deck, top/bottom ordering, ownership, individual/shared mode semantics, multiple face-up planes, or phenomena.
* It does not fully solve planar control, departure rules, Two-Headed Giant, or Grand Melee.

Use its names and separation of concerns as design inspiration; rewrite and test the implementations against the current branch and current rules.

## 6. Target architecture

### 6.1 Ownership of responsibilities

The target should concentrate variant rules rather than distribute them through `Player`, `GameImpl`, and every plane:

```text
GameState
└── PlanechaseState
    ├── mode (initially SHARED; later INDIVIDUAL)
    ├── planar deck(s): ordered planar-card IDs
    ├── face-up planar-card IDs
    ├── planar-controller context
    └── per-player special-action use counts (or action activation state)

PlanarCard runtime abstraction
├── Plane (existing CommandObject model evolved incrementally)
└── Phenomenon (future)

Planechase rules primitives
├── RollPlanarDieSpecialAction
├── raw planar die roller / PlanarDieRollResult
├── PlanechasePlanarDieResultResolver
├── ChaosEnsuesEffect
├── ChaosEnsuesTriggeredAbility
├── inherent PlaneswalkingTriggeredAbility
├── PlaneswalkEffect / planeswalk operation
├── PlaneswalkToSourceTriggeredAbility
└── phenomenon encounter + SBA support
```

Exact class names may change, but the boundaries may not collapse:

1. randomness produces a raw semantic result;
2. Planechase rules resolve that result;
3. chaos is an independently producible semantic event;
4. the planeswalker result creates a stack-using inherent trigger;
5. planeswalking is a reusable operation over deck/order/face-up state;
6. planar control is resolved centrally.

`PlanechaseState` is a recommended cohesive home, not a requirement to create one giant mutable class. Any alternative must copy/restore/serialize/hash hidden state correctly and provide the same boundaries.

### 6.2 Identity and events

Events must carry semantic identities rather than requiring listeners to scan `getCurrentPlane()`:

* roller/player who caused a planar result;
* source ability when one exists (the inherent trigger itself has no source under 901.8);
* optional specific planar-card ID for “chaos ensues for [object]”;
* IDs of every object walked away from;
* ID of the object turned face up and the player who planeswalked;
* deck association needed for bottom/top operations.

Before retaining `PLANESWALK` and `PLANESWALKED`, write and test their event contract: when replacement checks occur, what `targetId`, `sourceId`, and `playerId` mean, whether multiple walked-away IDs require a batch/context object, and when “to” triggers are collected. Avoid introducing several ambiguous near-synonyms such as `ROLLED_PLANESWALK`, `PLANESWALK`, and `PLANESWALKED` without explicit lifecycle meanings.

## 7. Planar die flow

### 7.1 Voluntary action

`RollPlanarDieSpecialAction` must be installed once per eligible player/game, not once per plane. Its availability predicate must explicitly verify all of:

```text
game is Planechase
actor == active player
actor == priority player
current phase is that actor's main phase
stack is empty
actor can pay {N}
```

Here `N` is the number of prior successful uses of this special action by that actor this turn. The action itself does not use the stack. Its roll and immediate result processing happen as taking the special action, while any normal triggers caused by rolling wait to be put on the stack through normal trigger processing.

Count a use at the engine lifecycle point that matches “taken this action,” including correct behavior if cost payment/activation is declined or fails. Ensure action state survives game-state copy/rollback and resets per turn, including extra turns. Do not use `PlanarRollWatcher` as the new source of truth.

### 7.2 Raw result and resolver

Preferred flow:

```text
RollPlanarDieSpecialAction ─┐
card RollPlanarDieEffect ───┼─> raw PlanarDieRollResult
other rules effects ────────┘           │
                                       v
                         PlanechasePlanarDieResultResolver
                         ├── BLANK: no Planechase event
                         ├── CHAOS: ChaosEnsuesEffect/event
                         └── PLANESWALKER: create inherent trigger
```

A raw roller may continue to use the common dice engine so rule 901.9d triggers work, but should return a semantic enum rather than expose physical face numbers to callers. Numerical die-result replacement/effects must continue to ignore planar rolls as they do today. `RollPlanarDieEffect` for cards uses the same result resolver but does not touch the special-action counter.

Do not put Planechase consequence logic in the generic `Player.rollDieResult` path. Do not require the caller to pass the current plane's chaos effect list.

### 7.3 Compatibility with the nine-sided house rule

Phase 1 retains `GameOptions.PLANECHASE_PLANAR_DIE_*` probabilities exactly to isolate behavior changes. Tests should force `PlanarDieRollResult` outcomes (or use the existing deterministic integer queue behind a semantic helper) rather than assert frequency. A later, isolated patch may change one chaos/one planeswalker/seven blank to the rules-correct one/one/four and adjust UI/documentation/tests together.

## 8. Chaos ensues

Implement a reusable `ChaosEnsuesEffect` that causes the same semantic event whether invoked by a chaos die result or literal card text. It should accept/encode an optional particular planar-card reference so rule 311.7's revealed-card case is possible later.

Implement a reusable `ChaosEnsuesTriggeredAbility` for the inherent chaos ability of a plane. It must:

* function from command for the applicable face-up plane;
* trigger once for ordinary chaos on each applicable plane;
* trigger only for the specified object in object-specific chaos, including a revealed plane still in a deck;
* be controlled at trigger creation by the current planar controller;
* use normal triggered-ability stacking and targeting;
* preserve source object identity through copying/rollback/face-down new-object transitions.

Do not build a compatibility adapter that makes unmigrated plane roll wrappers respond to the new event. Such a bridge would preserve the coupling this rework is intended to remove and create a duplicate-trigger hazard. Phase 1 should use a purpose-built test fixture or one fully migrated pilot plane; other planes may remain unavailable or unsupported until Phase 3.

## 9. Planeswalking flow

### 9.1 Inherent triggered ability

A planeswalker die result must create a source-less `PlaneswalkingTriggeredAbility` controlled by the roller (901.8). That object goes through the normal triggered-ability queue and stack. Players receive priority and may respond before resolution (901.9c). On resolution it invokes the common planeswalk operation for the proper planar controller/deck context.

Because Phase 1 intentionally does not implement the real deck, its resolution-level test may use a narrow test seam or the existing `PlaneswalkEffect` solely to prove that resolving the inherent trigger invokes the planeswalk operation. This is not a requirement to keep the old random Planechase mode playable, and new code must not expose random selection as the target architecture.

Rule 901.10a says an inherent planeswalking ability on the stack ceases to exist if a plane leaves the game. This departure behavior must be included in the Phase 4/5 audit when real ownership and multiple face-up objects exist; decide whether Phase 1 can test it meaningfully under the compatibility model.

### 9.2 Common planeswalk operation

By Phase 4/5, one operation must implement rule 701.31b atomically:

1. verify the acting player is allowed to planeswalk in the current mode;
2. snapshot every applicable face-up plane and phenomenon;
3. turn each face down and put it on the bottom of its owner's associated planar deck;
4. move the actual top card of the acting player's applicable deck off its ordering and turn it face up;
5. record the walked-away set and walked-to identity;
6. end “until a player planeswalks” durations and emit clearly contracted before/after events;
7. allow encounter/planeswalk-to triggers to be collected under normal APNAP ordering;
8. run normal priority/SBA processing.

Do not choose a random implementation class during this operation.

### 9.3 Planeswalk-to-source trigger

`PlaneswalkToSourceTriggeredAbility` (or equivalent) must match the exact source ID against the walked-to ID in the completed event. It must not compare a plane enum/name or ask for a singleton current plane. It must capture the correct planar controller at trigger creation and remain valid even if control changes before resolution, according to normal XMage trigger/controller semantics.

## 10. Planar controller

Planar controller is a rules-engine concept, not a field assigned once when `addPlane` runs. Phase 2 must define one authoritative resolver/API used by:

* `Plane.getControllerId` and planar-card abilities while the command-object model remains;
* event and triggered-ability creation;
* source-controller effects and targets;
* planeswalk permission and deck choice;
* face-up phenomenon abilities;
* shared-deck rules ownership queries;
* player departure and turn transitions.

For ordinary current shared-deck games it normally resolves to `game.getActivePlayerId()`. It must update at the appropriate turn boundary before plane triggers for the new active player's turn are evaluated. If the controller would leave, transfer before departure as 901.6 requires. Do not scatter `ability.setControllerId(game.getActivePlayerId())` mutations through plane effects.

Design the API to accept planar-card/deck context even if Phase 2 has only one shared context. Grand Melee can have several planar controllers (901.14), and Two-Headed Giant changes “you” semantics (901.12b-c); a single global UUID is an implementation step, not a universal truth.

Required controller tests include turn change, extra turn, active-player departure, trigger controller captured correctly, “you” effects, and no stale original-creator controller. Team/Grand Melee tests may be deferred with explicit unsupported-mode guards.

### 10.1 Phase 2 implementation

The shared Planechase mode stores its authoritative planar controller in `GameState`. `Game.getPlanarControllerId(UUID)` accepts planar-card context even though the current shared mode resolves every such query to the same player; this parameter is the extension point for later individual-deck and Grand Melee contexts. `Game.setPlanarControllerId(UUID)` is the only engine operation that changes that value and synchronizes the current command-object `Plane` and its abilities so existing source-controller queries remain correct during the migration.

The controller is set before the initial plane is added, updated immediately after the active-player field changes and before turn-begin processing, and copied/restored with the rest of `GameState`. This covers normal turns and extra turns without relying on plane-local active-player mutations. If the planar controller leaves, control transfers to the active player or, when the active player is leaving, the next in-game player before departure processing removes objects that player controls. The shared plane is retained rather than discarded and randomly replaced.

While the single shared-deck option remains the only enabled Planechase model, owner queries for a `Plane` resolve through the same planar-controller API as required by rules 108.3a and 901.15b. Multiple simultaneous planar-controller contexts, Two-Headed Giant's primary-player semantics, and Grand Melee remain unsupported until their game modes can supply a context-aware resolver; callers must not infer that the stored shared UUID is a universal model for those variants.

## 11. Planar deck representation

### 11.1 No `Zone.PLANAR_DECK`

Do **not** create `Zone.PLANAR_DECK`. Rules 311.2, 312.2, and 901.4 keep all planar cards in command. Represent the planar deck as ordering metadata over command-zone-associated objects:

```text
Command zone association
  planar card A (face down)
  planar card B (face up)
  planar card C (face down)

PlanarDeck ordering
  top -> [A, C, ...] -> bottom

Face-up set
  [B]
```

A possible implementation is `Deque<UUID>`, but serialization libraries, deterministic copy/restore, iteration stability, reveal/look APIs, and hidden-information views must be evaluated before selecting the concrete collection.

### 11.2 Required invariants

* Each planar-card object is either in exactly one deck ordering or face up in command, except during an atomic transition.
* Deck order contains stable object IDs/references, never class names or display names.
* Every object retains an associated owner/deck needed for bottom placement.
* Shared mode provides one ordering; the model can later provide one per player.
* Shuffle uses the game RNG path and is deterministic under test facilities.
* Copies, rollback snapshots, restarts, reconnects, spectators, game logs, and network views preserve appropriate state without leaking hidden order.
* Bottoming several face-up cards must define the rules-compliant ordering/choice behavior after checking all applicable rules and card rulings during implementation; do not silently use hash/command iteration order.

`Plane.createRandomPlane()` and `seenPlanes` may remain as dead or isolated legacy code until Phase 4 removes them, but new Planechase paths must not call them merely to preserve old-mode playability. Reflection/`Planes` may remain temporarily as factories for implemented content, but must not define deck order.

## 12. Multiple face-up planar cards

Add collection-first APIs, for example:

```text
Collection<PlanarCard> getFaceUpPlanarCards()
Collection<Plane> getFaceUpPlanes()
Collection<Phenomenon> getFaceUpPhenomena()
```

The names and mutability may differ, but callers must not receive a mutable internal collection. New rules code iterates the collection. `GameState.getCurrentPlane()` may remain only as deprecated legacy API until its callers are migrated. It should fail clearly if more than one plane is present and must not silently select the first in new code.

Face-up status cannot be inferred merely from presence in `Command`, because face-down deck members are also command-zone cards. Store explicit runtime state or derive it from the invariant that face-up IDs are absent from deck order. The design must support multiple independent deck/controller contexts for Grand Melee even if that variant is not enabled immediately.

## 13. Phenomena design

Phenomena are excluded from Phase 1 but must fit the common planar-card runtime. The eventual design needs:

* card type `PHENOMENON` distinct from `PLANE`, with no subtype (312.1, 312.3);
* stable object identity, owner/deck association, face-up/down state, command-zone behavior, and planar controller;
* an `EncounterPhenomenonTriggeredAbility` tied to moving that exact object off a deck and turning it face up (312.5);
* a setup path that bottoms phenomena and suppresses all their triggers until a starting plane appears (103.7, 901.5);
* stack tracking sufficient to determine whether the face-up phenomenon is the source of a triggered ability that has triggered but not yet left the stack;
* an explicit 704.6f state-based-action check that planeswalks at the next priority point only after the relevant triggered ability has left the stack;
* correct behavior if the encounter trigger is countered, otherwise removed, copied, or accompanied by other triggers;
* tests for repeated phenomena, empty/invalid decks, departure, and several face-up planar cards.

Do not model the follow-up as a delayed trigger or unconditional tail effect of the encounter ability. It is a state-based action and must still occur when the original ability leaves the stack without resolving.

## 14. Migration and feature-gating strategy

1. Prefer a correctness-first cutover. Do not spend implementation effort preserving the current random-plane simulation or its per-plane roll abilities.
2. Add rules primitives and purpose-built tests before mass edits.
3. Do not introduce dual old/new roll or chaos paths. If a plane has not migrated, mark it unsupported or keep Planechase feature-gated rather than adapting its obsolete wrapper.
4. Establish central planar control before converting active-player hacks to ordinary source-controller effects.
5. Migrate planes in small audited groups, with focused tests for special triggers/delayed behavior. Do not regex-convert all classes.
6. New paths must stop calling random/seen selection. Legacy methods can remain temporarily unreachable until the ordered-deck phase deletes them.
7. Add collection APIs before enabling multiple face-up state; singleton helpers are deprecated migration debt, not supported architecture.
8. Extend network/view/options structures only when their phase needs them; serialization compatibility must still be reviewed for ordinary games and persisted/networked state.
9. Each phase must be internally testable and reviewable, but need not expose a playable Planechase mode. If an intermediate phase cannot provide rules-coherent Planechase, disable the option or fail clearly until its enabling phase.

Removal gates:

| Legacy component | Remove when |
|---|---|
| per-plane roll activated abilities | remove or make unreachable when Phase 1 installs the special action; delete remaining definitions during Phase 3 |
| `PlanarDieRollCostIncreasingEffect` for Planechase | no migrated plane/action caller needs it and dice callers are audited (Phase 3) |
| `PlanarRollWatcher` as Planechase cost truth | Phase 1 action counter passes effect-roll separation tests; remove class only after all non-Planechase/test callers are audited |
| `seenPlanes` and random selection | stop calling from new paths immediately; delete when the ordered shared deck passes traversal/copy/rollback tests (Phase 4) |
| `getCurrentPlane()` in rules code | prohibit in new rules code immediately; delete/deprecate after collection APIs are adopted (Phase 5) |
| controller mutation hacks | central planar controller and affected plane tests pass (Phases 2-3) |

## 15. Implementation phases

### Phase 1 — Planechase Rules Core

Implement:

* one `RollPlanarDieSpecialAction` per eligible player/game;
* exact active-player, priority, main-phase, and empty-stack availability;
* activation/use-count-based `{0}`, `{1}`, `{2}` escalating cost;
* raw `PlanarDieRollResult` separated from Planechase result resolution;
* effect-generated planar rolls that share resolution but not action count;
* `ChaosEnsuesEffect`, semantic `CHAOS_ENSUES`, and reusable `ChaosEnsuesTriggeredAbility`;
* the source-less inherent planeswalking triggered ability;
* planeswalker-result behavior that uses the stack, then invokes the isolated planeswalk operation/test seam on resolution;
* focused deterministic engine tests and a purpose-built fixture or one fully migrated pilot plane;
* feature gating that prevents users from entering a knowingly incoherent mixture of old and new Planechase rules, if Phase 1 cannot provide a coherent playable mode.

Do **not** implement the real planar deck, phenomena, central controller overhaul, mass plane migration, or die probability change.

### Phase 2 — Planar Controller

Implement authoritative planar-controller resolution/update behavior, wire plane ability/source-controller queries to it, handle normal turn/extra-turn/departure transitions, and add focused tests. Remove the architectural need for temporary active-player/controller mutation, but migrate individual hacks in Phase 3. Explicitly guard/defer unsupported Two-Headed Giant and Grand Melee semantics rather than pretending one UUID solves them.

### Phase 3 — Existing Plane Migration

Audit and migrate all existing `mage.game.command.planes` classes away from:

* `ActivateIfConditionActivatedAbility` planar-roll boilerplate;
* plane-owned `RollPlanarDieEffect` chaos lists;
* `PlanarRollWatcher`-based cost behavior;
* `PlanarDieRollCostIncreasingEffect`;
* bespoke current-plane/name triggers where `PlaneswalkToSourceTriggeredAbility` applies;
* manual active-player/controller hacks.

Add/update focused tests for every non-mechanical conversion, especially Panopticon, Academy at Tolaria West, Edge of Malacol, and Tazeem. Do not add compatibility adapters for the old per-plane roll model.

#### Phase 3 execution specification

Phase 3 is a bounded content migration over the 21 classes currently registered in
`mage.game.command.planes`; it is not another rules-core or planar-deck phase.
`FieldsOfSummerPlane` is the Phase 1 pilot and already uses
`ChaosEnsuesTriggeredAbility`. The other 20 classes still contain the complete
legacy roll wrapper (activated ability, zero-mana cost, watcher, `mayActivate(ANY)`,
and cost-increasing static ability). The migration must leave **no** plane-owned
way to roll the planar die. The game-level special action and card-generated
`RollPlanarDieEffect` are the only roll producers after this phase.

For each of the 20 legacy classes, perform this sequence deliberately rather than
as a repository-wide textual replacement:

1. Recheck the current Oracle text and the official release notes/card rulings.
   Treat the old implementation and upstream PR #11316 only as implementation
   leads, especially where the old comment paraphrases rather than quotes Oracle.
2. Preserve the non-chaos abilities unless the controller migration exposes an
   actual correctness defect. Replace only the roll wrapper with one
   `ChaosEnsuesTriggeredAbility` holding the printed chaos effects and targets in
   their resolution order.
3. Change effects phrased with "you" to ordinary source-controller effects or
   filters. Phase 2 makes the source controller authoritative; do not query the
   active player and do not temporarily mutate an ability's controller.
4. Attach targets to the new triggered ability. Do not retain parallel
   `List<Effect>`/`List<Target>` structures, null target placeholders, or duplicate
   target instances merely to satisfy the removed `RollPlanarDieEffect` API.
   Confirm that every effect uses the intended target pointer.
5. Delete the obsolete imports and all per-plane instances of
   `ActivateIfConditionActivatedAbility`, `MainPhaseStackEmptyCondition`,
   `GenericManaCost`, `RollPlanarDieEffect`, `PlanarRollWatcher`, and
   `PlanarDieRollCostIncreasingEffect`.
6. Add a focused deterministic chaos test before moving to the next audited
   group. Assert that the trigger is put on the stack exactly once, that its
   controller is the planar controller captured when it triggers, that targets
   belong to the trigger, and that the printed result occurs only on resolution.

Use the following inventory as the required checklist. “Straight” means that the
existing chaos effect can probably be moved under the semantic trigger, not that
the class may be changed without Oracle/rulings review.

| Migration group | Planes | Required attention beyond wrapper removal |
|---|---|---|
| Pilot/already semantic | Fields of Summer | Retain as a regression fixture; its non-chaos `getCurrentPlane()` guard remains Phase 5 singleton debt, not a reason to restore the old roll path. |
| Straight, no chaos target | Academy at Tolaria West; Agyrem; Edge of Malacol; Hedron Fields of Agadeem; Panopticon; Tazeem; The Dark Barony; The Eon Fog; The Great Forest; Trail of the Mage-Rings; Truga Jungle; Turri Island; Undercity Reaches | Replace null-target wrapper plumbing with the semantic trigger. Verify every "you" effect uses the planar controller. Several classes also have special risks listed below. |
| Targeted chaos | Akoum; Astral Arena; Bant; Feeding Grounds; Lethe Lake; Naya; The Zephyr Maze | Put the target on `ChaosEnsuesTriggeredAbility`, preserve effect order and target-pointer sharing, and test target selection/legality at trigger stacking and resolution. |

The four mandatory deep audits are:

* **Panopticon:** first implement and test
  `PlaneswalkToSourceTriggeredAbility`. It must match the exact object ID carried
  by the planeswalk-to event, not scan `getCurrentPlane()` or compare a plane name.
  Define the current `PLANESWALK`/`PLANESWALKED` payload contract before relying on
  it, and keep the API compatible with Phase 4's ordered deck and Phase 5's
  multiple face-up cards. Replace the bespoke `PanopticonTriggeredAbility` only
  after positive, wrong-plane, and trigger-controller tests pass.
* **Academy at Tolaria West:** its end-step intervening-if and its chaos trigger
  both mean the planar controller by "you." Verify an empty-hand check both when
  the end-step ability would trigger and when it resolves. The chaos discard must
  affect the controller of the chaos ability, not whichever player happens to be
  active later.
* **Edge of Malacol:** remove the temporary `source.setControllerId(...)` dance.
  Its untap replacement must use the centrally resolved planar controller for
  "a creature you control" and must not leak controller changes if an event is
  rejected or processing exits early. Test the replacement during the planar
  controller's untap step, a noncontroller's untap step, and after control passes.
* **Tazeem:** migrate the chaos draw to a source-controller land count and verify
  that the value is calculated at resolution. Replace its custom singleton-based
  blocking restriction only with an engine-native effect whose lifetime and
  source identity are equivalent. Do not use `getCurrentPlane()` as an
  applicability shortcut in new code.

In addition, Agyrem requires an explicit delayed-trigger regression: a creature
that dies while Agyrem is face up must still return at the next end step as
instructed even if the game planeswalks away before then, with the correct owner,
destination, and object identity.

Also audit all remaining `getCurrentPlane()` callers in these classes. Phase 3
must remove callers used to identify a planeswalk-to source or to emulate the
planar controller. Other singleton applicability guards may remain only when
their removal genuinely depends on Phase 5's face-up collection/runtime work;
record each such caller as explicit Phase 5 debt rather than silently carrying it
forward.

Land Phase 3 in small reviewable groups. A recommended order is:

1. add/test `PlaneswalkToSourceTriggeredAbility` and migrate Panopticon;
2. migrate simple untargeted chaos abilities;
3. migrate targeted and multi-effect chaos abilities;
4. migrate Academy at Tolaria West, Edge of Malacol, and Tazeem with their
   controller/condition tests;
5. run the complete Planechase and general dice regression sets, then audit the
   repository-wide callers of both legacy cost classes.

Phase 3 is complete only when all of the following are true:

* all 21 planes declare semantic chaos abilities and none owns a planar-roll
  activated ability;
* a repository search finds no legacy roll-wrapper/cost/watcher reference under
  `mage.game.command.planes`;
* Panopticon uses a tested object-identity planeswalk-to trigger;
* no plane mutates an ability controller to impersonate the active player;
* every migrated plane has focused behavioral coverage, with explicit coverage
  for target timing, controller snapshots, exact-once chaos, and delayed/duration
  identity where applicable;
* `PlanarDieRollCostIncreasingEffect` is deleted if its caller audit is empty;
  `PlanarRollWatcher` is deleted only if its separate non-Planechase and test
  caller audit is empty; and
* random/`seenPlanes` traversal, the nine-sided probability, planar-deck state,
  phenomena, and multiple-face-up support remain out of scope for this phase.

### Phase 4 — Real Shared Planar Deck

Implement one ordered, shuffled supplemental planar deck of IDs/references while every planar card remains command-zone-associated. Add deterministic deck injection/shuffle/order test APIs. Replace initialization and `PlaneswalkEffect` random/seen behavior with actual top/bottom traversal. Model association/ownership for shared mode and apply 901.15 ownership semantics. Test copy, rollback, restart, departure, hidden views, reconnect/serialization, exhaustion/cycling, and event order.

Do not create `Zone.PLANAR_DECK`.

#### Phase 4 implementation

The shared deck is stored in `GameState` as `SharedPlanarDeck`. Its face-down
ordering contains copied plane objects with stable IDs; `getOrder()` exposes an
immutable ID-only snapshot so callers cannot inspect the hidden objects. The
single face-up plane is absent from that ordering and remains in `Command`.
Planeswalking removes that same object from the face-up command collection,
puts it on the bottom, and turns the actual top object face up without changing
its identity. Game-state copy, rollback restore, and restart respectively deep
copy, restore, and clear the ordering.

The implemented `Planes` registry supplies the initial shared deck and is
shuffled through `RandomUtil`, while `GameOptions.sharedPlanarDeck` and
`SharedPlanarDeck.setPlanes(..., false)` provide deterministic known-order
injection seams for integration and narrow unit tests. This is deck
construction only: traversal never uses a random plane factory or `seenPlanes`.
Shared-mode ownership continues to resolve through the authoritative planar
controller API described in Phase 2. A richer planar-card runtime, multiple
simultaneous face-up cards, and phenomena remain assigned to Phases 5 and 6.

### Phase 5 — Planar Card Runtime / Multiple Face-Up Planes

Introduce/evolve a common runtime abstraction for planes and future phenomena, including type, stable identity, associated deck/owner, face state, and abilities. Add collection-first face-up APIs and migrate engine callers away from singleton assumptions. Support bottoming/walking away from all applicable face-up planar cards and test multiple-face-up event/controller behavior. Keep the CommandObject model unless implementation research demonstrates a concrete blocker; do not mass-convert to `CardImpl` by default.

#### Phase 5 implementation

`PlanarCard` is the common command-zone runtime contract for planes and future
phenomena. It exposes the planar card type, stable command-object identity,
planar-deck association and owner metadata, and explicit face state. `Plane`
implements that contract and now reports the `PLANE` card type. Turning a
face-up planar card face down advances its object-change counter without moving
it to a different Magic zone.

`GameState.getFaceUpPlanarCards()` is the authoritative collection-first API;
`getFaceUpPlanes()`, `hasFaceUpPlane(...)`, and `isFaceUpPlanarCard(...)` are
typed/query conveniences. The old singleton `getCurrentPlane()` API has been
removed, and plane rules, server diagnostics, and tests now use these
collection APIs. This allows zero, one, or multiple simultaneous face-up
planar cards without making command-zone ordering observable.

`SharedPlanarDeck` now stores the common runtime type and gives every stored
object a stable deck ID. Planeswalking snapshots and bottoms every face-up
planar card, removes all of their active continuous/trigger registrations, and
then turns the actual top card of the shared deck face up. The `PLANESWALK` and
`PLANESWALKED` events identify the exact destination object in `targetId` and
the planeswalking player in `playerId`; source-bound "planeswalk to" triggers
therefore compare the event target with their own source ID.

### Phase 6 — Phenomena

Implement phenomenon runtime content, encounter triggers, beginning-of-game skip/no-trigger logic, triggered-ability source tracking, and the 704.6f/312.7 state-based action. Add deterministic ordered-deck tests for resolution, countering/removal from stack, setup, multiple face-up cards, and controller/departure behavior before adding broad phenomenon content.

#### Phase 6 implementation

`Phenomenon` implements the common `PlanarCard` contract with phenomenon card
type, no subtype, explicit face state, stable deck/owner metadata, command-zone
abilities, and object-change counting when turned face down. Mutual Epiphany is
the pilot runtime content and uses the reusable source-bound
`EncounterPhenomenonTriggeredAbility`.

At setup, `GameImpl` turns each leading phenomenon face up and then bottoms it,
but suppresses ability registration and encounter events until it finds the starting plane. During play,
turning a phenomenon face up emits `ENCOUNTERED_PHENOMENON` for that exact
object. The Planechase state-based-action check examines both waiting triggers
and noncopy stack abilities with the phenomenon as source; only after its
trigger leaves both locations does rule 704.6f invoke the shared planeswalk
operation. This deliberately handles resolution, countering, and other stack
removal identically rather than attaching planeswalking to the encounter
effect.

The temporary `GameOptions.sharedPlanarPhenomena` construction seam puts
phenomena before configured planes so deterministic setup/traversal can be
tested without prematurely defining Phase 7's mixed planar-deck protocol.

### Phase 7 — Content and UI

Add missing Planechase content, including applicable MOC/WHO planes and phenomena, only after their mechanics have focused tests. Add shared planar-deck selection/editor/validation and appropriate server protocol and game views. Later evaluate individual planar decks, Two-Headed Giant specifics, and Grand Melee/multiple-controller UI. An auto-generated legal shared deck may be offered as a convenience, but it must use the real deck model rather than emulate the legacy random-plane mode.

### Separate follow-up — Planar die probability correction

In an isolated patch after Phase 1 stability, change the historical nine-sided house rule to the six-sided distribution in 901.3a, unless product owners explicitly retain it as a labeled configurable house rule. Update constants, semantic test helpers, UI wording, and regression tests together. Do not mix this probability change into the architecture patch.

## 16. Testing strategy

### 16.1 General principles

* Tests must be deterministic. Use/extend `setDieRollResult`, semantic result injection, named plane fixtures, and deterministic deck order; never loop until randomness yields an outcome.
* Prefer tests at the narrowest engine boundary plus end-to-end tests through the player action interface.
* Verify negative availability, event count, stack presence, priority windows, source/controller identity, and state after resolution—not only final visible outcomes.
* Retain non-Planechase dice regressions because planar rolls deliberately participate in some general die triggers but have no numerical result under 901.9d.
* Every new state component needs copy/restore/rollback and restart coverage. Ordered hidden data needs serialization/view-leak coverage.
* GitHub Actions is the authoritative Maven/JDK 17 validator under repository policy; implementation PRs must include focused `Mage.Tests` coverage.

### 16.2 Mandatory Phase 1 matrix

1. Special action unavailable during upkeep, draw, combat, end step, and another player's turn.
2. Unavailable when the actor lacks priority.
3. Unavailable while the stack is nonempty.
4. Unavailable to non-active players.
5. Available in precombat and postcombat main phases with priority and an empty stack.
6. First voluntary roll costs `{0}`, second `{1}`, third `{2}`.
7. A card/effect-generated planar roll between voluntary rolls does not increment cost.
8. Declining/failing an action or cost does not incorrectly increment count.
9. Count resets on the player's next turn and behaves correctly for extra turns.
10. Blank produces no chaos or planeswalking consequence and returns priority normally.
11. Chaos result causes one semantic chaos event.
12. Direct `ChaosEnsuesEffect` causes the same trigger path without rolling.
13. The applicable plane chaos ability triggers exactly once and uses the correct source/controller.
14. Planeswalker result creates the inherent source-less planeswalking trigger.
15. Planeswalking has not occurred while that trigger is on the stack; players can respond.
16. Countering/removing that trigger prevents its planeswalk; resolving it invokes planeswalking.
17. A spell/ability planar roll also creates the correct inherent trigger without altering action count.
18. Existing “roll one or more dice” triggers observe planar rolls as required.
19. Numerical die-result effects continue to ignore the planar result.
20. Ordinary numerical dice, replacement effects, and dice tests remain unaffected.
21. Special-action/action-count state survives game-state copy and rollback.

Extend `Mage.Tests/src/test/java/org/mage/test/cards/rolldice/RollDiceTest.java` for cross-dice regressions or add a dedicated Planechase engine test package; retain `FracturedPowerstoneTest` as the key effect-generated-roll cost scenario.

### 16.3 Later-phase minimums

* **Phase 2:** turn/extra-turn controller transitions; active player leaves; “you” effects; trigger controller snapshot; no controller-mutation hacks.
* **Phase 3:** each implemented plane remains behaviorally covered; planeswalk-to source matching; chaos exactly once; delayed effects retain identity.
* **Phase 4:** known order, shuffle determinism, top/bottom cycling, all face-up cards bottom correctly, shared ownership, no premature repeats caused by random selection, rollback/reconnect/no hidden-order leak.
* **Phase 5:** zero/one/multiple face-up planes; collection API; walk-away-from-all; different deck associations; events carry exact IDs.
* **Phase 6:** start-game phenomenon skips with no trigger; encounter trigger stacks; SBA waits while its source trigger remains on stack; SBA occurs after resolve/counter/removal; next card traversal; multiple phenomena; departure.
* **Phase 7:** deck legality (size, uniqueness, phenomenon cap), client/server round-trip, invalid input, generated-default deck, and visibility.

## 17. Explicit non-goals

For the first architecture implementation patch (Phase 1), do not:

* create `Zone.PLANAR_DECK`;
* implement real deck ordering or phenomena;
* convert every `Plane` to `CardImpl`;
* migrate every plane class;
* change the historical nine-sided distribution;
* add WHO/MOC content or a planar deck editor;
* solve every multiplayer variant;
* cherry-pick or merge PR #11316;
* broadly redesign generic dice, actions, command objects, or card repositories beyond demonstrated needs.

For this documentation task specifically, no gameplay Java or tests are changed and no Java/Maven toolchain or dependencies are downloaded.

## 18. Open questions and required pre-implementation investigations

These do not block adoption of the architecture, but the responsible phase must resolve and document them before code lands:

1. **State shape:** dedicated serializable `PlanechaseState` versus smaller structures on `GameState`; either must support deep copy, rollback, restart, equality/value computation, and network serialization.
2. **Action count storage:** use protected activation bookkeeping, a Planechase-specific per-turn counter, or a dedicated action instance API. Confirm exact increment timing and copy/rollback behavior without widening generic APIs unnecessarily.
3. **Special-action installation:** one persistent action per player versus dynamically generated actions; confirm AI/action discovery, reconnects, player departure, controller availability, and duplicate prevention.
4. **Trigger pipeline:** the cleanest XMage representation of a rule-created source-less triggered ability and how to test/counter it without fabricating a source ID.
5. **Event contract:** retain/refine `PLANESWALK` and `PLANESWALKED`, or replace them. Define replacement semantics, batching, IDs, and ordering before implementing `PlaneswalkToSourceTriggeredAbility`.
6. **Feature gate:** which phase can first expose a rules-coherent Planechase option, and how earlier phases clearly disable unsupported legacy planes rather than bridging old and new chaos/roll paths.
7. **Shared ownership:** where to implement 108.3a/901.15b so owner queries are correct without destructively changing stable deck association needed for bottom placement.
8. **Bottom order:** when several face-up planar cards are put on deck bottoms simultaneously, identify all controlling rules/choice requirements and a deterministic UI/test representation.
9. **Face-down new objects:** how command-object IDs/zone-change counters should represent 311.6/312.6 without premature `CardImpl` conversion.
10. **Revealed planar cards:** representation and visibility for object-specific chaos under 311.7.
11. **Planar controller scope:** ordinary shared mode first, while leaving a clear extension for Two-Headed Giant's “you” rule and Grand Melee's multiple controllers.
12. **Player departure:** exact ordered-deck behavior, inherited trigger disappearance under 901.10a, phenomenon abilities under 901.10b, and event ordering.
13. **Test API:** introduce semantic `setPlanarDieRollResult` so tests do not depend on whether the compatibility die has six or nine physical sides.
14. **Registry/content:** evolve `Planes` or introduce a planar-card factory/metadata registry that accommodates phenomena and deck validation without requiring all runtime objects to become `CardImpl`.
15. **UI and hidden information:** which planar deck data is public, revealed, or hidden in game views/logs and how spectators/reconnects receive it.
16. **House-rule compatibility:** remove the nine-sided distribution or retain it only as an explicitly labeled option after the isolated rules correction.

Until an open question is resolved, choose the narrowest reversible implementation consistent with the rules and phase boundaries above. Do not use uncertainty as justification to restore singleton, random-selection, plane-owned-roll, or stale-controller architecture.
