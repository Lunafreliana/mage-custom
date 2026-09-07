# Read this document before implementing custom cards in this repository.

This is a repository-specific implementation guide. Treat the checked-out source—not memory of another XMage version—as authoritative. Paths below are links into this checkout.

## Rules Research / Oracle Is Not Enough

**This workflow is mandatory for every future card implementation.** Oracle text identifies what a card says, but it is not always a complete specification of what the rules make those words do. Keywords, keyword actions, ability words, and rules-defined terms can compress several abilities, choices, events, timing restrictions, or object-tracking rules into a few words. Reminder text is explanatory shorthand, not the complete rules.

Examples that require rules research include vanishing, suspend, time travel, morph, disguise, cloak, manifest, discover, proliferate, connive, cascade, ward, replacement effects, linked abilities, face-down permanents, copy effects, and the defined meanings of “dies” and “leaves the battlefield.” For example, **vanishing N** is not merely “put N counters on this”: the Comprehensive Rules define entering with time counters, an upkeep time-counter-removal trigger, and a sacrifice trigger when the last time counter is removed, with exact conditions and interactions. **Time travel** is a keyword action: the player identifies eligible suspended cards they own and permanents they control with time counters, may choose among them, chooses add or remove for each, and the selected counter changes occur together. Do not reconstruct either mechanic from its reminder text.

### When online research is required

Research the card and mechanic online before choosing an implementation whenever any of these is true:

* the card has a keyword or keyword action whose complete behavior is not explicit in Oracle text;
* the mechanic is unfamiliar, or any timing, choice, ownership, targeting, zone, or interaction detail is uncertain;
* it involves face-down objects, copy effects, replacement effects, linked abilities, continuous effects/layers, zone changes, delayed triggers, Last Known Information, or unusual targeting;
* existing XMage implementations look inconsistent, isolated, old, or adapted to obsolete wording;
* the intended interaction cannot be established confidently from Oracle text alone.

### Source priority

Use sources in this order, recording the relevant rules numbers/rulings in implementation notes or the task record:

