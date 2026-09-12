package mage.abilities.effects.common;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.constants.Outcome;
import mage.game.Game;
import mage.game.command.PlaneswalkContext;
import mage.players.Player;

/**
 * @author VibecodingQueens
 */
public class PlaneswalkEffect extends OneShotEffect {

    private final boolean optional;
    private final PlaneswalkContext.Cause cause;

    public PlaneswalkEffect(boolean optional) {
        this(optional, PlaneswalkContext.Cause.SPELL_OR_ABILITY);
    }

    public PlaneswalkEffect(boolean optional, PlaneswalkContext.Cause cause) {
        super(Outcome.Neutral);
        this.optional = optional;
        this.cause = cause;
        staticText = optional ? "you may planeswalk" : "you planeswalk";
    }

    protected PlaneswalkEffect(final PlaneswalkEffect effect) {
        super(effect);
        this.optional = effect.optional;
        this.cause = effect.cause;
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }

        if (!game.getState().isPlaneChase() || game.getState().getFaceUpPlanarCards().isEmpty()) {
            return true; // Not playing with planeswalk enabled.
        }

        if (optional && !controller.chooseUse(outcome, "Planeswalk?", source, game)) {
            return true;
        }

        return game.planeswalk(new PlaneswalkContext(controller.getId(),
                cause, source.getSourceId()));
    }

    @Override
    public PlaneswalkEffect copy() {
        return new PlaneswalkEffect(this);
    }
}
