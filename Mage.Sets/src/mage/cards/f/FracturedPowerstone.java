package mage.cards.f;

import java.util.UUID;
import mage.abilities.Ability;
import mage.abilities.common.ActivateAsSorceryActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.RollPlanarDieEffect;
import mage.abilities.mana.ColorlessManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;

public final class FracturedPowerstone extends CardImpl {

    public FracturedPowerstone(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{2}");

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T}: Roll the planar die. Activate only as a sorcery.
        Ability ability = new ActivateAsSorceryActivatedAbility(
                new RollPlanarDieEffect(null, null), new TapSourceCost()
        );
        this.addAbility(ability);
    }

    private FracturedPowerstone(final FracturedPowerstone card) {
        super(card);
    }

    @Override
    public FracturedPowerstone copy() {
        return new FracturedPowerstone(this);
    }
}
