package mage.cards.h;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.DelayedTriggeredAbility;
import mage.abilities.Mode;
import mage.abilities.SpellAbility;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.RemoveCountersSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.RequirementEffect;
import mage.abilities.effects.common.ChooseNewTargetsTargetEffect;
import mage.abilities.effects.common.LookAtTargetPlayerHandEffect;
import mage.abilities.effects.common.PreventDamageByTargetEffect;
import mage.abilities.effects.common.combat.AttacksIfAbleTargetEffect;
import mage.abilities.effects.common.cost.CostModificationEffectImpl;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.CostModificationType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.WatcherScope;
import mage.constants.Zone;
import mage.counters.Counter;
import mage.counters.CounterType;
import mage.filter.FilterCard;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.game.stack.Spell;
import mage.players.Player;
import mage.target.TargetPermanent;
import mage.target.common.TargetCardInHand;
import mage.target.common.TargetOpponent;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;
import mage.watchers.Watcher;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Original custom card for CLUN.
 *
 * @author OpenAI
 */
public final class HarumaVeilBeneathTheStorm extends CardImpl {

    public HarumaVeilBeneathTheStorm(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{W}{B}");
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.NINJA);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // Kanchi Taipu — Whenever you look at one or more cards in an opponent's hand or one or more cards in an opponent's hand are revealed, put a perception counter on Haruma.
        this.addAbility(new HarumaKanchiTaipuAbility());

        // {1}{U}: Look at target opponent's hand. Activate only once each turn.
        SimpleActivatedAbility look = new SimpleActivatedAbility(
                new LookAtTargetPlayerHandEffect(), new ManaCostsImpl<>("{1}{U}")
        );
        look.addTarget(new TargetOpponent());
        look.setMaxActivationsPerTurn(1);
        this.addAbility(look);

        // Genjutsu — Remove a perception counter from Haruma: Choose one. Each mode can only target every player once per turn.
        Ability genjutsu = new SimpleActivatedAbility(new HarumaFalseOpeningEffect(),
                new RemoveCountersSourceCost(new Counter(CounterType.PERCEPTION.getName(), 1)));
        genjutsu.withFlavorWord("Genjutsu");
        genjutsu.addTarget(new TargetPermanent(StaticFilters.FILTER_OPPONENTS_PERMANENT_CREATURE));
        genjutsu.getModes().setLimitUsageByOnce(true);
        genjutsu.setModeTag("False Opening");

        Mode mode = new Mode(new HarumaFracturedPerceptionEffect());
        mode.addTarget(new TargetOpponent());
        mode.setModeTag("Fractured Perception");
        genjutsu.addMode(mode);

        mode = new Mode(new HarumaPhantomTechniqueEffect());
        mode.addTarget(new TargetOpponent());
        mode.setModeTag("Phantom Technique");
        genjutsu.addMode(mode);
        this.addAbility(genjutsu, new HarumaRevealedHandWatcher());

        // Healing Arts — At the beginning of your end step, you may reveal any number of cards from your hand. You gain 2 life for each card revealed this way.
        this.addAbility(new BeginningOfEndStepTriggeredAbility(new HarumaHealingArtsEffect(), true)
                .withFlavorWord("Healing Arts"));
    }

    private HarumaVeilBeneathTheStorm(final HarumaVeilBeneathTheStorm card) {
        super(card);
    }

    @Override
    public HarumaVeilBeneathTheStorm copy() {
        return new HarumaVeilBeneathTheStorm(this);
    }
}

class HarumaKanchiTaipuAbility extends TriggeredAbilityImpl {
    HarumaKanchiTaipuAbility() {
        super(Zone.BATTLEFIELD, new HarumaAddPerceptionCounterEffect(), false);
        setTriggerPhrase("<i>Kanchi Taipu</i> — Whenever you look at one or more cards in an opponent's hand or one or more cards in an opponent's hand are revealed, ");
    }
    private HarumaKanchiTaipuAbility(HarumaKanchiTaipuAbility ability) { super(ability); }
    @Override public HarumaKanchiTaipuAbility copy() { return new HarumaKanchiTaipuAbility(this); }
    @Override public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.HAND_LOOKED_AT || event.getType() == GameEvent.EventType.HAND_REVEALED;
    }
    @Override public boolean checkTrigger(GameEvent event, Game game) {
        return game.getOpponents(getControllerId()).contains(event.getTargetId())
                && (event.getType() == GameEvent.EventType.HAND_REVEALED || getControllerId().equals(event.getPlayerId()));
    }
}

