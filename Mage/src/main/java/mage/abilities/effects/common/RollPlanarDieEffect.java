package mage.abilities.effects.common;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.constants.Outcome;
import mage.constants.PlanarDieRollResult;
import mage.game.Game;
import mage.players.Player;

/**
 * @author spjspj
 */
public class RollPlanarDieEffect extends OneShotEffect {

    public RollPlanarDieEffect() {
        super(Outcome.Neutral);
    }

    protected RollPlanarDieEffect(final RollPlanarDieEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        PlanarDieRollResult planarRoll = controller.rollPlanarDie(outcome, source, game);
        return PlanechasePlanarDieResultResolver.resolve(planarRoll, controller.getId(), source, game);
    }

    @Override
    public RollPlanarDieEffect copy() {
        return new RollPlanarDieEffect(this);
    }
}
