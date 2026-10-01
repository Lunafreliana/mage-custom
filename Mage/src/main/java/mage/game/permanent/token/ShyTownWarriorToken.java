package mage.game.permanent.token;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.combat.CowardsCantBlockWarriorsEffect;
import mage.abilities.keyword.HasteAbility;
import mage.constants.CardType;
import mage.constants.SubType;

/**
 * @author The XMage Developers
 */
public final class ShyTownWarriorToken extends TokenImpl {

    public ShyTownWarriorToken() {
        super("Warrior Token", "2/2 red Warrior creature token with haste and \"Cowards can't block Warriors.\"");
        cardType.add(CardType.CREATURE);
        color.setRed(true);
        subtype.add(SubType.WARRIOR);
        power = new MageInt(2);
        toughness = new MageInt(2);

        addAbility(HasteAbility.getInstance());
        addAbility(new SimpleStaticAbility(new CowardsCantBlockWarriorsEffect()));
    }

    private ShyTownWarriorToken(final ShyTownWarriorToken token) {
        super(token);
    }

    @Override
    public ShyTownWarriorToken copy() {
        return new ShyTownWarriorToken(this);
    }
}
