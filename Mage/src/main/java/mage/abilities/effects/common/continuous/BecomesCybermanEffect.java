package mage.abilities.effects.common.continuous;

import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.constants.SubType;
import mage.game.Game;
import mage.game.permanent.Permanent;

/**
 * Applies the additional copiable characteristics used by face-down Cybermen.
 * The canonical face-down characteristics must be applied separately with
 * {@link BecomesFaceDownCreatureEffect}.
 */
public class BecomesCybermanEffect extends ContinuousEffectImpl {

    public BecomesCybermanEffect() {
        super(Duration.Custom, Layer.CopyEffects_1, SubLayer.FaceDownEffects_1b, Outcome.Neutral);
    }

    private BecomesCybermanEffect(final BecomesCybermanEffect effect) {
        super(effect);
    }

    @Override
    public BecomesCybermanEffect copy() {
        return new BecomesCybermanEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent == null || !permanent.isFaceDown(game)) {
            discard();
            return false;
        }
        permanent.removeAllSuperTypes(game);
        permanent.removeAllCardTypes(game);
        permanent.removeAllSubTypes(game);
        permanent.addCardType(game, CardType.ARTIFACT, CardType.CREATURE);
        permanent.addSubType(game, SubType.CYBERMAN);
        return true;
    }
}
