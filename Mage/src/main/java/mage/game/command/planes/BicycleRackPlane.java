package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.continuous.BoostSourceEffect;
import mage.abilities.effects.common.continuous.GainAbilityControlledEffect;
import mage.abilities.keyword.CyclingAbility;
import mage.cards.Card;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.SubLayer;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.command.Plane;
import mage.players.Player;
import mage.watchers.common.DiscardedCardWatcher;

/**
 * @author The XMage Developers
 */
public final class BicycleRackPlane extends Plane {

    public BicycleRackPlane() {
        setPlaneType(Planes.PLANE_BICYCLE_RACK);

        // Cards in each player's hand have cycling {2}.
        getAbilities().add(new SimpleStaticAbility(Zone.COMMAND, new BicycleRackCyclingEffect()));

        // Whenever chaos ensues, until end of turn, creatures you control have
        // "This creature gets +1/+1 for each card you've discarded this turn."
        Ability grantedAbility = new SimpleStaticAbility(new BoostSourceEffect(
                BicycleRackDiscardedValue.instance,
                BicycleRackDiscardedValue.instance,
                Duration.WhileOnBattlefield
        ));
        Ability ability = new ChaosEnsuesTriggeredAbility(new GainAbilityControlledEffect(
                grantedAbility, Duration.EndOfTurn, StaticFilters.FILTER_PERMANENT_CREATURES
        ).setText("until end of turn, creatures you control have \"This creature gets +1/+1 "
                + "for each card you've discarded this turn.\""), false);
        ability.addWatcher(new DiscardedCardWatcher());
        getAbilities().add(ability);
    }

    private BicycleRackPlane(final BicycleRackPlane plane) {
        super(plane);
    }

    @Override
    public BicycleRackPlane copy() {
        return new BicycleRackPlane(this);
    }
}

class BicycleRackCyclingEffect extends ContinuousEffectImpl {

    private final Ability cyclingAbility;

    BicycleRackCyclingEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6,
                SubLayer.NA, Outcome.AddAbility);
        cyclingAbility = new CyclingAbility(new GenericManaCost(2));
        staticText = "Cards in each player's hand have cycling {2}";
    }

    private BicycleRackCyclingEffect(final BicycleRackCyclingEffect effect) {
        super(effect);
        cyclingAbility = effect.cyclingAbility.copy();
    }

    @Override
    public BicycleRackCyclingEffect copy() {
        return new BicycleRackCyclingEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        for (Player player : game.getPlayers().values()) {
            for (Card card : player.getHand().getCards(game)) {
                game.getState().addOtherAbility(card, cyclingAbility);
            }
        }
        return true;
    }
}

enum BicycleRackDiscardedValue implements DynamicValue {
    instance;

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect) {
        return DiscardedCardWatcher.getDiscarded(sourceAbility.getControllerId(), game);
    }

    @Override
    public BicycleRackDiscardedValue copy() {
        return instance;
    }

    @Override
    public String getMessage() {
        return "cards you've discarded this turn";
    }

    @Override
    public String toString() {
        return "1";
    }
}
