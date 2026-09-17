package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlaneswalkAwayFromSourceTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.abilities.costs.Cost;
import mage.abilities.costs.Costs;
import mage.abilities.costs.CostsImpl;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.AsThoughEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.RemoveAllCountersAllEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.Card;
import mage.constants.AsThoughEffectType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.AbilityPredicate;
import mage.filter.predicate.permanent.TokenPredicate;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;
import mage.target.common.TargetControlledPermanent;
import mage.target.targetpointer.FixedTarget;

import java.util.UUID;

/**
 * @author The XMage Developers
 */
public final class TheWindyCityPlane extends Plane {

    private static final FilterControlledCreaturePermanent COUNTER_FILTER
            = new FilterControlledCreaturePermanent("nontoken creature you control without flying");
    private static final FilterCreaturePermanent FLYING_CREATURE_FILTER
            = new FilterCreaturePermanent("creature with flying");
    private static final FilterCreaturePermanent CREATURE_FILTER
            = new FilterCreaturePermanent("creatures");

    static {
        COUNTER_FILTER.add(TokenPredicate.FALSE);
        COUNTER_FILTER.add(Predicates.not(new AbilityPredicate(FlyingAbility.class)));
        FLYING_CREATURE_FILTER.add(new AbilityPredicate(FlyingAbility.class));
    }

    public TheWindyCityPlane() {
        this.setPlaneType(Planes.PLANE_THE_WINDY_CITY);

        // When you planeswalk here and at the beginning of your upkeep, put a flying counter on
        // target nontoken creature you control without flying.
        Ability ability = new PlaneswalkToSourceTriggeredAbility(
                new AddCountersTargetEffect(CounterType.FLYING.createInstance())
        );
        ability.addTarget(new TargetControlledPermanent(COUNTER_FILTER));
        this.getAbilities().add(ability);
        ability = new BeginningOfUpkeepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU,
                new AddCountersTargetEffect(CounterType.FLYING.createInstance()), false
        );
        ability.addTarget(new TargetControlledPermanent(COUNTER_FILTER));
        this.getAbilities().add(ability);

        // Whenever chaos ensues, exile target creature with flying. For as long as it remains
        // exiled, its owner may cast it by paying {2} rather than paying its mana cost.
        ability = new ChaosEnsuesTriggeredAbility(new TheWindyCityExileEffect(), false);
        ability.addTarget(new TargetPermanent(FLYING_CREATURE_FILTER));
        this.getAbilities().add(ability);

        // When you planeswalk away from The Windy City, remove all flying counters from all creatures.
        this.getAbilities().add(new PlaneswalkAwayFromSourceTriggeredAbility(
                new RemoveAllCountersAllEffect(CounterType.FLYING, CREATURE_FILTER)
        ));
    }

    private TheWindyCityPlane(final TheWindyCityPlane plane) {
        super(plane);
    }

    @Override
    public TheWindyCityPlane copy() {
        return new TheWindyCityPlane(this);
    }
}

class TheWindyCityExileEffect extends OneShotEffect {

    TheWindyCityExileEffect() {
        super(Outcome.Benefit);
        staticText = "exile target creature with flying. For as long as it remains exiled, its owner "
                + "may cast it by paying {2} rather than paying its mana cost";
    }

    private TheWindyCityExileEffect(final TheWindyCityExileEffect effect) {
        super(effect);
    }

    @Override
    public TheWindyCityExileEffect copy() {
        return new TheWindyCityExileEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent == null || !permanent.moveToExile(null, "The Windy City", source, game)) {
            return false;
        }
        Card card = game.getCard(permanent.getId());
        if (card != null) {
            game.addEffect(new TheWindyCityCastEffect(card, game), source);
        }
        return true;
    }
}

class TheWindyCityCastEffect extends AsThoughEffectImpl {

    TheWindyCityCastEffect(Card card, Game game) {
        super(AsThoughEffectType.CAST_FROM_NOT_OWN_HAND_ZONE, Duration.Custom, Outcome.Benefit);
        setTargetPointer(new FixedTarget(card, game));
    }

    private TheWindyCityCastEffect(final TheWindyCityCastEffect effect) {
        super(effect);
    }

    @Override
    public TheWindyCityCastEffect copy() {
        return new TheWindyCityCastEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return true;
    }

    @Override
    public boolean applies(UUID objectId, Ability source, UUID affectedControllerId, Game game) {
        Card card = game.getCard(getTargetPointer().getFirst(game, source));
        if (card == null) {
            discard();
            return false;
        }
        if (!card.getId().equals(objectId) || !card.isOwnedBy(affectedControllerId)) {
            return false;
        }
        Player player = game.getPlayer(affectedControllerId);
        if (player == null) {
            return false;
        }
        Costs<Cost> additionalCosts = new CostsImpl<>();
        additionalCosts.addAll(card.getSpellAbility().getCosts());
        player.setCastSourceIdWithAlternateMana(
                card.getId(), new ManaCostsImpl<>("{2}"), additionalCosts
        );
        return true;
    }
}
