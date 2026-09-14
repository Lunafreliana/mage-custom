package mage.game.permanent.token;

import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.SacrificeSourceCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.constants.CardType;

/**
 * @author The XMage Developers
 */
public final class HatToken extends TokenImpl {

    public HatToken() {
        super("Hat Token", "Hat token");
        cardType.add(CardType.ARTIFACT);

        SimpleActivatedAbility ability = new SimpleActivatedAbility(
                new CreateTokenEffect(new RabbitToken()), new GenericManaCost(1)
        );
        ability.addCost(new TapSourceCost());
        ability.addCost(new SacrificeSourceCost());
        this.addAbility(ability);
    }

    private HatToken(final HatToken token) {
        super(token);
    }

    @Override
    public HatToken copy() {
        return new HatToken(this);
    }
}
