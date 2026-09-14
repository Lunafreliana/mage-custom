package mage.abilities.effects.common;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BecomesCybermanEffect;
import mage.constants.Outcome;
import mage.game.Game;
import mage.game.permanent.Permanent;

/**
 * Turns a targeted creature face down with the copiable characteristics of a
 * 2/2 Cyberman artifact creature.
 */
public class TurnFaceDownCybermanTargetEffect extends OneShotEffect {

    public TurnFaceDownCybermanTargetEffect() {
        super(Outcome.Detriment);
        this.staticText = "turn target creature face down. It becomes a 2/2 Cyberman artifact creature";
    }

    private TurnFaceDownCybermanTargetEffect(final TurnFaceDownCybermanTargetEffect effect) {
        super(effect);
    }

    @Override
    public TurnFaceDownCybermanTargetEffect copy() {
        return new TurnFaceDownCybermanTargetEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent == null || permanent.isTransformable()) {
            return false;
        }
        game.addEffect(new BecomesCybermanEffect(new MageObjectReference(permanent, game)), source);
        return true;
    }
}
