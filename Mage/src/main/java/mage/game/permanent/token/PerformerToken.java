package mage.game.permanent.token;

import mage.MageInt;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.HasteAbility;
import mage.constants.CardType;
import mage.constants.SubType;

/**
 * @author The XMage Developers
 */
public final class PerformerToken extends TokenImpl {

    public PerformerToken() {
        super("Performer Token", "1/1 red Performer creature token with flying and haste");
        cardType.add(CardType.CREATURE);
        subtype.add(SubType.PERFORMER);
        color.setRed(true);
        power = new MageInt(1);
        toughness = new MageInt(1);
        addAbility(FlyingAbility.getInstance());
        addAbility(HasteAbility.getInstance());
    }

    private PerformerToken(final PerformerToken token) {
        super(token);
    }

    @Override
    public PerformerToken copy() {
        return new PerformerToken(this);
    }
}
