package mage.cards.t;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.DelayedTriggeredAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousRuleModifyingEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DestroyAllEffect;
import mage.abilities.effects.common.SacrificeSourceEffect;
import mage.abilities.effects.common.UntapTargetEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.common.FilterNonlandPermanent;
import mage.filter.predicate.mageobject.ManaValuePredicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.game.permanent.Permanent;
import mage.target.common.TargetControlledCreaturePermanent;

import java.util.UUID;

/**
 * @author VibecodingQueens
 */
public final class TheMoment extends CardImpl {

    public TheMoment(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{2}");

        this.supertype.add(SuperType.LEGENDARY);

        // At the beginning of your upkeep, put a time counter on The Moment.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(
                new AddCountersSourceEffect(CounterType.TIME.createInstance())
        ));

        // {2}, {T}: Untap target creature you control. It phases out until The Moment leaves the battlefield.
        SimpleActivatedAbility phaseOutAbility = new SimpleActivatedAbility(
                new UntapTargetEffect(), new ManaCostsImpl<>("{2}")
        );
        phaseOutAbility.addCost(new TapSourceCost());
        phaseOutAbility.addEffect(new TheMomentPhaseOutEffect());
        phaseOutAbility.addTarget(new TargetControlledCreaturePermanent());
        this.addAbility(phaseOutAbility);

        // {3}, {T}: Destroy each nonland permanent with mana value less than or equal to the number of time counters on The Moment. Then sacrifice The Moment. Activate only as a sorcery.
        SimpleActivatedAbility destroyAbility = new SimpleActivatedAbility(
                new TheMomentDestroyEffect(), new ManaCostsImpl<>("{3}")
        );
        destroyAbility.addCost(new TapSourceCost());
        destroyAbility.setTiming(TimingRule.SORCERY);
        this.addAbility(destroyAbility);
    }

    private TheMoment(final TheMoment card) {
        super(card);
    }

    @Override
    public TheMoment copy() {
        return new TheMoment(this);
    }
}

class TheMomentPhaseOutEffect extends OneShotEffect {

    TheMomentPhaseOutEffect() {
        super(Outcome.Detriment);
        staticText = "It phases out until {this} leaves the battlefield";
    }

    private TheMomentPhaseOutEffect(final TheMomentPhaseOutEffect effect) {
        super(effect);
    }

    @Override
    public TheMomentPhaseOutEffect copy() {
        return new TheMomentPhaseOutEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent sourcePermanent = source.getSourcePermanentIfItStillExists(game);
        Permanent permanent = game.getPermanent(source.getFirstTarget());
        if (sourcePermanent == null || permanent == null) {
            return false;
        }
        MageObjectReference mor = new MageObjectReference(permanent, game);
        permanent.phaseOut(game);
        game.addEffect(new TheMomentPhaseInPreventionEffect(mor), source);
        game.addDelayedTriggeredAbility(new TheMomentLeavesTriggeredAbility(mor), source);
        return true;
    }
}

class TheMomentPhaseInPreventionEffect extends ContinuousRuleModifyingEffectImpl {

    private final MageObjectReference mor;

    TheMomentPhaseInPreventionEffect(MageObjectReference mor) {
        super(Duration.WhileOnBattlefield, Outcome.Neutral);
        this.mor = mor;
    }

    private TheMomentPhaseInPreventionEffect(final TheMomentPhaseInPreventionEffect effect) {
        super(effect);
        this.mor = effect.mor;
    }

    @Override
    public TheMomentPhaseInPreventionEffect copy() {
        return new TheMomentPhaseInPreventionEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.PHASE_IN;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        return source.getSourcePermanentIfItStillExists(game) != null
                && mor.refersTo(event.getTargetId(), game);
    }
}

class TheMomentLeavesTriggeredAbility extends DelayedTriggeredAbility {

    TheMomentLeavesTriggeredAbility(MageObjectReference mor) {
        super(new TheMomentPhaseInEffect(mor), Duration.Custom, true, false);
        this.usesStack = false;
    }

    private TheMomentLeavesTriggeredAbility(final TheMomentLeavesTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public TheMomentLeavesTriggeredAbility copy() {
        return new TheMomentLeavesTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ZONE_CHANGE;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return event.getTargetId().equals(this.getSourceId())
                && ((ZoneChangeEvent) event).getFromZone() == Zone.BATTLEFIELD;
    }
}

class TheMomentPhaseInEffect extends OneShotEffect {

    private final MageObjectReference mor;

    TheMomentPhaseInEffect(MageObjectReference mor) {
        super(Outcome.Benefit);
        this.mor = mor;
    }

    private TheMomentPhaseInEffect(final TheMomentPhaseInEffect effect) {
        super(effect);
        this.mor = effect.mor;
    }

    @Override
    public TheMomentPhaseInEffect copy() {
        return new TheMomentPhaseInEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = mor.getPermanent(game);
        return permanent != null && permanent.phaseIn(game);
    }
}

class TheMomentDestroyEffect extends OneShotEffect {

    TheMomentDestroyEffect() {
        super(Outcome.DestroyPermanent);
        staticText = "destroy each nonland permanent with mana value less than or equal to the number "
                + "of time counters on {this}. Then sacrifice {this}";
    }

    private TheMomentDestroyEffect(final TheMomentDestroyEffect effect) {
        super(effect);
    }

    @Override
    public TheMomentDestroyEffect copy() {
        return new TheMomentDestroyEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (permanent == null) {
            return false;
        }
        int count = permanent.getCounters(game).getCount(CounterType.TIME);
        FilterNonlandPermanent filter = new FilterNonlandPermanent("each nonland permanent with mana value " + count + " or less");
        filter.add(new ManaValuePredicate(ComparisonType.OR_LESS, count));
        new DestroyAllEffect(filter).apply(game, source);
        new SacrificeSourceEffect().apply(game, source);
        return true;
    }
}