1. **Current Magic Comprehensive Rules and official Wizards rules material.** Start at [Wizards: Rules](https://magic.wizards.com/en/rules) and follow its current Comprehensive Rules link; do not keep an old downloaded rules file as authority.
2. **Current official Oracle text from [Gatherer](https://gatherer.wizards.com/).** Search the exact card and confirm wording, types, mana cost, and rulings shown there.
3. **Official Wizards Release Notes for the card's set.** Search [Magic news](https://magic.wizards.com/en/news) for the exact set/product name plus “Release Notes,” and verify that the article is for the correct release.
4. **Official Wizards card-specific rulings and mechanic explanations.** Check the card entry in the Release Notes and official mechanic articles as well as Gatherer's rulings.
5. **Modern XMage implementations of the same mechanic.** Compare several callers and the engine implementation; code is evidence of engine behavior, not higher authority than the rules.
6. **[Scryfall](https://scryfall.com/) as a convenient secondary reference** for current Oracle text and rulings. If it disagrees with an official source, or uncertainty remains, prefer Wizards' current rules material/Gatherer.

**The official Release Notes for the specific set MUST be checked whenever they exist. This is not optional.** Release Notes commonly supply practical mechanic definitions, edge cases, interaction details, and card-specific rulings that are easy to miss when reading only Oracle text or an isolated Comprehensive Rules entry. This check is especially important for crossover sets, Commander products, supplemental products, and mechanically unusual releases.

For a card from a known set:

1. Identify its exact set/product and set code from `Mage.Sets/src/mage/sets` and current Oracle data.
2. Find that set's official Wizards Release Notes; do not silently substitute notes for a similarly named product.
3. Search within the notes for the exact card name.
4. Search within the notes for every unfamiliar keyword, keyword action, and named mechanic on the card.
5. Apply both the general mechanic discussion and any card-specific ruling while designing implementation and tests.

Do **not** rely on printed text when Oracle differs, reminder text as though it were exhaustive rules, memory of a mechanic, a search-result summary, or an old XMage implementation merely because it exists. Record access dates when rules version matters.

### Required pre-coding sequence for an unfamiliar or compressed mechanic

1. Read the card's current Oracle text in Gatherer (use Scryfall only as a secondary convenience).
2. List every keyword, keyword action, ability word, and rules-defined term in it. Ability words have no rules meaning themselves, but the full ability still must be modeled.
3. Open the **current** Comprehensive Rules and read every relevant definition and cross-reference, not only the glossary entry.
4. Identify the card's set and find and read that set's official Wizards Release Notes whenever they exist.
5. Search those Release Notes for both the exact card name and every relevant keyword/action/mechanic.
6. Check official card-specific rulings and mechanic explanations; distinguish explanatory rulings from rules text.
7. Search XMage for modern cards using the same mechanic and for the shared ability/effect/watcher/condition implementation.
8. Compare every rules requirement—events, zones, choices, timing, controller/owner, target status, simultaneous actions, linked data, copy behavior, and LKI—with what the current engine code actually provides.
9. Only then select reusable infrastructure or justify new code and focused tests.

If research uncovers an engine constraint or rules detail broadly useful to later implementations, update these guides (with the official source and rules version/date where appropriate) rather than forcing every future task to rediscover it. Online research supplements repository inspection; it never excuses guessing about the checked-out API.

## 1. Repository structure

XMage is a multi-module Maven project.

| Concern | Current location |
|---|---|
| Shared engine/card model | [`Mage/src/main/java/mage`](../Mage/src/main/java/mage) |
| Card base classes (`Card`, `CardImpl`, `CardSetInfo`, split/transform variants) | [`Mage/src/main/java/mage/cards`](../Mage/src/main/java/mage/cards) |
| Abilities and triggers | [`Mage/src/main/java/mage/abilities`](../Mage/src/main/java/mage/abilities), especially `common`, `keyword`, `mana`, `triggers` |
| Effects | [`Mage/src/main/java/mage/abilities/effects`](../Mage/src/main/java/mage/abilities/effects), especially `common`, `common/continuous`, and `replacement` |
| Costs | [`Mage/src/main/java/mage/abilities/costs`](../Mage/src/main/java/mage/abilities/costs) (`common`, `mana`, `costadjusters`) |
| Targets and target adjustment | [`Mage/src/main/java/mage/target`](../Mage/src/main/java/mage/target), [`mage/target/common`](../Mage/src/main/java/mage/target/common) |
| Filters and predicates | [`Mage/src/main/java/mage/filter`](../Mage/src/main/java/mage/filter), [`mage/filter/common`](../Mage/src/main/java/mage/filter/common), [`mage/filter/predicate`](../Mage/src/main/java/mage/filter/predicate) |
| Conditions/decorators | [`Mage/src/main/java/mage/abilities/condition`](../Mage/src/main/java/mage/abilities/condition), [`mage/abilities/decorator`](../Mage/src/main/java/mage/abilities/decorator) |
| Dynamic values | [`Mage/src/main/java/mage/abilities/dynamicvalue`](../Mage/src/main/java/mage/abilities/dynamicvalue), [`common`](../Mage/src/main/java/mage/abilities/dynamicvalue/common) |
| Watchers | [`Mage/src/main/java/mage/watchers`](../Mage/src/main/java/mage/watchers), [`common`](../Mage/src/main/java/mage/watchers/common) |
| Constants (`Zone`, `Duration`, types, layers) | [`Mage/src/main/java/mage/constants`](../Mage/src/main/java/mage/constants) |
| Game state/events/permanents | [`Mage/src/main/java/mage/game`](../Mage/src/main/java/mage/game) |
| Reusable utilities | [`Mage/src/main/java/mage/util`](../Mage/src/main/java/mage/util) |
| Actual card classes, alphabetized by first letter | [`Mage.Sets/src/mage/cards`](../Mage.Sets/src/mage/cards) |
| Set registries | [`Mage.Sets/src/mage/sets`](../Mage.Sets/src/mage/sets) |
| Reusable token classes | [`Mage/src/main/java/mage/game/permanent/token`](../Mage/src/main/java/mage/game/permanent/token) |
| Card behavior tests | [`Mage.Tests/src/test/java/org/mage/test/cards`](../Mage.Tests/src/test/java/org/mage/test/cards) |
| Test framework | [`Mage.Tests/src/test/java/org/mage/test`](../Mage.Tests/src/test/java/org/mage/test) |

A normal card addition touches one `Mage.Sets/src/mage/cards/<letter>/...java` file, one or more existing-set registries, and focused tests. Touch `Mage` only if exhaustive searching proves the mechanic needs genuinely reusable engine infrastructure. Never put generally reusable behavior in a set class.

## 2. Anatomy of a card

[`LightningBolt.java`](../Mage.Sets/src/mage/cards/l/LightningBolt.java) is the canonical small spell:

```java
public final class LightningBolt extends CardImpl {
    public LightningBolt(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{R}");
        this.getSpellAbility().addTarget(new TargetAnyTarget());
        this.getSpellAbility().addEffect(new DamageTargetEffect(3));
    }
    private LightningBolt(final LightningBolt card) { super(card); }
    @Override public LightningBolt copy() { return new LightningBolt(this); }
}
```

* `CardImpl` supplies identity, owner/controller data, spell ability, characteristics, and cloning machinery.
* `UUID ownerId` identifies the deck owner. It is not necessarily the current controller.
* `CardSetInfo` carries printing metadata supplied by set registration.
* `super(ownerId, setInfo, CardType[], manaCost)` initializes the card. Mana syntax uses braces (`{2}{U}`, `{X}{R}`, `{W/U}`).
* `CardType` is the type line's major type. Add `SubType` through `this.subtype.add(...)`; current code also commonly uses `addSubType(...)`. Add `SuperType` through `supertype.add(...)`/`addSuperType(...)`; follow nearby current code.
* Creatures assign `this.power` and `this.toughness` to `new MageInt(n)`, as [`ManOWar.java`](../Mage.Sets/src/mage/cards/m/ManOWar.java) does. Variable `*` values generally need characteristic-defining abilities, not an arbitrary fixed `MageInt`.
* Planeswalkers set `this.setStartingLoyalty(n)` and add `LoyaltyAbility` instances. Verify the exact current pattern in a similar planeswalker.
* Color is normally derived from mana cost/color indicators. Explicit color identity/indicator is used for colorless costs, back faces, or rules text; search `getColorIdentity().add(...)` and `getColor().set...` before copying a pattern.

A spell's built-in `SpellAbility` is extended through `getSpellAbility()`. Permanents receive abilities through `addAbility`:

* spell: Bolt's `DamageTargetEffect` plus `TargetAnyTarget`;
* triggered: [`ManOWar.java`](../Mage.Sets/src/mage/cards/m/ManOWar.java) creates `EntersBattlefieldTriggeredAbility`, then adds its target;
* mana: [`ElvishMystic.java`](../Mage.Sets/src/mage/cards/e/ElvishMystic.java) reuses `GreenManaAbility`;
* keyword/static: [`SerraAngel.java`](../Mage.Sets/src/mage/cards/s/SerraAngel.java) reuses singleton flying and vigilance abilities;
* activated: normally `new SimpleActivatedAbility(effect, cost)` followed by targets/additional costs;
* loyalty: `new LoyaltyAbility(effect, loyaltyDelta)`;
* replacement: add a `ReplacementEffect` through a suitable static ability or an existing replacement ability;
* continuous: usually `SimpleStaticAbility(new ...ContinuousEffect(...))` or a keyword ability.

Optional instructions use existing `OptionalEffect`, `OptionalAdditionalCost`, optional triggers, or `Choice` APIs according to the exact wording. “If you do” often requires `DoIfCostPaid`/`DoIfClashWon`-style sequencing, not two independent effects. Modal spells add `Mode` objects to the spell ability and each mode owns its effects/targets. Search current cards with the same “choose one/two” wording because minimum/maximum modes and repeated modes differ.

Every card ends with a private copy constructor calling `super(card)` and a covariant `copy()`. Copy every mutable/custom field explicitly if `super(card)` cannot do so.

## 3. Practical effect reference

Effects live principally in `mage.abilities.effects.common`; continuous effects are usually in `.common.continuous`, replacement effects in `.replacement`. Constructor overloads evolve: open the class and a recent caller before use.

| Effect (package suffix `.effects.common` unless noted) | Purpose and important inputs | Real card example |
|---|---|---|
| `DamageTargetEffect(int/DynamicValue)` | Damage the selected target; amount and “source deals” text variants matter | [`LightningBolt.java`](../Mage.Sets/src/mage/cards/l/LightningBolt.java) |
| `DamageAllEffect(int/DynamicValue, FilterPermanent)` | Damage every matching permanent (and overloads may include players) | [`Pyroclasm.java`](../Mage.Sets/src/mage/cards/p/Pyroclasm.java) |
| `GainLifeEffect(int/DynamicValue)` | Source controller gains life | [`HealingSalve.java`](../Mage.Sets/src/mage/cards/h/HealingSalve.java) |
| `LoseLifeTargetEffect(int/DynamicValue)` | Target player loses life (not damage) | [`SovereignsBite.java`](../Mage.Sets/src/mage/cards/s/SovereignsBite.java) |
| `DrawCardSourceControllerEffect(int/DynamicValue)` | Controller draws | [`Divination.java`](../Mage.Sets/src/mage/cards/d/Divination.java) |
| `DiscardTargetEffect(int/DynamicValue)` | Target player discards, with target pointer supplied by the ability | [`MindRot.java`](../Mage.Sets/src/mage/cards/m/MindRot.java) |
| `DestroyTargetEffect` | Destroy target permanent; destruction obeys indestructible/regeneration rules | [`Murder.java`](../Mage.Sets/src/mage/cards/m/Murder.java) |
| `DestroyAllEffect(FilterPermanent)` | Destroy all matching permanents | [`DayOfJudgment.java`](../Mage.Sets/src/mage/cards/d/DayOfJudgment.java) |
| `ExileTargetEffect` | Exile targeted permanent/card | [`PathToExile.java`](../Mage.Sets/src/mage/cards/p/PathToExile.java) |
| `ExileFromZoneTargetEffect(Zone, ...)` | Exile a target from a specified nonbattlefield zone | [`TormodsCrypt.java`](../Mage.Sets/src/mage/cards/t/TormodsCrypt.java) |
| `SacrificeEffect(FilterPermanent, int, ...)` | A player sacrifices matching permanents; do not fake this with destroy | [`DiabolicEdict.java`](../Mage.Sets/src/mage/cards/d/DiabolicEdict.java) |
| `CreateTokenEffect(Token, int/DynamicValue)` | Create token(s); token object describes characteristics | [`RaiseTheAlarm.java`](../Mage.Sets/src/mage/cards/r/RaiseTheAlarm.java) |
| `AddCountersTargetEffect(CounterType[, amount])` | Add counters to target; use `CounterType`/counter objects | [`HungerOfTheHowlpack.java`](../Mage.Sets/src/mage/cards/h/HungerOfTheHowlpack.java) |
| `AddCountersSourceEffect` | Add counters to the source itself | [`ExperimentOne.java`](../Mage.Sets/src/mage/cards/e/ExperimentOne.java) |
| `BoostTargetEffect(power, toughness, Duration)` | Temporary/per-duration P/T change | [`GiantGrowth.java`](../Mage.Sets/src/mage/cards/g/GiantGrowth.java) |
| `BoostControlledEffect(..., Filter, Duration)` | Boost all controlled matching creatures | [`GloriousCharge.java`](../Mage.Sets/src/mage/cards/g/GloriousCharge.java) |
| `GainAbilityTargetEffect(Ability, Duration)` | Grant a keyword/ability to target | [`Jump.java`](../Mage.Sets/src/mage/cards/j/Jump.java) |
| `GainControlTargetEffect(Duration)` | Change control, optionally temporarily | [`ActOfTreason.java`](../Mage.Sets/src/mage/cards/a/ActOfTreason.java) |
| `CopyTargetStackObjectEffect` / `CopyStackObjectEffect` | Copy a spell/stack object; copy/retarget choices are subtle | [`Reverberate.java`](../Mage.Sets/src/mage/cards/r/Reverberate.java) |
| `CreateTokenCopyTargetEffect` | Make a token copy of a permanent | [`CacklingCounterpart.java`](../Mage.Sets/src/mage/cards/c/CacklingCounterpart.java) |
| `SearchLibraryPutInHandEffect(TargetCard, ...)` | Search using a card target/filter, reveal/shuffle as encoded | [`DiabolicTutor.java`](../Mage.Sets/src/mage/cards/d/DiabolicTutor.java) |
| `SearchLibraryPutInPlayEffect` | Search and move matching cards to battlefield, with tapped/controller flags | [`RampantGrowth.java`](../Mage.Sets/src/mage/cards/r/RampantGrowth.java) |
| `MillCardsTargetEffect` / `MillCardsControllerEffect` | Put top cards into graveyard; amount can be dynamic | [`TomeScour.java`](../Mage.Sets/src/mage/cards/t/TomeScour.java) |
| `ReturnToHandTargetEffect` | Return selected permanent/card to owner's hand | [`ManOWar.java`](../Mage.Sets/src/mage/cards/m/ManOWar.java) |
| `ReturnFromGraveyardToHandTargetEffect` | Move graveyard target to hand | [`RaiseDead.java`](../Mage.Sets/src/mage/cards/r/RaiseDead.java) |
| `ReturnFromGraveyardToBattlefieldTargetEffect` | Reanimate a targeted card | [`Zombify.java`](../Mage.Sets/src/mage/cards/z/Zombify.java) |
| `TapTargetEffect` / `UntapTargetEffect` | Tap/untap legal targets | [`Twiddle.java`](../Mage.Sets/src/mage/cards/t/Twiddle.java) |
| `PreventDamageToTargetEffect` | Create a prevention shield; amount/duration and target pointer matter | [`HealingSalve.java`](../Mage.Sets/src/mage/cards/h/HealingSalve.java) |
| `ReplacementEffectImpl` (`.effects`) subclasses | Intercept matching `GameEvent`s and replace/modify them | [`RestInPeace.java`](../Mage.Sets/src/mage/cards/r/RestInPeace.java) |
| `ContinuousEffectImpl` and layered subclasses | Continuously alter rules/characteristics using the correct layer | [`GloriousAnthem.java`](../Mage.Sets/src/mage/cards/g/GloriousAnthem.java) |
| `ConditionalOneShotEffect` (`mage.abilities.decorator`) | Resolve one effect or another based on a `Condition` | [`ChillingTrap.java`](../Mage.Sets/src/mage/cards/c/ChillingTrap.java) |

Other high-frequency families worth searching before custom code: `CounterTargetEffect`, `RevealLibraryPutIntoHandEffect`, `LookLibraryAndPickControllerEffect`, `ScryEffect`, `SurveilEffect`, `ShuffleLibraryEffect`, `FightTargetsEffect`, `AttachEffect`, `PlayAdditionalLandsControllerEffect`, delayed-trigger creation effects, and `AsThoughEffect` subclasses.

When a `CopyApplier` adds a compound keyword ability directly to a blueprint's
ability collection, flatten its `getSubAbilities()` results into that collection
and clear them from the parent ability. Card and token construction normally
flattens those hidden component abilities automatically, but direct collection
mutation does not; leaving them only nested can make the copied permanent display
the keyword without its behavior, while retaining them both nested and flattened
can register its triggers twice.

**Duration is semantic.** Pass `Duration.EndOfTurn` only for “until end of turn”; battlefield static effects generally use `WhileOnBattlefield`; one-shot prevention/replacement shields often use `OneUse`; special cleanup logic may use `Custom`. Never select duration merely to make displayed text look right.

## 4. Ability system

Core interfaces/base classes are in [`mage/abilities`](../Mage/src/main/java/mage/abilities).

* `SpellAbility` represents casting a card. Card constructors normally modify the one created by `CardImpl`, rather than instantiate it.
* `ActivatedAbility` is the base for “cost: effect.” `SimpleActivatedAbility` is the preferred concise implementation for ordinary activated abilities. Its current constructors take an `Effect` and `Cost` (with zone overloads in the class); add further costs, targets, and effects afterward.
* `TriggeredAbility` is the base for event-driven abilities. Prefer reusable triggers from `mage.abilities.common`: `EntersBattlefieldTriggeredAbility`, `DiesTriggeredAbility`, `BeginningOfUpkeepTriggeredAbility`, `AttacksTriggeredAbility`, `DealsDamageToAPlayerTriggeredAbility`, `SpellCastControllerTriggeredAbility`, etc.
* `StaticAbility`/`SimpleStaticAbility` apply rules or continuous effects without using the stack. Many evergreen mechanics should be keyword singletons (`FlyingAbility.getInstance()`), not hand-built static effects.
* Mana abilities are in `mage.abilities.mana`: use `GreenManaAbility`, `ColorlessManaAbility`, `SimpleManaAbility`, or conditional/filter variants. They follow special mana-ability rules.
* `LoyaltyAbility` combines a loyalty cost (positive/negative integer) with an effect.
* Alternative costs are generally ability objects/`AlternativeSourceCosts` plus a cost object; additional costs are appended to the spell ability with `addCost`. Reuse keyword implementations for kicker, buyback, flashback, evoke, spectacle, casualty, etc. Do not merely reduce the printed mana cost.
* Optional costs are tracked so resolution can ask `getOptionalCosts()`/`getCosts()` or use a condition such as `KickedCondition`. Copy the pattern of a card with the same keyword.

Trigger wording matters: an intervening-if condition is checked both when triggering and resolving, while an ordinary condition inside the effect is generally checked only on resolution. Use constructors that accept a `Condition` when that is the rules meaning. “May” may be an optional trigger or optional effect; identify who chooses and when.

## 5. Targets

A `Target` is a choice slot with legality, minimum/maximum choices, and chosen IDs. A `Filter` is only the reusable membership rule. Effects normally read their target through a `TargetPointer`; adding a filter alone does not target anything.

Common classes:

* `TargetPermanent`, `TargetCreaturePermanent`, `TargetControlledPermanent`, `TargetControlledCreaturePermanent`, `TargetLandPermanent`, `TargetArtifactPermanent` select battlefield permanents.
* `TargetPlayer`, `TargetOpponent`, and `TargetAnyTarget` select players/opponents or the rules concept “any target.” `TargetAnyTarget` includes creatures, players, and planeswalkers as implemented by current rules.
* `TargetCard` is a base/card choice and commonly appears as `TargetCardInYourGraveyard`, `TargetCardInHand`, or `TargetCardInLibrary`; zone is explicit.
* `TargetSpell`, `TargetStackObject`, `TargetActivatedAbility`, `TargetActivatedOrTriggeredAbility`, and `TargetSpellOrPermanent` select stack objects/abilities where appropriate.
* Amount-distribution targets (`TargetCreaturePermanentAmount`, `TargetAnyTargetAmount`) also record divided values.

Typical sequence:

```java
Ability ability = new EntersBattlefieldTriggeredAbility(new ReturnToHandTargetEffect());
ability.addTarget(new TargetCreaturePermanent());
this.addAbility(ability);
```

Separate `addTarget` calls create separate target groups. A target with `(min, max, filter, ...)` represents multiple selections in one group; verify whether duplicates and “different targets” are allowed. “Up to one” normally means minimum zero, maximum one—not an `OptionalEffect` around a mandatory target. Add targets to the same mode/effect-bearing ability. If an effect uses a non-default pointer, set it explicitly. Target legality is checked on announcement and resolution; a spell with all targets illegal does not resolve.

Common errors: using `TargetPlayer` where Oracle says opponent; using battlefield target for a graveyard card; forgetting “you control”; targeting when Oracle says “choose” (hexproof should not interfere); missing `another`; allowing the same object twice; and assuming effect 2 automatically uses effect 1's target.

## 6. Filters and predicates

Filters describe eligible objects and build readable rules text. Bases include `FilterPermanent`, `FilterCreaturePermanent`, `FilterControlledCreaturePermanent`, `FilterCard`, `FilterSpell`, `FilterPlayer`, and common prebuilt filters in [`mage/filter/common`](../Mage/src/main/java/mage/filter/common).

Composition usually looks like:

```java
private static final FilterCreaturePermanent filter =
        new FilterCreaturePermanent("another creature you control");
static {
    filter.add(ControllerPredicate.YOU);
    filter.add(AnotherPredicate.instance);
}
```

Predicates are small tests. Current reusable families include `CardTypePredicate`, `SubTypePredicate`, `SuperTypePredicate`, `ColorPredicate`, `ManaValuePredicate`, `NamePredicate`, `AbilityPredicate`, `TappedPredicate`, `TokenPredicate`, `ControllerPredicate`, `OwnerPredicate`, `AnotherPredicate`, and comparison predicates. Inspect packages under [`mage/filter/predicate`](../Mage/src/main/java/mage/filter/predicate).

Use `static final` for immutable, shared filters whose description and predicates never vary. Do not mutate such a filter later. Use an instance/local filter when text or membership depends on constructor input/card state. Prefer specialized common filters where they encode controller or type correctly. A filter does not enforce source exclusion unless `AnotherPredicate` (or equivalent source-aware logic) is present.

## 7. Dynamic values

`DynamicValue` computes an integer from `Game` and an `Ability` at the correct time. Use it whenever X/count/power/life cannot safely be fixed in the constructor. Reusable values in [`dynamicvalue/common`](../Mage/src/main/java/mage/abilities/dynamicvalue/common) include:

* `StaticValue` for a fixed value where an API requires `DynamicValue`;
* `GetXValue` (the current equivalent to colloquial “XValue”) for X paid/defined by the source;
* `PermanentsOnBattlefieldCount(filter)` and specialized `CreaturesYouControlCount`, `LandsYouControlCount`;
* `CardsInControllerHandCount`, `CardsInTargetHandCount`, graveyard/library/exile counts;
* `CountersCount`, `CountersSourceCount`, `CountersControllerCount`;
* `SourcePermanentPowerValue`/`SourcePermanentToughnessValue`;
* `ManaValueInGraveyard`, `PermanentEnteringBattlefieldManaValue`, and mana-spent values;
* `OpponentsCount`, devotion/domain/party counts, and saved-result values.

There is no justification for inventing a local count class until these and usages have been searched. Check whether the value is evaluated on announcement, payment, trigger, or resolution and whether it needs LKI. A dynamic amount shared by multiple effects may need a `FixedTarget`/saved value so later state changes do not recalculate a number the rules locked earlier.

## 8. Conditions

`Condition` answers a rules question in game context. Reusable conditions are in [`mage/abilities/condition/common`](../Mage/src/main/java/mage/abilities/condition/common): `KickedCondition`, `MorbidCondition`, `DeliriumCondition`, `RevoltCondition`, `CardsInHandCondition`, `ControlsPermanentCondition`, `SourceTappedCondition`, `FatefulHourCondition`, `ThresholdCondition`, `CastFromGraveyardSourceCondition`, and many mechanic-specific conditions.

Decorators in [`mage/abilities/decorator`](../Mage/src/main/java/mage/abilities/decorator) include:

* `ConditionalEffect`: general effect decoration;
* `ConditionalOneShotEffect`: choose one one-shot result (optional otherwise-effect overloads);
* `ConditionalContinuousEffect`: apply a continuous effect while condition is true.

Use a trigger's condition facility for intervening-if. Use a conditional effect for a resolution-time “if.” Create a custom condition only if the fact can be computed directly and no reusable condition expresses it; if history is required, it may need a Watcher rather than repeatedly scanning current state.

## 9. Watchers

A `Watcher` is copied game memory updated from events: cards drawn/cast, permanents sacrificed, damage, attacks, prior turns, and similar facts no longer derivable from current objects. Shared watchers live in [`Mage/src/main/java/mage/watchers/common`](../Mage/src/main/java/mage/watchers/common), including `CardsDrawnThisTurnWatcher`, `CreaturesDiedWatcher`, `FirstSpellCastThisTurnWatcher`, `PermanentsSacrificedWatcher`, `PlayerLostLifeWatcher`, `CastFromGraveyardWatcher`, `OnceEachTurnCastWatcher`, and `RevoltWatcher`.

Cards add a watcher to an ability/card only where registration is required; many common abilities/conditions arrange it themselves. Search usages, not just the watcher definition. Tests in [`Mage.Tests/.../cards/watchers`](../Mage.Tests/src/test/java/org/mage/test/cards/watchers) demonstrate expected reset/copy behavior. [`TempleOfPowerTest.java`](../Mage.Tests/src/test/java/org/mage/test/cards/watchers/TempleOfPowerTest.java) and [`ZuberasTest.java`](../Mage.Tests/src/test/java/org/mage/test/cards/watchers/ZuberasTest.java) are focused examples.

A card-local custom watcher is appropriate only for truly card-specific historical data; search `extends Watcher` under `Mage.Sets/src/mage/cards` for live examples and study its `watch`, reset scope, copy constructor, and `copy()`. Common mistakes are registering it too late, wrong event type, comparing the wrong player/source, failing turn reset, failing to deep-copy collections, recording replaced events, or using a watcher for facts available from `Game` right now.

## 10. Game state, zones, and identity

[`Zone.java`](../Mage/src/main/java/mage/constants/Zone.java) currently defines `HAND`, `GRAVEYARD`, `LIBRARY`, `BATTLEFIELD`, `STACK`, `EXILED`, `ALL`, `OUTSIDE`, and `COMMAND`. `BATTLEFIELD`, `GRAVEYARD`, `STACK`, `EXILED`, and `COMMAND` are public; hand/library are hidden. `ALL` is a matcher, not a physical destination. `OUTSIDE` supports objects outside the game.

Zone changes are events and generally produce a new game object identity/state. Use the engine move/exile/return effects and zone-change counters; do not keep a `Permanent`/`Card` reference and assume it represents the object after it moves. A blinked permanent is new. A card moved graveyard-to-battlefield is not the graveyard object for continuous tracking.

Last Known Information (LKI) is needed when rules ask about characteristics immediately before an object left (power of a dead creature, controller of a departed permanent). Use event data, `getLastKnownInformation`, or a proven analogous implementation. Querying the current battlefield after the object left returns null or the wrong incarnation. `sourceId` identifies the ability source, not its target; `controllerId` and owner ID answer different questions.

## 11. Duration

[`Duration.java`](../Mage/src/main/java/mage/constants/Duration.java) is definitive. Current values are `OneUse`, `EndOfGame`, `WhileOnBattlefield`, `WhileControlled`, `WhileOnStack`, `WhileInGraveyard`, `EndOfTurn`, `UntilYourNextTurn`, `UntilYourNextEndStep`, `UntilNextEndStep`, `UntilEndCombatOfYourNextTurn`, `UntilYourNextUpkeepStep`, `UntilEndOfYourNextTurn`, `UntilSourceLeavesBattlefield`, `EndOfCombat`, `EndOfStep`, and `Custom`.

Durations also encode whether source zone change invalidates an effect and whether controller is fixed. `Custom` requires the effect to implement expiration correctly. `OneUse` is not a synonym for one-shot: it is for a continuing effect consumed once. Avoid accidentally using `WhileOnBattlefield` for an effect created by a resolving instant, or `EndOfTurn` for “until your next turn.” Layered effects also need the correct `Layer`/`SubLayer`; copy a close analogue.

### Face-down permanents: use the modern copy-layer implementation

For a new effect that turns one permanent face down, prefer
`BecomesFaceDownCreatureEffect`. Read that class and several current callers before
writing card-local logic. `FaceDownType.MANUAL` is appropriate when a resolving
spell or ability turns an existing battlefield permanent face down, rather than
casting, manifesting, disguising, or cloaking it. Supply a
`MageObjectReference` and a duration that tracks the same battlefield object.

`BecomesFaceDownCreatureEffect` establishes the canonical face-down
characteristics in the copy layer. While face down, the object must lose or hide
its original name, supertypes, card types, subtypes, color, rules-text abilities,
and mana cost, and receive the appropriate base 2/2 characteristics. It also
preserves face-down-compatible infrastructure, including applicable ways to turn
a morph card face up. Do not approximate this by setting power/toughness and then
removing selected properties in `TypeChangingEffects_4`: doing so can leave
printed characteristics visible, interacts incorrectly with later layers, and
can break face-up mechanics.

`BecomesFaceDownCreatureAllEffect` is a legacy implementation with explicit
TODO/old behavior. It may still be required by existing multi-permanent cards,
but it should not be selected or copied as the default pattern for a new
single-target card.

Some cards define special face-down characteristics beyond the canonical ones.
For example, Cyber Conversion first applies the modern canonical face-down
effect, then layers “Artifact Creature — Cyberman” on top in the face-down copy
sublayer. That additional effect must check that the same object is still face
down and expire if it turns face up or changes zones. It must not replace the
canonical effect.

Face-down tests must go beyond checking `isFaceDown` and 2/2. Verify the empty
name and mana cost, colorlessness, removal of every old supertype and subtype,
removal of printed abilities, exact new types/subtypes, and object identity after
a zone change. When relevant, turn a morph permanent face up and verify both
that its normal characteristics return and that special face-down
characteristics disappear. Include a transformable permanent case.

## 12. Copy/clone pattern

Game simulation, rollback, AI, and copied game states require independent card/ability/effect objects. The standard card pattern is:

```java
private CardName(final CardName card) {
    super(card);
    // copy card-specific mutable fields here
}
@Override
public CardName copy() {
    return new CardName(this);
}
```

Do not call the public constructor from `copy()`: that loses runtime state and may duplicate initialization. Immutable singleton fields need not be cloned; mutable lists/maps/custom choices/watchers usually do. Every custom `EffectImpl`, `AbilityImpl`, `Watcher`, and mutable helper likewise needs a correct copy constructor and `copy()` where its base contract requires it.

## 13. Set registration

A set is a singleton `ExpansionSet` subclass under [`Mage.Sets/src/mage/sets`](../Mage.Sets/src/mage/sets). [`ZendikarRising.java`](../Mage.Sets/src/mage/sets/ZendikarRising.java) illustrates:

```java
super("Zendikar Rising", "ZNR", ExpansionSet.buildDate(2020, 9, 25), SetType.EXPANSION);
cards.add(new SetCardInfo("Murder", 47, Rarity.COMMON, mage.cards.m.Murder.class));
```

The `super` call establishes set name, code, release date, and type. `SetCardInfo` registers exact card name, collector number, rarity, and implementation class. Collector numbers may be integers or strings such as `"9*"` (see [`ZendikarPromos.java`](../Mage.Sets/src/mage/sets/ZendikarPromos.java)); use the printed value. Variants/reprints may add multiple entries pointing to the same implementation with variation constants such as `NON_FULL_USE_VARIOUS`. Rarity belongs to each printing.

For a new card in an existing set, normally add its implementation and one `SetCardInfo` per relevant printing in existing set file(s), then add tests. For a reprint of an already implemented card, only set registration may be needed. Verify with `rg 'new SetCardInfo\("Exact Name"' Mage.Sets/src/mage/sets`, the repository's verify tooling/tests, and card lookup tests—never infer registration from class existence.

## 14. Card tests

Behavior tests are grouped by mechanic/set under [`Mage.Tests/src/test/java/org/mage/test/cards`](../Mage.Tests/src/test/java/org/mage/test/cards). Most extend a base such as `CardTestPlayerBase` or `CardTestCommander4Players`, found beneath [`org/mage/test`](../Mage.Tests/src/test/java/org/mage/test). Read the base signatures before writing a test.

Typical scenario:

```java
addCard(Zone.BATTLEFIELD, playerA, "Mountain", 1);
addCard(Zone.HAND, playerA, "Lightning Bolt");
addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Lightning Bolt", "Grizzly Bears");
setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
execute();
assertGraveyardCount(playerB, "Grizzly Bears", 1);
```

The framework's actual overloads allow turn/step scheduling and choices. The base normally supplies `playerA` and `playerB` (special multiplayer bases configure more players); `addPlayer` is not the ordinary card-test setup call in this checkout. Common tools include `addCard(zone, player, name, count)`, `castSpell`, `activateAbility`, `setChoice`, `setModeChoice`, `setTarget`, `passPhase`, `setStopAt`, and `execute`. Assertions include `assertPermanentCount`, `assertLife`, `assertGraveyardCount`, `assertHandCount`, `assertLibraryCount`, `assertExileCount`, `assertTapped`, `assertPowerToughness`, `assertCounterCount`, `assertAbility`, and `assertPlayerHasAbility`.

Use real tests such as [`LightningBoltTest.java`](../Mage.Tests/src/test/java/org/mage/test/cards/abilities/oneshot/damage/LightningBoltTest.java) demonstrates direct damage; locate other cards/mechanics with `rg`. [`TestActivatedContinuousEffect.java`](../Mage.Tests/src/test/java/org/mage/test/serverside/cards/effects/TestActivatedContinuousEffect.java) demonstrates lower-level continuous-effect testing, while watcher tests cited above cover history. Tests should prove success, restriction/illegal case, timing, duration cleanup, multiplayer/controller behavior, and edge cases for any nontrivial implementation. Run the narrow Maven test first, then the appropriate module suite.

## 15. Common implementation mistakes

1. **Wrong zone:** battlefield target used for a graveyard card, or ability active in the default zone rather than graveyard/command/exile.
2. **Wrong person:** owner, controller, source controller, target controller, active player, and defending player are distinct.
3. **Wrong ID:** `sourceId`, target ID, original source ID, and zone-change ID are not interchangeable.
4. **No/incorrect target:** filter added without `addTarget`, “choose” incorrectly targets, or “up to” remains mandatory.
5. **Overbroad filter:** missing subtype, “nonland,” “you control,” opponent, nontoken, or `AnotherPredicate`.
6. **Bad target sharing:** later effects read a different/default target pointer.
7. **Wrong duration/layer:** temporary boost persists; type/color/ability/P/T modifications occur in wrong continuous layer.
8. **Broken copying:** custom fields share mutable state or disappear in copied games.
9. **Stale object after zone change:** old permanent/card reference is reused for the new object.
10. **Trigger multiplicity:** event is observed once per object/player when Oracle calls for once, or vice versa.
11. **“If you do” modeled independently:** second action happens even when the optional/cost action failed.
12. **Intervening-if modeled only at resolution:** trigger incorrectly enters stack.
13. **Optional cost guessed from mana paid:** use the engine's recorded optional-cost state.
14. **X calculated at the wrong time:** distinguish chosen X, mana actually spent, and changing battlefield counts.
15. **LKI ignored:** departed source's power/controller is queried from current state.
16. **Reinvented infrastructure:** bespoke effects/watchers duplicate a battle-tested common class.
17. **Manual state mutation:** bypasses events, replacement effects, triggers, and zone identity.
18. **Source/controller assumption:** source may have left or changed control before resolution.
19. **Text-only correctness:** `setText` can make UI text look right while rules behavior remains wrong.
20. **Insufficient tests:** happy path passes but legality, cleanup, optional decline, multiple events, or copied state fails.

## 16. Reuse-first rule (mandatory)

Before creating any Ability, Effect, Watcher, Condition, Filter, Predicate, DynamicValue, utility, or helper:

1. Search exact/similar Oracle wording among cards: `rg -i 'distinctive phrase' Mage.Sets/src/mage/cards`.
2. Search several distinctive fragments, including rules synonyms and the operative verb.
3. Search class names and concepts under `Mage/src/main/java/mage/abilities`, `filter`, `target`, and `watchers`.
4. Inspect several representative callers; prefer the newest rules wording and implementations reused by many cards, not a lone legacy workaround.
5. Open the infrastructure class itself and verify constructor signatures, target pointer, zone, duration, copying, and text generation.
6. Reuse it. Create new infrastructure only after documenting why every candidate is unsuitable.

Also use `rg 'new CandidateEffect|CandidateEffect.getInstance' Mage.Sets/src/mage/cards` to learn the normal idiom. Reuse improves replacement/trigger interaction, multiplayer semantics, copying, UI text, and test coverage. It reduces divergence and merge conflicts when this custom branch later consumes upstream changes.

## 17. Coding conventions observed here

* Package is `mage.cards.<lowercase-first-letter>`; public card name matches filename and normalized printed name.
* Imports are explicit and grouped by formatter; never wrap imports in exception handling and avoid wildcard imports unless surrounding code mandates them.
* Typical field/constructor order: static filters/values, public constructor, private copy constructor, `copy()`, then card-specific helper classes.
* Card classes are normally `public final`; helpers are usually package-private top-level classes below the card, named `<CardName>Effect/Ability/Watcher/Condition`.
* Use `static final` filters/dynamic singleton values when immutable; initialize complex filters in a static block.
* Constructor order is `super`, sub/supertypes, P/T or loyalty, then abilities in Oracle order.
* Oracle comments commonly precede each ability. `@author` appears in class Javadoc. Comments explain rules decisions, not obvious Java.
* Prefer existing keyword `getInstance()` singleton abilities where available.
* `setText(...)` overrides generated rules text; use only when generated text is inaccurate/awkward. `getText(Mode)` in custom effects should derive correct text; do not use text to conceal mismatched behavior.
* `Hint` implementations belong on abilities when the UI/AI needs a computed value; existing common hints should be preferred.
* `getEffects()` is used when a custom ability needs to inspect/modify contained effects; preserve indexes only when the sequence is stable and explained.
* Every copyable class follows copy-constructor/`copy()` convention.
* Follow four-space indentation and nearby wrapping. Do not reformat unrelated code.

## 18. Oracle-text decision tree

1. **Is it a spell instruction?** Add effects/targets to `getSpellAbility()` in printed order.
2. **“When/Whenever/At …”** Use a triggered ability. “At the beginning of your upkeep/end step” suggests the matching reusable beginning trigger. Determine controller, active player, and trigger zone.
3. **“When [this] enters”** use `EntersBattlefieldTriggeredAbility`; “dies” uses `DiesTriggeredAbility`; do not approximate dies with any graveyard move.
4. **“If” in the trigger clause before the comma** may be intervening-if: use the trigger condition so it checks twice.
5. **“As [this] enters” / “enters with”** search as-enters replacement/enters-with-counter abilities; an ETB trigger is too late.
6. **“If … would … instead”** is a replacement effect matching the event, not a trigger after it.
7. **“You may”** use optional trigger/effect/cost according to when the choice occurs and whether targets are still selected when declining.
8. **“If you do”** chain the consequence to successful completion/payment, not a separate unconditional effect.
9. **“Unless [player] pays”** use an unless-cost/counter-unless effect with the correct payer and timing.
10. **“Target”** add a target of the exact zone/object/player category. No word “target” usually means do not add one.
11. **“Up to one/N target(s)”** target min is zero; mode can be cast/chosen without a target unless other rules prevent it.
12. **“Another”** exclude the source with a source-aware predicate.
13. **“You control/own”** use controller/owner-aware filter; these are not interchangeable.
14. **“Each/all”** usually use a filtered all-effect, not targets; protection/hexproof behave differently.
15. **“For each/equal to/number of”** use a reusable `DynamicValue` or deliberate iteration if each item causes a separate choice/event.
16. **“X”** determine whether X is chosen, paid, target count, or defined characteristic; use the matching X value/cost.
17. **“Until end of turn/combat/your next turn”** select the exact `Duration`.
18. **“As long as/while”** use a static/conditional continuous effect with recalculation, usually `WhileOnBattlefield`.
19. **“Gets +N/+M”** choose single target, source, controlled group, or continuous boost class and correct duration.
20. **“Has/gains/loses [ability]”** use gain/lose ability continuous effects in the ability layer.
21. **“Choose one/two”** use `Mode`; verify min/max selections, repeated modes, and target-per-mode behavior.
22. **“Choose a color/type/name”** use the appropriate `Choice` and store it in game/ability state that copies correctly.
23. **“Additional cost”** call `addCost`; **“rather than pay”** requires an alternative-cost framework; **cost reduction** needs a cost-adjusting ability.
24. **“Once each turn”** use a reusable max-usage/restriction pattern; **“first time each turn”** search first-event watchers/triggers.
25. **“This turn” about past events** search Watchers; current state alone is usually insufficient.
26. **“That card/creature/amount”** preserve identity/value through target pointers, LKI, or saved dynamic values.
27. **“Return it” after a zone move** resolve the new object using zone-change-aware engine effects; do not reuse old reference.
28. **“You may cast/play” from exile/graveyard** use permission/as-though effects with duration and alternate timing/cost rules.
29. **“Copy”** distinguish spell, ability, permanent, card, and token copy; use the corresponding family.
30. **“Without paying its mana cost”** is an alternative casting cost, not setting mana to zero manually.

## 19. Detailed representative implementations

### Very simple spell — Lightning Bolt
File: [`Mage.Sets/src/mage/cards/l/LightningBolt.java`](../Mage.Sets/src/mage/cards/l/LightningBolt.java).

* `extends CardImpl` chooses the ordinary single-faced card base.
* `super(... INSTANT, "{R}")` supplies printed type/cost.
* `TargetAnyTarget` implements the modern rules category.
* `DamageTargetEffect(3)` consumes that target at resolution.
* The private copy constructor and `copy()` are boilerplate but required.

### Simple creature/mana ability — Elvish Mystic
File: [`Mage.Sets/src/mage/cards/e/ElvishMystic.java`](../Mage.Sets/src/mage/cards/e/ElvishMystic.java).

* Creature type and `{G}` are set in `super`.
* ELF and DRUID are separate subtypes.
* `MageInt(1)` sets printed P/T.
* `GreenManaAbility` already packages tap cost, produced mana, mana-ability semantics, and text. This is the reuse-first ideal.

### ETB trigger — Man-o'-War
File: [`Mage.Sets/src/mage/cards/m/ManOWar.java`](../Mage.Sets/src/mage/cards/m/ManOWar.java).

* The trigger object holds `ReturnToHandTargetEffect`.
* `TargetCreaturePermanent` supplies one battlefield creature target.
* Target is added to the trigger, then the trigger to the card.
* It does not say “another,” so the creature can target itself while the trigger is on the stack.

### Static keywords — Serra Angel
File: [`Mage.Sets/src/mage/cards/s/SerraAngel.java`](../Mage.Sets/src/mage/cards/s/SerraAngel.java).

* Printed 4/4 Angel characteristics are constructor fields.
* Flying and vigilance reuse singleton keyword abilities. No custom continuous effects are needed.

### Activated ability — Prodigal Sorcerer
File: [`Mage.Sets/src/mage/cards/p/ProdigalSorcerer.java`](../Mage.Sets/src/mage/cards/p/ProdigalSorcerer.java).

Read the constructor as `cost: effect`: the reusable activated ability/effect and `TargetAnyTarget` encode tapping to deal damage. Compare its exact current code before implementing any pinger; summoning sickness and tap-cost behavior come from the engine, not card-local checks.

### Static continuous effect — Glorious Anthem
File: [`Mage.Sets/src/mage/cards/g/GloriousAnthem.java`](../Mage.Sets/src/mage/cards/g/GloriousAnthem.java).

Its static ability uses a controlled-creature filter and a continuous boost for `WhileOnBattlefield`. This illustrates why a global static modifier is not a one-shot loop over current creatures: newly entered creatures must also receive it, and layer recalculation must remain correct.

### Moderately complex replacement/static card — Rest in Peace
File: [`Mage.Sets/src/mage/cards/r/RestInPeace.java`](../Mage.Sets/src/mage/cards/r/RestInPeace.java).

The ETB one-shot exile and ongoing graveyard replacement are separate rules sentences and therefore separate abilities/effects. The replacement observes moves that *would* put cards into graveyards and changes their destination before triggers dependent on the original event. Reuse its existing replacement infrastructure rather than manually moving cards after they arrive.

These examples are representative patterns, not templates to copy blindly. Always compare Oracle wording and inspect the exact class in this checkout.

## 20. Future implementation checklist

- [ ] Oracle text, rulings, printing/type line, cost, P/T/loyalty, and color identity verified.
- [ ] Exact and distinctive Oracle fragments searched across existing cards.
- [ ] Reusable abilities/effects/watchers/conditions/filters/predicates/dynamic values searched and opened.
- [ ] Representative current implementations compared; legacy outlier avoided.
- [ ] Correct card base, package, constructor, types/subtypes/supertypes, and ability order used.
- [ ] Every target's type, zone, controller/owner restriction, min/max, uniqueness, and pointer verified.
- [ ] Costs, optional/alternative/additional cost state, choices, modes, and X timing verified.
- [ ] Trigger event, trigger zone, multiplicity, intervening-if, and “if you do” semantics verified.
- [ ] Zones, owner/controller, object identity, zone-change counters, and LKI verified.
- [ ] Continuous layer, dependencies, and exact `Duration` verified.
- [ ] Copy constructor/`copy()` and every custom mutable field verified.
- [ ] Existing-set registration, collector number (including suffix), rarity, variants, and reprints verified.
- [ ] Focused tests added for success and meaningful failure/edge cases.
- [ ] Focused tests run; relevant module compile/build run where appropriate.
- [ ] `git diff --check`, `git diff`, and changed-file list reviewed.
- [ ] No generated, source, test, engine, formatting, or unrelated changes included.
