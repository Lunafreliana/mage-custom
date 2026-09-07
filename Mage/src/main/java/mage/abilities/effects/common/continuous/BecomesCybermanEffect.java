package mage.abilities.effects.common.continuous;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.game.Game;
import mage.game.permanent.Permanent;

/**
 * Applies both the canonical face-down characteristics and the additional
 * copiable characteristics used by face-down Cybermen.
 */
public class BecomesCybermanEffect extends BecomesFaceDownCreatureEffect {

    public BecomesCybermanEffect(MageObjectReference objectReference) {
        super(null, objectReference, Duration.Custom, FaceDownType.MANUAL);
        this.outcome = Outcome.Neutral;
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
        if (!super.apply(game, source)) {
            return false;
        }
        Permanent permanent = objectReference.getPermanent(game);
        if (permanent == null || !permanent.isFaceDown(game)) {
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
