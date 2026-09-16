package mage.abilities.effects.common.continuous;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.cards.repository.TokenInfo;
import mage.cards.repository.TokenRepository;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.EmptyToken;
import mage.game.permanent.token.Token;
import mage.util.CardUtil;

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

        // The base face-down effect supplies the shared card-back image. Replace
        // only this Cyberman's image metadata with its dedicated reminder art.
        TokenInfo reminder = TokenRepository.instance.findPreferredTokenInfoForXmage(
                TokenRepository.XMAGE_IMAGE_NAME_FACE_DOWN_CYBERMAN, permanent.getId());
        if (reminder != null) {
            Token image = new EmptyToken();
            image.setExpansionSetCode(reminder.getSetCode());
            image.setCardNumber("0");
            image.setImageFileName(reminder.getName());
            image.setImageNumber(reminder.getImageNumber());
            image.setUsesVariousArt(false);
            CardUtil.copySetAndCardNumber(permanent, image);
        }
        return true;
    }
}
