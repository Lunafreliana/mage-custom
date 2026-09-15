package mage.game.command.phenomena;

import mage.abilities.common.EncounterPhenomenonTriggeredAbility;
import mage.abilities.effects.common.ReverseTurnOrderEffect;
import mage.constants.Phenomena;
import mage.game.command.Phenomenon;

/**
 * @author The XMage Developers
 */
public final class TimeDistortionPhenomenon extends Phenomenon {

    public TimeDistortionPhenomenon() {
        super(Phenomena.TIME_DISTORTION.getFullName());

        // When you encounter Time Distortion, reverse the game's turn order.
        this.getAbilities().add(new EncounterPhenomenonTriggeredAbility(new ReverseTurnOrderEffect()));
    }

    private TimeDistortionPhenomenon(final TimeDistortionPhenomenon phenomenon) {
        super(phenomenon);
    }

    @Override
    public TimeDistortionPhenomenon copy() {
        return new TimeDistortionPhenomenon(this);
    }
}
