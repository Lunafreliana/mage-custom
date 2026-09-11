package mage.cards.t;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.DelayedTriggeredAbility;
import mage.abilities.common.ActivateAsSorceryActivatedAbility;
import mage.abilities.common.SkipUntapOptionalAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousRuleModifyingEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.filter.common.FilterNonlandPermanent;
import mage.filter.predicate.mageobject.AnotherPredicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.game.permanent.Permanent;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 * @author VibecodingQueens
 */
public final class ThePandorica extends CardImpl {

    private static final FilterNonlandPermanent filter
            = new FilterNonlandPermanent("another target nonland permanent");

    static {
        filter.add(AnotherPredicate.instance);
    }

    public ThePandorica(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{2}{W}");

        this.supertype.add(SuperType.LEGENDARY);

        // You may choose not to untap The Pandorica during your untap step.
        this.addAbility(new SkipUntapOptionalAbility());

        // {1}{W}, {T}: Untap another target nonland permanent, then it phases out. It can't phase in for as long as The Pandorica remains tapped. When The Pandorica becomes untapped or leaves the battlefield, that permanent phases in. Activate only as a sorcery.
        Ability ability = new ActivateAsSorceryActivatedAbility(
                new ThePandoricaEffect(), new ManaCostsImpl<>("{1}{W}")
        );
        ability.addCost(new TapSourceCost());
        ability.addTarget(new TargetPermanent(filter));
        this.addAbility(ability);
    }

    private ThePandorica(final ThePandorica card) {
        super(card);
    }

    @Override
    public ThePandorica copy() {
        return new ThePandorica(this);
    }
}

class ThePandoricaEffect extends OneShotEffect {

    ThePandoricaEffect() {
        super(Outcome.Detriment);
        staticText = "untap another target nonland permanent, then it phases out. "
                + "It can't phase in for as long as {this} remains tapped. "
                + "When {this} becomes untapped or leaves the battlefield, that permanent phases in";
    }

    private ThePandoricaEffect(final ThePandoricaEffect effect) {
        super(effect);
    }

    @Override
    public ThePandoricaEffect copy() {
        return new ThePandoricaEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(source.getFirstTarget());
        if (permanent == null) {
            return false;
        }
        permanent.untap(game);
        MageObjectReference mor = new MageObjectReference(permanent, game);
        if (!permanent.phaseOut(game)) {
            return false;
        }
        game.addEffect(new ThePandoricaPhaseInPreventionEffect(mor), source);
        game.addDelayedTriggeredAbility(new ThePandoricaDelayedTriggeredAbility(mor), source);
        return true;
    }
}

class ThePandoricaPhaseInPreventionEffect extends ContinuousRuleModifyingEffectImpl {

    private final MageObjectReference mor;

    ThePandoricaPhaseInPreventionEffect(MageObjectReference mor) {
        super(Duration.Custom, Outcome.Neutral);
        this.mor = mor;
    }

    private ThePandoricaPhaseInPreventionEffect(final ThePandoricaPhaseInPreventionEffect effect) {
        super(effect);
        this.mor = effect.mor;
    }

    @Override
    public ThePandoricaPhaseInPreventionEffect copy() {
        return new ThePandoricaPhaseInPreventionEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.PHASE_IN;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        Permanent pandorica = source.getSourcePermanentIfItStillExists(game);
        return pandorica != null
                && pandorica.isTapped()
                && mor.refersTo(event.getTargetId(), game);
    }

    @Override
    public boolean isInactive(Ability source, Game game) {
        Permanent pandorica = source.getSourcePermanentIfItStillExists(game);
        return pandorica == null || !pandorica.isTapped();
    }
}

class ThePandoricaDelayedTriggeredAbility extends DelayedTriggeredAbility {

    ThePandoricaDelayedTriggeredAbility(MageObjectReference mor) {
        super(new ThePandoricaPhaseInEffect(mor), Duration.Custom, true);
    }

    private ThePandoricaDelayedTriggeredAbility(final ThePandoricaDelayedTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public ThePandoricaDelayedTriggeredAbility copy() {
        return new ThePandoricaDelayedTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.UNTAPPED
                || event.getType() == GameEvent.EventType.ZONE_CHANGE;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!getSourceId().equals(event.getTargetId())) {
            return false;
        }
        return event.getType() == GameEvent.EventType.UNTAPPED
                || ((ZoneChangeEvent) event).getFromZone() == Zone.BATTLEFIELD;
    }

    @Override
    public String getRule() {
        return "When {this} becomes untapped or leaves the battlefield, that permanent phases in.";
    }
}

class ThePandoricaPhaseInEffect extends OneShotEffect {

    private final MageObjectReference mor;

    ThePandoricaPhaseInEffect(MageObjectReference mor) {
        super(Outcome.Benefit);
        this.mor = mor;
    }

    private ThePandoricaPhaseInEffect(final ThePandoricaPhaseInEffect effect) {
        super(effect);
        this.mor = effect.mor;
    }

    @Override
    public ThePandoricaPhaseInEffect copy() {
        return new ThePandoricaPhaseInEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = mor.getPermanent(game);
        return permanent != null && permanent.phaseIn(game);
    }
}
