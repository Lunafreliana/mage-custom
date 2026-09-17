package mage.game.permanent.token;

import mage.MageInt;
import mage.abilities.keyword.FlyingAbility;
import mage.constants.CardType;
import mage.constants.SubType;

/**
 * @author muz
 */
public final class LyraTolarianArchangelToken extends TokenImpl {

    public LyraTolarianArchangelToken() {
        super("Angel Token", "3/3 blue Angel creature token with flying");
        cardType.add(CardType.CREATURE);
        color.setBlue(true);
        subtype.add(SubType.ANGEL);
        power = new MageInt(3);
        toughness = new MageInt(3);

        addAbility(FlyingAbility.getInstance());
    }

    private LyraTolarianArchangelToken(final LyraTolarianArchangelToken token) {
        super(token);
    }

    @Override
    public LyraTolarianArchangelToken copy() {
        return new LyraTolarianArchangelToken(this);
    }
}
