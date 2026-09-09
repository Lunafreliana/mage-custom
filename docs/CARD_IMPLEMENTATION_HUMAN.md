# How to Read an XMage Card Implementation

This guide is for a reader with little or no Java experience. It is not a Java course. Its goal is to help you decide whether a Codex-created card looks sensible. All links point to real files in this repository; [`CARD_IMPLEMENTATION_LLM.md`](CARD_IMPLEMENTATION_LLM.md) is the more technical companion.

## Card text is shorthand: rules research is mandatory

The words printed on a Magic card—and even its current Oracle text—do not always state every step the game performs. Magic often uses a short keyword like a label for a much larger section of the rules. In programming terms, seeing **“Vanishing 3”** or **“Time travel”** is like seeing a call to an existing function: the short line calls behavior defined somewhere else. You cannot safely rewrite that function by guessing from its name.

```text
visible card text:
    Time travel.

conceptually:
    executeTimeTravelRules(...)
```

The visible text is the label for a much larger rules procedure; it is not the procedure itself.

For example, vanishing covers entering with time counters, removing a counter at upkeep, and the triggered sacrifice connected to removing the last one. Time travel has exact rules about eligible suspended cards and permanents, ownership versus control, optional choices, adding or removing a time counter for each chosen object, and making the changes together. Reminder text may summarize this, but it is not the full rule. Face-down cards, copies, replacement effects, linked abilities, “dies,” layers, and objects changing zones have similarly important rules that may be invisible in one sentence of card text.

A sensible Codex workflow therefore looks like this:

