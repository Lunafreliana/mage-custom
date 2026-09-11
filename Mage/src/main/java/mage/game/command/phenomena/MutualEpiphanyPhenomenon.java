package mage.game.command.phenomena;

import mage.abilities.common.EncounterPhenomenonTriggeredAbility;
import mage.abilities.effects.common.DrawCardAllEffect;
import mage.constants.Phenomena;
import mage.game.command.Phenomenon;

/**
 * @author The XMage Developers
 */
public final class MutualEpiphanyPhenomenon extends Phenomenon {

    public MutualEpiphanyPhenomenon() {
        super(Phenomena.MUTUAL_EPIPHANY.getFullName());

        // When you encounter Mutual Epiphany, each player draws four cards.
        this.getAbilities().add(new EncounterPhenomenonTriggeredAbility(new DrawCardAllEffect(4)));
    }

    private MutualEpiphanyPhenomenon(final MutualEpiphanyPhenomenon phenomenon) {
        super(phenomenon);
    }

    @Override
    public MutualEpiphanyPhenomenon copy() {
        return new MutualEpiphanyPhenomenon(this);
    }
}