class HarumaAddPerceptionCounterEffect extends OneShotEffect {
    HarumaAddPerceptionCounterEffect() { super(Outcome.Benefit); staticText = "put a perception counter on {this}"; }
    private HarumaAddPerceptionCounterEffect(HarumaAddPerceptionCounterEffect effect) { super(effect); }
    @Override public HarumaAddPerceptionCounterEffect copy() { return new HarumaAddPerceptionCounterEffect(this); }
    @Override public boolean apply(Game game, Ability source) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        return permanent != null && permanent.addCounters(CounterType.PERCEPTION.createInstance(), source.getControllerId(), source, game);
    }
}

class HarumaFalseOpeningEffect extends OneShotEffect {
    HarumaFalseOpeningEffect() { super(Outcome.Benefit); staticText = "<i>False Opening</i> — Until end of turn, choose a creature target opponent controls, it attacks this combat if able. Prevent all combat damage that creature would deal this combat"; }
    private HarumaFalseOpeningEffect(HarumaFalseOpeningEffect effect) { super(effect); }
    @Override public HarumaFalseOpeningEffect copy() { return new HarumaFalseOpeningEffect(this); }
    @Override public boolean apply(Game game, Ability source) {
        Permanent creature = game.getPermanent(source.getFirstTarget());
        if (creature == null) return false;
        RequirementEffect attack = new AttacksIfAbleTargetEffect(Duration.EndOfCombat);
        attack.setTargetPointer(new FixedTarget(creature, game));
        game.addEffect(attack, source);
        ContinuousEffect prevent = new PreventDamageByTargetEffect(Duration.EndOfCombat, true);
        prevent.setTargetPointer(new FixedTarget(creature, game));
        game.addEffect(prevent, source);
        return true;
    }
}

class HarumaFracturedPerceptionEffect extends OneShotEffect {
    HarumaFracturedPerceptionEffect() { super(Outcome.Benefit); staticText = "<i>Fractured Perception</i> — Choose a card revealed from target opponent's hand this turn. Spells with that name cost {2} more to cast until your next turn"; }
    private HarumaFracturedPerceptionEffect(HarumaFracturedPerceptionEffect effect) { super(effect); }
    @Override public HarumaFracturedPerceptionEffect copy() { return new HarumaFracturedPerceptionEffect(this); }
    @Override public boolean apply(Game game, Ability source) {
        HarumaRevealedHandWatcher watcher = game.getState().getWatcher(HarumaRevealedHandWatcher.class, source.getSourceId());
        Player controller = game.getPlayer(source.getControllerId());
        Set<String> names = watcher == null ? Collections.emptySet() : watcher.getNames(source.getFirstTarget());
        if (controller == null || names.isEmpty()) return false;
        mage.choices.ChoiceImpl choice = new mage.choices.ChoiceImpl(true);
        choice.setMessage("Choose a card revealed from that opponent's hand this turn");
        choice.setChoices(names);
        if (!controller.choose(Outcome.Benefit, choice, game)) return false;
        game.addEffect(new HarumaNamedSpellCostEffect(choice.getChoice()), source);
        return true;
    }
}

class HarumaNamedSpellCostEffect extends CostModificationEffectImpl {
    private final String name;
    HarumaNamedSpellCostEffect(String name) { super(Duration.UntilYourNextTurn, Outcome.Detriment, CostModificationType.INCREASE_COST); this.name = name; }
    private HarumaNamedSpellCostEffect(HarumaNamedSpellCostEffect effect) { super(effect); this.name = effect.name; }
    @Override public HarumaNamedSpellCostEffect copy() { return new HarumaNamedSpellCostEffect(this); }
    @Override public boolean apply(Game game, Ability source, Ability abilityToModify) { CardUtil.increaseCost(abilityToModify, 2); return true; }
    @Override public boolean applies(Ability abilityToModify, Ability source, Game game) {
        Card card = abilityToModify instanceof SpellAbility ? ((SpellAbility) abilityToModify).getCharacteristics(game) : null;
        return card != null && CardUtil.haveSameNames(card, name, game);
    }
}