1. Confirm current Oracle text on official [Gatherer](https://gatherer.wizards.com/). Do not trust an old printed version when it differs.
2. Circle every keyword, keyword action, ability word, and special rules term.
3. Read the relevant entries and cross-references in the **current** Comprehensive Rules linked from [Wizards' rules page](https://magic.wizards.com/en/rules).
4. Identify the exact set/product, then find its official Release Notes through [Wizards' Magic news](https://magic.wizards.com/en/news). When Release Notes exist, checking them is mandatory—not optional.
5. Search those notes for both the card's exact name and every unfamiliar keyword or keyword action on it; also check official card-specific rulings.
6. Compare several modern XMage cards and the shared engine class for that mechanic.
7. Check that the proposed code covers every choice, event, zone, timing rule, controller/owner distinction, and interaction the official rules require.
8. Only then implement it and write tests for the non-obvious behavior.

Release Notes are useful because they often explain a new mechanic in practical language, collect interaction details and edge cases, and place card-specific rulings beside the general mechanic explanation. These details can be easier to overlook in the much larger Comprehensive Rules. Release Notes deserve extra attention for crossover releases, Commander products, supplemental sets, and unusual mechanics.

This online research is required when a mechanic is unfamiliar or uncertain, when current XMage examples disagree or look old, and for face-down objects, copy/replacement/continuous effects, linked abilities, layers, zone changes, delayed triggers, Last Known Information, or unusual targets. [Scryfall](https://scryfall.com/) is useful as a secondary lookup for Oracle text and rulings, but Wizards' current Comprehensive Rules, Gatherer, and official Release Notes take priority if anything disagrees or remains unclear.

**Review warning:** “The Oracle text only says two words” is not a reason for a two-line implementation. But “the mechanic is complicated” is also not a reason to rebuild it: Codex should first find the existing XMage keyword/engine implementation. Ask Codex which official rule numbers it checked and which modern XMage examples it compared. Memory, reminder text, and one old XMage card are not sufficient evidence.

## 1. Basic Java vocabulary, using XMage

Consider this shortened code from [`LightningBolt.java`](../Mage.Sets/src/mage/cards/l/LightningBolt.java):

```java
public final class LightningBolt extends CardImpl {
    public LightningBolt(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{R}");
    }
    @Override
    public LightningBolt copy() {
        return new LightningBolt(this);
    }
}
```

| Term | Plain-English meaning in this codebase |
|---|---|
| **class** | A blueprint. `LightningBolt` describes what all Lightning Bolt card objects contain and do. |
| **object** | One actual thing made from a class—for example, one Lightning Bolt in one player's deck. |
| **method** | A named operation, such as `copy()` or `addAbility(...)`. Parentheses usually signal a method call. |
| **constructor** | The special method that builds an object. It has the class's name: `LightningBolt(...)`. |
| **parameter** | A named input accepted by a method/constructor: `ownerId` is a parameter. |
| **argument** | The actual value passed into that input. In `new DamageTargetEffect(3)`, `3` is an argument. |
| **variable** | A named place holding a value/object, such as `Ability ability`. |
| **field** | A variable belonging to an object or class, such as a card's `power`. |
| **public** | Other code is allowed to use this class/method. |
| **private** | Only this class can directly use it. Copy constructors are normally private. |
| **protected** | This class and related child/package code can use it; common in engine base classes. |
| **final** | For a class, it cannot be subclassed. For a variable, its reference cannot be reassigned. |
| **static** | Belongs to the class once, rather than separately to each card object. Shared filters are often static. |
| **extends** | Inherits the behavior of a parent. `extends CardImpl` says “start with XMage's normal card skeleton.” |
| **new** | Create a new object, such as `new TargetAnyTarget()`. |
| **return** | Send a result back. `copy()` returns the new copy. |
| **`@Override`** | A note to Java: this method deliberately replaces a method promised by a parent/interface. |
| **import** | Makes a class from another package available by its short name. |
| **UUID** | A unique identifier. XMage uses UUIDs to tell game objects and players apart. It is not a card name. |
| **enum** | A fixed menu of named choices, such as `CardType.INSTANT` or `Zone.HAND`. |
| **List** | An ordered collection that can grow or shrink. |
| **array** | A fixed-size collection; `new CardType[]{CardType.INSTANT}` is a one-item array. |
| **interface** | A contract saying what operations an implementation must provide. `Effect`, `Condition`, and `Predicate` participate in such contracts. |
| **abstract class** | A partly finished blueprint intended for subclasses, not direct construction. Many engine `...Impl` bases are abstract. |
| **Filter** | A description of which objects qualify, such as “creatures you control.” |
| **Predicate** | One small yes/no rule inside a filter, such as “is an Elf” or “you control it.” |

`this` means “this particular object.” A dot means “use something belonging to it”: `this.addAbility(...)` asks this card to add an ability.

## 2. A typical card, line by line

Here is the important structure of [`ManOWar.java`](../Mage.Sets/src/mage/cards/m/ManOWar.java):

```java
public final class ManOWar extends CardImpl {
    public ManOWar(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}");
        this.subtype.add(SubType.JELLYFISH);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        Ability ability = new EntersBattlefieldTriggeredAbility(
                new ReturnToHandTargetEffect());
        ability.addTarget(new TargetCreaturePermanent());
        this.addAbility(ability);
    }
}
```

* `public`: card-loading code may use it.
* `final`: nobody should build a more specialized class on top of this exact card.
* `class ManOWar`: this blueprint is named `ManOWar` and matches the filename.
* `extends CardImpl`: it inherits normal XMage card behavior.
* `ManOWar(UUID ownerId, CardSetInfo setInfo)`: when XMage creates the card, it supplies its owner and printing/set information.
* `super(...)`: asks `CardImpl` to initialize the common card parts. Here they say “creature” and mana cost `{2}{U}`.
* `subtype.add(JELLYFISH)`: adds the creature subtype. Subtypes are enum values rather than space-separated words: for example, the rules define **Time Lord** as one two-word creature type, represented by `SubType.TIME_LORD`.
* `power`/`toughness`: sets the printed 2/2 numbers. `MageInt` is XMage's number wrapper.
* `Ability ability = new ...`: creates the “when this enters” trigger and places the return-to-hand effect inside it.
* `addTarget(...)`: says that trigger selects a creature permanent.
* `this.addAbility(ability)`: attaches the completed trigger to the card.

At the bottom of the real file, a private copy constructor and `copy()` let XMage duplicate game state safely. Their presence is normal and important.

Spell cards already have a spell ability. [`LightningBolt.java`](../Mage.Sets/src/mage/cards/l/LightningBolt.java) therefore says `this.getSpellAbility().addEffect(...)` rather than `this.addAbility(...)`. Supertypes (for example legendary), loyalty, explicit colors, modes, additional costs, and choices appear only when the printed card needs them.

## 3. The core XMage building blocks

* **`CardImpl`** — the basic skeleton of an ordinary card.
* **Ability** — something a card can do or have.
* **Effect** — what actually happens when an ability resolves or applies.
* **Target** — the slot for what an ability aims at; it also checks legality.
* **Filter** — rules describing which game objects qualify.
* **Cost** — what must be paid to cast or activate something.
* **Trigger** — something that automatically notices an event and creates an ability.
* **Condition** — a requirement/question that must be true.
* **Watcher** — game memory that records something which happened earlier.
* **DynamicValue** — a number calculated from the game instead of fixed in code.
* **Duration** — how long an effect continues.
* **Zone** — where a card is: hand, battlefield, graveyard, and so on.
* **Predicate** — one small rule used inside a filter.
* **Replacement effect** — changes an event that would happen (“instead”).
* **Continuous effect** — a rule that keeps applying while its condition/duration lasts.
* **Mode** — one selectable branch of “choose one/two.”
* **Choice** — a non-target selection, such as a color or creature type.

## 4. Large lookup table

### What “turn face down” really means

Turning a permanent face down is much more than changing its displayed power
and toughness to 2/2. Its old identity is hidden while it is face down: its
printed name, mana cost, colors, supertypes such as Legendary, card types,
subtypes, and ordinary printed abilities must no longer describe the face-down
object. A card that only becomes 2/2 but still says “Human Soldier,” “Legendary,”
or shows its original ability has not been implemented correctly.

XMage's modern `BecomesFaceDownCreatureEffect` performs this complete conversion
in the engine's copy layer. In beginner-friendly terms, a “layer” is one stage in
the rules' ordered process for calculating what a game object currently looks
like. Face-down identity is established early, so later effects operate on the
correct hidden object. Reusing this engine effect is safer than having each card
manually erase a list of properties, where one forgotten property can leak the
original card's identity or break morph.

Cyber Conversion is a useful three-step example:

1. Start with the normal face-up creature.
2. Apply XMage's canonical face-down conversion, producing a nameless,
   colorless 2/2 creature with its old characteristics hidden.
3. Layer the card's special instruction on top, making that face-down object an
   Artifact Creature — Cyberman only while it stays face down.

If it turns face up, the canonical face-down conversion and the extra Artifact
and Cyberman characteristics stop applying, so the original card returns. The
older `BecomesFaceDownCreatureAllEffect` remains in the repository for legacy
multi-object implementations, but it generally should not be copied for a new
single-target card.

Most effects below live in [`Mage/src/main/java/mage/abilities/effects/common`](../Mage/src/main/java/mage/abilities/effects/common). Always open the class before assuming which constructor is correct.

| Code / class | Plain-English meaning | Typical use | Real example |
|---|---|---|---|
| `CardImpl` | Ordinary card skeleton | Most single-faced cards | [`LightningBolt.java`](../Mage.Sets/src/mage/cards/l/LightningBolt.java) |
| `SpellAbility` | The act of casting a card | Instants/sorceries and permanent spells | Bolt above |
| `SimpleActivatedAbility` | “Pay a cost: do an effect” | Ordinary activated ability | [`ProdigalSorcerer.java`](../Mage.Sets/src/mage/cards/p/ProdigalSorcerer.java) |
| `EntersBattlefieldTriggeredAbility` | Notices this entering | ETB effects | [`ManOWar.java`](../Mage.Sets/src/mage/cards/m/ManOWar.java) |
| `DiesTriggeredAbility` | Notices this dying | Death triggers | [`DoomedTraveler.java`](../Mage.Sets/src/mage/cards/d/DoomedTraveler.java) |
| `BeginningOfUpkeepTriggeredAbility` | Notices an upkeep beginning | Upkeep triggers | [`PhyrexianArena.java`](../Mage.Sets/src/mage/cards/p/PhyrexianArena.java) |
| `SimpleStaticAbility` | Rule that remains in force | Continuous rules/modifiers | [`GloriousAnthem.java`](../Mage.Sets/src/mage/cards/g/GloriousAnthem.java) |
| `LoyaltyAbility` | Planeswalker ability and loyalty change | `+1`, `-2`, etc. | [`JaceBeleren.java`](../Mage.Sets/src/mage/cards/j/JaceBeleren.java) |
| `GreenManaAbility` | Tap to add green mana | Green mana creatures | [`ElvishMystic.java`](../Mage.Sets/src/mage/cards/e/ElvishMystic.java) |
| `GainLifeEffect` | Controller gains life | “You gain N life” | [`HealingSalve.java`](../Mage.Sets/src/mage/cards/h/HealingSalve.java) |
| `LoseLifeTargetEffect` | Chosen player loses life | Life loss, not damage | [`SovereignsBite.java`](../Mage.Sets/src/mage/cards/s/SovereignsBite.java) |
| `DamageTargetEffect` | Deals damage to chosen object/player | Burn/ping effects | [`LightningBolt.java`](../Mage.Sets/src/mage/cards/l/LightningBolt.java) |
| `DamageAllEffect` | Damages every matching object | Board-wide damage | [`Pyroclasm.java`](../Mage.Sets/src/mage/cards/p/Pyroclasm.java) |
| `DrawCardSourceControllerEffect` | Controller draws cards | “Draw two cards” | [`Divination.java`](../Mage.Sets/src/mage/cards/d/Divination.java) |
| `DiscardTargetEffect` | Chosen player discards | Discard spells | [`MindRot.java`](../Mage.Sets/src/mage/cards/m/MindRot.java) |
| `DestroyTargetEffect` | Destroys chosen permanent | Creature/permanent removal | [`Murder.java`](../Mage.Sets/src/mage/cards/m/Murder.java) |
| `DestroyAllEffect` | Destroys all matching permanents | Board wipes | [`DayOfJudgment.java`](../Mage.Sets/src/mage/cards/d/DayOfJudgment.java) |
| `ExileTargetEffect` | Exiles chosen object | Exile removal | [`PathToExile.java`](../Mage.Sets/src/mage/cards/p/PathToExile.java) |
| `SacrificeEffect` | Makes player sacrifice | Edict effects | [`DiabolicEdict.java`](../Mage.Sets/src/mage/cards/d/DiabolicEdict.java) |
| `CreateTokenEffect` | Creates token objects | Make creature tokens | [`RaiseTheAlarm.java`](../Mage.Sets/src/mage/cards/r/RaiseTheAlarm.java) |
| `AddCountersTargetEffect` | Adds counters to selected object | +1/+1, loyalty, charge counters | [`HungerOfTheHowlpack.java`](../Mage.Sets/src/mage/cards/h/HungerOfTheHowlpack.java) |
| `AddCountersSourceEffect` | Adds counters to this source | Creature grows itself | [`ExperimentOne.java`](../Mage.Sets/src/mage/cards/e/ExperimentOne.java) |
| `BoostTargetEffect` | Changes chosen creature's P/T | Usually “gets +N/+N until EOT” | [`GiantGrowth.java`](../Mage.Sets/src/mage/cards/g/GiantGrowth.java) |
| `GainAbilityTargetEffect` | Gives selected object an ability | Grant flying/haste/etc. | [`Jump.java`](../Mage.Sets/src/mage/cards/j/Jump.java) |
| `GainControlTargetEffect` | Changes who controls a permanent | Theft effects | [`ActOfTreason.java`](../Mage.Sets/src/mage/cards/a/ActOfTreason.java) |
| `ReturnToHandTargetEffect` | Returns selected permanent to owner's hand | “Bounce” | [`ManOWar.java`](../Mage.Sets/src/mage/cards/m/ManOWar.java) |
| `ReturnFromGraveyardToHandTargetEffect` | Gets a graveyard card back | Raise-dead effects | [`RaiseDead.java`](../Mage.Sets/src/mage/cards/r/RaiseDead.java) |
| `ReturnFromGraveyardToBattlefieldTargetEffect` | Puts graveyard card onto battlefield | Reanimation | [`Zombify.java`](../Mage.Sets/src/mage/cards/z/Zombify.java) |
| `MillCardsTargetEffect` | Moves top library cards to graveyard | Milling | [`TomeScour.java`](../Mage.Sets/src/mage/cards/t/TomeScour.java) |
| `SearchLibraryPutInHandEffect` | Searches library into hand | Tutors | [`DiabolicTutor.java`](../Mage.Sets/src/mage/cards/d/DiabolicTutor.java) |
| `SearchLibraryPutInPlayEffect` | Searches library onto battlefield | Land ramp | [`RampantGrowth.java`](../Mage.Sets/src/mage/cards/r/RampantGrowth.java) |
| `TapTargetEffect` / `UntapTargetEffect` | Taps/untaps chosen object | Tap manipulation | [`Twiddle.java`](../Mage.Sets/src/mage/cards/t/Twiddle.java) |
| `PreventDamageToTargetEffect` | Sets up a damage shield | Damage prevention | [`HealingSalve.java`](../Mage.Sets/src/mage/cards/h/HealingSalve.java) |
| `CreateTokenCopyTargetEffect` | Makes a token copy | Clone-like token effects | [`CacklingCounterpart.java`](../Mage.Sets/src/mage/cards/c/CacklingCounterpart.java) |
| `TargetAnyTarget` | Creature, player, or planeswalker target | “Any target” | [`LightningBolt.java`](../Mage.Sets/src/mage/cards/l/LightningBolt.java) |
| `TargetCreaturePermanent` | Creature on battlefield | “Target creature” | [`ManOWar.java`](../Mage.Sets/src/mage/cards/m/ManOWar.java) |
| `TargetControlledPermanent` | Permanent controlled by chooser/source controller | “Target permanent you control” | Search [`target/common`](../Mage/src/main/java/mage/target/common) |
| `TargetOpponent` | Opponent only | “Target opponent” | Search same directory |
| `TargetCardInYourGraveyard` | Your graveyard card | Recursion | [`RaiseDead.java`](../Mage.Sets/src/mage/cards/r/RaiseDead.java) |
| `TargetSpell` / `TargetStackObject` | Object currently on stack | Counters/copies | [`Counterspell.java`](../Mage.Sets/src/mage/cards/c/Counterspell.java) |
| `FilterCreaturePermanent` | Rules for eligible creatures | Used by target/global effects | [`mage/filter`](../Mage/src/main/java/mage/filter) |
| `ControllerPredicate.YOU` | Requires “you control it” | Added to filters | [`mage/filter/predicate`](../Mage/src/main/java/mage/filter/predicate) |
| `AnotherPredicate` | Excludes the source | Oracle word “another” | Same predicate directory |
| `PermanentsOnBattlefieldCount` | Counts matching permanents now | “Equal to number of…” | [`dynamicvalue/common`](../Mage/src/main/java/mage/abilities/dynamicvalue/common) |
| `CardsInControllerHandCount` | Counts controller's hand | Hand-size effects | Same directory |
| `GetXValue` | Reads relevant X | X spells/effects | Same directory |
| `CountersCount` | Counts counters | Counter-scaled values | Same directory |
| `ConditionalOneShotEffect` | Chooses effect based on condition at resolution | “If…, do…” | [`ChillingTrap.java`](../Mage.Sets/src/mage/cards/c/ChillingTrap.java) |
| `Duration.EndOfTurn` | Effect stops at turn end | “Until end of turn” | [`GiantGrowth.java`](../Mage.Sets/src/mage/cards/g/GiantGrowth.java) |
| `Duration.WhileOnBattlefield` | Applies while source remains | Static permanent effects | [`GloriousAnthem.java`](../Mage.Sets/src/mage/cards/g/GloriousAnthem.java) |
| `Watcher` | Remembers game history | “First/this turn,” prior events | [`mage/watchers/common`](../Mage/src/main/java/mage/watchers/common) |

## 5. Common code lines

* `this.addAbility(...)` — **When you see this, it roughly means** “attach this ability to the card.”
* `this.getSpellAbility().addEffect(...)` — **When you see this, it roughly means** “make this happen when the card spell resolves.”
* `ability.addEffect(...)` — **When you see this, it roughly means** “after the earlier parts, this ability also does this.” Order can matter.
* `ability.addTarget(...)` — **When you see this, it roughly means** “this ability must/may select an object matching these rules.”
* `ability.addCost(...)` — **When you see this, it roughly means** “pay this in addition to the main cost.”
* `new TargetCreaturePermanent(...)` — **When you see this, it roughly means** “create a target slot for a creature on the battlefield.”
* `new FilterCreaturePermanent(...)` — **When you see this, it roughly means** “describe a group of legal/matching creatures.” A filter by itself is not a target.
* `new ConditionalOneShotEffect(...)` — **When you see this, it roughly means** “choose which one-time result happens by checking a condition.”
* `setPower(...)` / `this.power = ...` — **When you see this, it roughly means** “set printed/base power.”
* `setToughness(...)` / `this.toughness = ...` — **When you see this, it roughly means** “set printed/base toughness.”
* `addSubType(...)` / `subtype.add(...)` — **When you see this, it roughly means** “add a subtype such as Elf, Aura, or Forest.”
* `addSuperType(...)` / `supertype.add(...)` — **When you see this, it roughly means** “add a supertype such as Legendary, Basic, or Snow.”
* `setText(...)` — **When you see this, it roughly means** “override wording shown to users.” It does not fix incorrect behavior.
* `SomeAbility.getInstance()` — **When you see this, it roughly means** “reuse one safe shared copy of a standard, immutable ability.”

## 6. A simple reading workflow

1. **Open `super(...)`.** Check card type and mana cost first.
2. **Check characteristics.** Look for subtypes, supertypes, color, power/toughness, or loyalty.
3. **Find every `addAbility(...)`.** Match each one to a sentence/keyword in Oracle text.
4. **Identify effects.** Class names usually reveal what happens.
5. **Identify targets.** Confirm target kind, zone, controller, and “up to.”
6. **Inspect filters/conditions.** Look for “you control,” “another,” type, color, tapped status, and timing.
7. **Read helper classes below the card.** Ask why common engine classes were insufficient.
8. **Look for a Watcher** if the card cares about something that happened earlier.
9. **Read the set entry** under [`Mage.Sets/src/mage/sets`](../Mage.Sets/src/mage/sets): exact name, collector number, rarity, class.
10. **Read tests** under [`Mage.Tests/src/test/java/org/mage/test/cards`](../Mage.Tests/src/test/java/org/mage/test/cards). Confirm they test the difficult behavior, not merely that the card loads.
11. **Check the diff.** A card should not normally require unrelated engine/UI/server changes.

## 7. Oracle wording clues

| Oracle wording | What it usually suggests in XMage |
|---|---|
| “When…” / “Whenever…” | A triggered ability listening for an event. |
| “At the beginning of…” | A beginning-of-step trigger, often a reusable class. |
| “When this enters…” | `EntersBattlefieldTriggeredAbility`. |
| “When this dies…” | `DiesTriggeredAbility`, not every move to a graveyard. |
| “As this enters…” / “enters with…” | An as-enters/replacement pattern, usually not an ETB trigger. |
| “You may…” | An optional effect or optional trigger; who chooses and when matters. |
| “If you do…” | The second action must depend on successfully doing the first. |
| “If …” inside a trigger clause | Possibly an “intervening if,” checked both when triggering and resolving. |
| “As long as…” | A continuously rechecked static/conditional continuous effect. |
| “Instead…” / “would…” | A replacement effect that changes an event before it happens. |
| “For each…” / “equal to…” | A calculated `DynamicValue`, or iteration if separate events/choices occur. |
| “Until end of turn…” | Usually `Duration.EndOfTurn`. |
| “Once each turn…” | A reusable activation/trigger usage restriction. |
| “For the first time each turn…” | A first-event trigger/Watcher pattern. |
| “Another…” | Filter must exclude this card/source. |
| “You control…” | Controller-aware target/filter. “Own” is different. |
| “Target…” | A `Target` object is required. |
| “Up to one target…” | Target minimum must be zero, so choosing none is legal. |
| “Each/all…” | Usually a filter-based non-target effect; hexproof should not stop it. |
| “Choose one/two…” | Modes, each with its own effects and perhaps targets. |
| “Choose a color/type…” | A `Choice`, not necessarily a target. |
| “Additional cost…” | An added `Cost`; it is paid while casting/activating. |
| “Without paying…” | Alternate casting cost infrastructure, not manual free movement. |
| “That creature/card/amount…” | The code must preserve the earlier object/value correctly, sometimes using game history or last known information. |

## 8. What a normal implementation looks like

A simple card should usually be small. It normally contains imports, one final card class, a constructor, a private copy constructor, and `copy()`. The constructor's parts tend to follow Oracle order. Common mechanics should reuse engine classes—exactly as [`ElvishMystic.java`](../Mage.Sets/src/mage/cards/e/ElvishMystic.java) reuses `GreenManaAbility` and [`SerraAngel.java`](../Mage.Sets/src/mage/cards/s/SerraAngel.java) reuses flying/vigilance.

Complex mechanics can legitimately need card-specific effect, ability, condition, watcher, or dynamic-value classes below the card. Helpers are not automatically bad. Each should have a clear rules reason, copying support, and focused tests. A static continuous rule should remain correct when permanents enter later; it should not usually be a one-time loop.

Some files are old and show legacy formatting or unusual patterns. “An existing card does it” is weaker evidence than “a reusable class and several representative cards do it.” Ask Codex to compare multiple implementations and explain which current pattern it followed.

## 9. Red flags: ask Codex to review again

* Many brand-new framework classes for one card.
* A very simple card becomes hundreds of lines.
* A custom effect duplicates a clearly common action such as drawing, damage, destroying, or token creation.
* Nontrivial behavior has no focused test.
* Strange hardcoded IDs, names, zones, player counts, or magic numbers.
* Duplicated rules logic or manual loops copied into several places.
* A custom Watcher for a common “this turn/first time” mechanic.
* A “whenever one or more” ability that listens to each object's event separately;
  simultaneous objects should be grouped into one batch so the ability triggers once.
* Huge unrelated formatting/generated-file diffs.
* Engine/core changes for a simple card without strong justification.
* Filters/predicates whose names or descriptions do not clearly match Oracle restrictions.
* Comments saying behavior is “approximate,” “good enough,” or unsupported without surfacing a limitation.
* Skipped tests, ignored failures, or only a compile claim with no command output.
* Direct manual game-state mutation rather than standard Effects/Abilities.
* UI text forced with `setText` while the underlying effect class does something different.
* Missing copy constructor/`copy()`, or a copy constructor that calls the public constructor.
* Targeting code where Oracle says “each,” “choose,” or otherwise does not say target.
* Reusing an object reference after it moved zones.

## 10. Review checklist without knowing Java

- [ ] Does `"{...}"` show the correct mana cost?
- [ ] Does the `CardType` match the printed type line?
- [ ] Are subtype and supertype entries correct?
- [ ] For creatures, are power/toughness correct? For planeswalkers, starting loyalty?
- [ ] Does each Oracle sentence/keyword appear to have a matching ability/effect?
- [ ] Do effect names roughly match the action?
- [ ] Are targets sensible: creature/player/spell/card, correct zone, correct controller/owner?
- [ ] Is “another” excluded and “up to” optional?
- [ ] Is temporary wording paired with the exact duration?
- [ ] Does “instead” use replacement-effect machinery?
- [ ] Does past-event wording use an existing Watcher or equivalent history mechanism?
- [ ] Are common mechanics reused rather than rebuilt?
- [ ] Is the standard private copy constructor plus `copy()` present?
- [ ] Is the card registered with correct name, set, collector number, and rarity?
- [ ] Is there a focused test for nontrivial behavior?
- [ ] Does the test cover decline/failure/timing/cleanup or multiple events where relevant?
- [ ] Did the stated test commands actually pass?
- [ ] Does `git diff` contain only expected card, set, and test files?

## 11. Slow, human-readable examples

### A. Simple spell: Lightning Bolt

File: [`Mage.Sets/src/mage/cards/l/LightningBolt.java`](../Mage.Sets/src/mage/cards/l/LightningBolt.java).

```java
super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{R}");
this.getSpellAbility().addTarget(new TargetAnyTarget());
this.getSpellAbility().addEffect(new DamageTargetEffect(3));
```

Translation: this is a red one-mana instant. Casting it requires any legal damage target. When it resolves, it deals 3 damage there. This is reassuringly short because damage and “any target” already exist in the engine.

### B. Simple creature: Serra Angel

File: [`Mage.Sets/src/mage/cards/s/SerraAngel.java`](../Mage.Sets/src/mage/cards/s/SerraAngel.java).

```java
this.subtype.add(SubType.ANGEL);
this.power = new MageInt(4);
this.toughness = new MageInt(4);
this.addAbility(FlyingAbility.getInstance());
this.addAbility(VigilanceAbility.getInstance());
```

Translation: it is an Angel, printed 4/4, with the engine's normal flying and vigilance. Singleton `getInstance()` is sensible here because those standard keyword objects are reusable.

### C. ETB card: Man-o'-War

File: [`Mage.Sets/src/mage/cards/m/ManOWar.java`](../Mage.Sets/src/mage/cards/m/ManOWar.java).

```java
Ability ability = new EntersBattlefieldTriggeredAbility(new ReturnToHandTargetEffect());
ability.addTarget(new TargetCreaturePermanent());
this.addAbility(ability);
```

Translation: when it enters, XMage puts a trigger on the stack. That trigger selects one creature permanent and returns it to its owner's hand. Since Oracle does not say “another,” using an ordinary creature target correctly allows Man-o'-War itself.

### D. Activated ability: Elvish Mystic

File: [`Mage.Sets/src/mage/cards/e/ElvishMystic.java`](../Mage.Sets/src/mage/cards/e/ElvishMystic.java).

```java
this.addAbility(new GreenManaAbility());
```

Translation: add XMage's standard “tap: add green” mana ability. The tap cost and special mana-ability behavior are already inside that class. One reused line is better than custom code.

For a targeted activated ability, compare [`ProdigalSorcerer.java`](../Mage.Sets/src/mage/cards/p/ProdigalSorcerer.java): read it as “tap cost: damage effect,” plus an any-target choice.

### E. Trigger: Phyrexian Arena

File: [`Mage.Sets/src/mage/cards/p/PhyrexianArena.java`](../Mage.Sets/src/mage/cards/p/PhyrexianArena.java).

Look for `BeginningOfUpkeepTriggeredAbility` and its ordered effects. In plain English: at its controller's upkeep, the trigger resolves, that player draws, and that player loses life. Order and correct controller matter; life loss is not damage.

### F. Continuous effect: Glorious Anthem

File: [`Mage.Sets/src/mage/cards/g/GloriousAnthem.java`](../Mage.Sets/src/mage/cards/g/GloriousAnthem.java).

Its ability continuously boosts creatures its controller controls while the enchantment remains on the battlefield. That is better than changing only creatures present when it entered: a later creature must also receive +1/+1, and one that changes controller may stop receiving it.

### G. More complex card: Rest in Peace

File: [`Mage.Sets/src/mage/cards/r/RestInPeace.java`](../Mage.Sets/src/mage/cards/r/RestInPeace.java).

This card has two conceptually different jobs. Its entry trigger exiles cards already in graveyards once. Its static replacement effect changes future graveyard-bound cards so they go to exile instead. A trigger that waits until after cards reach the graveyard would not be equivalent; other death/graveyard events could already have occurred. Separate reusable pieces are therefore a sign of careful modeling, not needless complexity.

## 12. Compact glossary

| Term | Plain-English meaning |
|---|---|
| Ability | A rule/action a card has. |
| Activated ability | Something a player starts by paying a cost before a colon. |
| Argument | Actual input supplied to code. |
| Card object | One particular in-game incarnation of a card. |
| Condition | A yes/no rules requirement. |
| Constructor | Code that initially builds an object. |
| Continuous effect | A modifier/rule continually applied for a duration. |
| Controller | Player currently controlling an object; may differ from owner. |
| Copy constructor | Builds an independent duplicate from an existing object. |
| Cost | Mana/tap/sacrifice/discard/etc. paid to use something. |
| Duration | When a continuing effect expires. |
| DynamicValue | Number computed from current/relevant game data. |
| Effect | The actual result of a spell/ability. |
| Filter | Full description of allowed/matching objects. |
| Game event | Something such as cast, damage, draw, die, or zone change. |
| LKI | Last Known Information: facts about an object just before it left. |
| Mode | One branch of a modal “choose” spell/ability. |
| Object identity | The exact in-game object, not merely the same physical card/name. |
| Oracle text | Official rules text an implementation must model. |
| Owner | Player whose deck the card began in. |
| Predicate | One yes/no test composing a filter. |
| Replacement effect | Changes what would happen, often signaled by “instead.” |
| Source | Card/object/ability that created an effect. |
| Spell ability | Ability representing casting and resolving a card. |
| Static ability | Rule that applies without activation or triggering. |
| Target | Legally chosen recipient/object named by “target.” |
| Triggered ability | Automatically reacts to “when/whenever/at.” |
| UUID | Machine identifier used to distinguish game entities. |
| Watcher | Copy-safe memory of earlier game events. |
| Zone | Hand, library, battlefield, graveyard, stack, exile, command, or outside. |

## 13. What should I ask Codex?

Useful follow-up prompts include:

* “Explain this implementation to me in plain English, line by line.”
* “Compare this implementation to three existing cards with similar Oracle text and link each file.”
* “Review this diff for unnecessary new infrastructure.”
* “Check whether every new helper class is truly necessary.”
* “Show me which existing XMage classes this implementation reuses.”
* “Explain each test and exactly what behavior it proves.”
* “Add or identify tests for declining optional actions, illegal targets, timing, and duration cleanup.”
* “Verify target zones, controller-versus-owner restrictions, and the word ‘another.’”
* “Verify whether this trigger has an intervening-if condition.”
* “Verify X is calculated at the correct time and from the correct value.”
* “Check whether Last Known Information or a Watcher is required.”
* “Show the exact set-registration entry and explain collector number, rarity, and variants.”
* “List every changed file and explain why it had to change.”
* “Run the focused tests and relevant compile command, then show whether each passed.”
* “Review this card as if you were an XMage maintainer.”
