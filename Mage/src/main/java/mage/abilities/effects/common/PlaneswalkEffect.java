package mage.abilities.effects.common;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.constants.Outcome;
import mage.game.Game;
import mage.game.command.Plane;
import mage.players.Player;

/**
 * @author Susucr
 */
public class PlaneswalkEffect extends OneShotEffect {

    private final boolean optional;

    public PlaneswalkEffect(boolean optional) {
        super(Outcome.Neutral);
        this.optional = optional;
        staticText = optional ? "you may planeswalk" : "you planeswalk";
    }

    protected PlaneswalkEffect(final PlaneswalkEffect effect) {
        super(effect);
        this.optional = effect.optional;
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }

        // As of now, a player may planeswalk iff there are planes in the command zone.
        boolean canPlaneswalk = game.getState().getCommand().stream().anyMatch(obj -> obj instanceof Plane);
        if (!canPlaneswalk) {
            return true; // Not playing with planeswalk enabled.
        }

        if (optional && !controller.chooseUse(outcome, "Planeswalk?", source, game)) {
            return true;
        }

        return game.planeswalk(controller.getId());
    }

    @Override
    public PlaneswalkEffect copy() {
        return new PlaneswalkEffect(this);
    }
}
