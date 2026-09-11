package mage.abilities.common;

import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.Effect;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;

/** A phenomenon's inherent encounter trigger (rule 312.5). */
public class EncounterPhenomenonTriggeredAbility extends TriggeredAbilityImpl {

    public EncounterPhenomenonTriggeredAbility(Effect effect) {
        super(Zone.COMMAND, effect, false);
        setTriggerPhrase("When you encounter {this}, ");
    }

    protected EncounterPhenomenonTriggeredAbility(final EncounterPhenomenonTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ENCOUNTERED_PHENOMENON;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return getSourceId().equals(event.getTargetId());
    }

    @Override
    public EncounterPhenomenonTriggeredAbility copy() {
        return new EncounterPhenomenonTriggeredAbility(this);
    }
}
