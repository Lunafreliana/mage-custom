package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.OneOrMoreCombatDamagePlayerTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.continuous.AddCardSubTypeTargetEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.SetTargetPointer;
import mage.constants.SubType;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.filter.FilterCard;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.mageobject.AbilityPredicate;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.PerformerToken;
import mage.players.Player;
import mage.target.TargetCard;
import mage.target.common.TargetCreaturePermanent;

import java.util.Objects;

/**
 * @author The XMage Developers
 */
public final class CircusOfTheSunPlane extends Plane {

    private static final FilterCreaturePermanent FLYING_CREATURE_FILTER
            = new FilterCreaturePermanent("creatures you control with flying");

    static {
        FLYING_CREATURE_FILTER.add(new AbilityPredicate(FlyingAbility.class));
    }

    public CircusOfTheSunPlane() {
        this.setPlaneType(Planes.PLANE_CIRCUS_OF_THE_SUN);

        // Whenever you planeswalk here or at the beginning of your upkeep, create two 1/1 red
        // Performer creature tokens with flying and haste.
        CreateTokenEffect createPerformers = new CreateTokenEffect(new PerformerToken(), 2);
        this.getAbilities().add(new PlaneswalkToSourceTriggeredAbility(createPerformers));
        this.getAbilities().add(new BeginningOfUpkeepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU, createPerformers.copy(), false
        ));

        // Whenever one or more creatures you control with flying deal combat damage to a player,
        // you may return it to your hand. If you do, draw cards equal to its power.
        this.getAbilities().add(new OneOrMoreCombatDamagePlayerTriggeredAbility(
                Zone.COMMAND, new CircusOfTheSunReturnEffect(), FLYING_CREATURE_FILTER,
                SetTargetPointer.PERMANENT, true
        ));

        // Whenever chaos ensues, put a flying counter on up to one target creature. It becomes a
        // Performer in addition to its other types.
        Ability ability = new ChaosEnsuesTriggeredAbility(
                new AddCountersTargetEffect(CounterType.FLYING.createInstance()), false
        );
        ability.addEffect(new AddCardSubTypeTargetEffect(SubType.PERFORMER, Duration.WhileOnBattlefield));
        ability.addTarget(new TargetCreaturePermanent(0, 1));
        this.getAbilities().add(ability);
    }

    private CircusOfTheSunPlane(final CircusOfTheSunPlane plane) {
        super(plane);
    }

    @Override
    public CircusOfTheSunPlane copy() {
        return new CircusOfTheSunPlane(this);
    }
}

class CircusOfTheSunReturnEffect extends OneShotEffect {

    CircusOfTheSunReturnEffect() {
        super(Outcome.DrawCard);
        staticText = "return it to your hand. If you do, draw cards equal to its power";
    }

    private CircusOfTheSunReturnEffect(final CircusOfTheSunReturnEffect effect) {
        super(effect);
    }

    @Override
    public CircusOfTheSunReturnEffect copy() {
        return new CircusOfTheSunReturnEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Cards candidates = new CardsImpl();
        getTargetPointer().getTargets(game, source).stream()
                .map(game::getPermanent)
                .filter(Objects::nonNull)
                .forEach(candidates::add);
        if (candidates.isEmpty()) {
            return false;
        }
        TargetCard choice = new TargetCard(Zone.BATTLEFIELD, new FilterCard("creature to return"));
        choice.withNotTarget(true);
        if (!controller.choose(Outcome.ReturnToHand, candidates, choice, source, game)) {
            return false;
        }
        Permanent permanent = game.getPermanent(choice.getFirstTarget());
        if (permanent == null) {
            return false;
        }
        int power = permanent.getPower().getValue();
        if (!controller.moveCards(permanent, Zone.HAND, source, game)) {
            return false;
        }
        controller.drawCards(power, source, game);
        return true;
    }
}
