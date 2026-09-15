package mage.game.permanent.token;

import mage.abilities.common.ActivateAsSorceryActivatedAbility;
import mage.abilities.costs.common.SacrificeSourceCost;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.constants.CardType;

/** The Entropy token created by Omenpath Instability. */
public final class EntropyToken extends TokenImpl {

    public EntropyToken() {
        super("Entropy Token", "Entropy enchantment token with \"Sacrifice this enchantment: "
                + "Chaos ensues. Activate only as a sorcery\"");
        cardType.add(CardType.ENCHANTMENT);

        SacrificeSourceCost cost = new SacrificeSourceCost();
        cost.setText("Sacrifice this enchantment");
        this.addAbility(new ActivateAsSorceryActivatedAbility(new ChaosEnsuesEffect(), cost));
    }

    private EntropyToken(final EntropyToken token) {
        super(token);
    }

    @Override
    public EntropyToken copy() {
        return new EntropyToken(this);
    }
}
