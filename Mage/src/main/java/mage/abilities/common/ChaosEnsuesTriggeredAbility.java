package mage.abilities.common;

import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.Effect;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;

/** The inherent chaos ability of a plane (rule 311.7). */
public class ChaosEnsuesTriggeredAbility extends TriggeredAbilityImpl {

    public ChaosEnsuesTriggeredAbility(Effect effect, boolean optional) {
        super(Zone.COMMAND, effect, optional);
        setTriggerPhrase("Whenever chaos ensues, ");
    }

    protected ChaosEnsuesTriggeredAbility(final ChaosEnsuesTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.CHAOS_ENSUES
                && (event.getTargetId() == null || event.getTargetId().equals(getSourceId()));
    }

    @Override
    public ChaosEnsuesTriggeredAbility copy() {
        return new ChaosEnsuesTriggeredAbility(this);
    }
}