class HarumaPhantomTechniqueEffect extends OneShotEffect {
    HarumaPhantomTechniqueEffect() { super(Outcome.Benefit); staticText = "<i>Phantom Technique</i> — Whenever target opponent casts a spell that was revealed from their hand this turn, you may remove a perception counter from {this}. If you do, you may choose new targets for that spell"; }
    private HarumaPhantomTechniqueEffect(HarumaPhantomTechniqueEffect effect) { super(effect); }
    @Override public HarumaPhantomTechniqueEffect copy() { return new HarumaPhantomTechniqueEffect(this); }
    @Override public boolean apply(Game game, Ability source) {
        game.addDelayedTriggeredAbility(new HarumaPhantomDelayedAbility(source.getFirstTarget(), source.getSourceId()), source);
        return true;
    }
}

class HarumaPhantomDelayedAbility extends DelayedTriggeredAbility {
    private final UUID opponentId;
    private final UUID harumaId;
    HarumaPhantomDelayedAbility(UUID opponentId, UUID harumaId) {
        super(new HarumaPhantomRetargetEffect(), Duration.EndOfTurn, false, true);
        this.opponentId = opponentId; this.harumaId = harumaId;
    }
    private HarumaPhantomDelayedAbility(HarumaPhantomDelayedAbility ability) { super(ability); opponentId = ability.opponentId; harumaId = ability.harumaId; }
    @Override public HarumaPhantomDelayedAbility copy() { return new HarumaPhantomDelayedAbility(this); }
    @Override public boolean checkEventType(GameEvent event, Game game) { return event.getType() == GameEvent.EventType.SPELL_CAST; }
    @Override public boolean checkTrigger(GameEvent event, Game game) {
        Spell spell = game.getStack().getSpell(event.getTargetId());
        HarumaRevealedHandWatcher watcher = game.getState().getWatcher(HarumaRevealedHandWatcher.class, harumaId);
        if (spell == null || watcher == null || !opponentId.equals(event.getPlayerId()) || !watcher.getNames(opponentId).contains(spell.getName())) return false;
        getEffects().setTargetPointer(new FixedTarget(spell.getId()));
        return true;
    }
}

class HarumaPhantomRetargetEffect extends OneShotEffect {
    HarumaPhantomRetargetEffect() { super(Outcome.Benefit); }
    private HarumaPhantomRetargetEffect(HarumaPhantomRetargetEffect effect) { super(effect); }
    @Override public HarumaPhantomRetargetEffect copy() { return new HarumaPhantomRetargetEffect(this); }
    @Override public boolean apply(Game game, Ability source) {
        Permanent haruma = game.getPermanent(source.getSourceId());
        if (haruma == null || haruma.getCounters(game).getCount(CounterType.PERCEPTION) < 1) return false;
        haruma.removeCounters(CounterType.PERCEPTION.getName(), 1, source, game);
        return new ChooseNewTargetsTargetEffect().setTargetPointer(getTargetPointer()).apply(game, source);
    }
}

class HarumaHealingArtsEffect extends OneShotEffect {
    HarumaHealingArtsEffect() { super(Outcome.GainLife); staticText = "you may reveal any number of cards from your hand. You gain 2 life for each card revealed this way"; }
    private HarumaHealingArtsEffect(HarumaHealingArtsEffect effect) { super(effect); }
    @Override public HarumaHealingArtsEffect copy() { return new HarumaHealingArtsEffect(this); }
    @Override public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) return false;
        TargetCardInHand target = new TargetCardInHand(0, Integer.MAX_VALUE, new FilterCard("cards to reveal"));
        if (!player.choose(Outcome.GainLife, target, source, game)) return false;
        Cards cards = new CardsImpl(target.getTargets());
        if (!cards.isEmpty()) player.revealCards(source, cards, game);
        player.gainLife(cards.size() * 2, game, source);
        return true;
    }
}

class HarumaRevealedHandWatcher extends Watcher {
    private final Map<UUID, Set<String>> names = new HashMap<>();
    HarumaRevealedHandWatcher() { super(WatcherScope.CARD); }
    @Override public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.HAND_REVEALED && event.getTargetId() != null && event.getData() != null) {
            names.computeIfAbsent(event.getTargetId(), x -> new LinkedHashSet<>()).addAll(Arrays.asList(event.getData().split("\\0", -1)));
        }
    }
    @Override public void reset() { super.reset(); names.clear(); }
    Set<String> getNames(UUID playerId) { return names.getOrDefault(playerId, Collections.emptySet()); }
}
