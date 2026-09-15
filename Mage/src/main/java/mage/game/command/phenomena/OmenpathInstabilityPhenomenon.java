package mage.game.command.phenomena;

import mage.abilities.common.EncounterPhenomenonTriggeredAbility;
import mage.abilities.effects.common.CreateTokenAllEffect;
import mage.constants.Phenomena;
import mage.constants.TargetController;
import mage.game.command.Phenomenon;
import mage.game.permanent.token.EntropyToken;
import mage.game.permanent.token.OmenpathToken;

/**
 * Black Lotus Unknown Planechase playtest card.
 */
public final class OmenpathInstabilityPhenomenon extends Phenomenon {

    public OmenpathInstabilityPhenomenon() {
        super(Phenomena.OMENPATH_INSTABILITY.getFullName());

        // When you encounter this phenomenon, each player creates an Entropy
        // enchantment token and an Omenpath enchantment token.
        EncounterPhenomenonTriggeredAbility ability = new EncounterPhenomenonTriggeredAbility(
                new CreateTokenAllEffect(new EntropyToken(), TargetController.EACH_PLAYER));
        ability.addEffect(new CreateTokenAllEffect(
                new OmenpathToken(), TargetController.EACH_PLAYER).concatBy("and"));
        this.getAbilities().add(ability);
    }

    private OmenpathInstabilityPhenomenon(final OmenpathInstabilityPhenomenon phenomenon) {
        super(phenomenon);
    }

    @Override
    public OmenpathInstabilityPhenomenon copy() {
        return new OmenpathInstabilityPhenomenon(this);
    }
}
