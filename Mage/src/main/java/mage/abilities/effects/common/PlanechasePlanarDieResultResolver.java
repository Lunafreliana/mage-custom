package mage.abilities.effects.common;

import mage.abilities.Ability;
import mage.abilities.common.PlaneswalkingTriggeredAbility;
import mage.constants.PlanarDieRollResult;
import mage.game.Game;
import mage.game.events.GameEvent;

import java.util.UUID;

/** Resolves the Planechase meaning of an already-rolled planar die result. */
public final class PlanechasePlanarDieResultResolver {

    private PlanechasePlanarDieResultResolver() {
    }

    public static boolean resolve(PlanarDieRollResult result, UUID rollerId, Ability source, Game game) {
        switch (result) {
            case BLANK_ROLL:
                return true;
            case CHAOS_ROLL:
                return new ChaosEnsuesEffect().apply(game, source);
            case PLANAR_ROLL:
                PlaneswalkingTriggeredAbility ability = new PlaneswalkingTriggeredAbility();
                ability.setControllerId(rollerId);
                GameEvent event = new GameEvent(GameEvent.EventType.ROLL_DIE,
                        rollerId, source, rollerId);
                game.addTriggeredAbility(ability, event);
                return true;
            default:
                throw new IllegalArgumentException("Unknown planar die result: " + result);
        }
    }
}
