package mage.game.permanent.token;

import mage.abilities.LoyaltyAbility;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.keyword.SurveilEffect;
import mage.constants.CardType;
import mage.constants.SubType;

/**
 * @author muz
 */
public final class JacePlaneswalkerToken extends TokenImpl {

    public JacePlaneswalkerToken() {
        super("Jace Token", "blue Jace planeswalker token");
        cardType.add(CardType.PLANESWALKER);
        color.setBlue(true);
        subtype.add(SubType.JACE);

        this.addAbility(new LoyaltyAbility(new SurveilEffect(1), -1));
        this.addAbility(new LoyaltyAbility(new DrawCardSourceControllerEffect(1), -3));
    }

    private JacePlaneswalkerToken(final JacePlaneswalkerToken token) {
        super(token);
    }

    @Override
    public JacePlaneswalkerToken copy() {
        return new JacePlaneswalkerToken(this);
    }
}
