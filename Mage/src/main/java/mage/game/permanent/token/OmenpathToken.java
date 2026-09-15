package mage.game.permanent.token;

import mage.abilities.common.ActivateAsSorceryActivatedAbility;
import mage.abilities.costs.common.SacrificeSourceCost;
import mage.abilities.effects.common.PlaneswalkEffect;
import mage.constants.CardType;

/** The Omenpath token created by Omenpath Instability. */
public final class OmenpathToken extends TokenImpl {

    public OmenpathToken() {
        super("Omenpath Token", "Omenpath enchantment token with \"Sacrifice this enchantment: "
                + "Planeswalk. Activate only as a sorcery.\"");
        cardType.add(CardType.ENCHANTMENT);

        SacrificeSourceCost cost = new SacrificeSourceCost();
        cost.setText("Sacrifice this enchantment");
        this.addAbility(new ActivateAsSorceryActivatedAbility(new PlaneswalkEffect(false), cost));
    }

    private OmenpathToken(final OmenpathToken token) {
        super(token);
    }

    @Override
    public OmenpathToken copy() {
        return new OmenpathToken(this);
    }
}
