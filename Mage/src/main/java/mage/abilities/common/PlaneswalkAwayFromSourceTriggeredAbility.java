package mage.abilities.common;

import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.Effect;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;

/** Triggers when a planeswalk moves away from this face-up planar object. */
public class PlaneswalkAwayFromSourceTriggeredAbility extends TriggeredAbilityImpl {

    public PlaneswalkAwayFromSourceTriggeredAbility(Effect effect) {
        super(Zone.COMMAND, effect);
        setTriggerPhrase("When you planeswalk away from {this}, ");
    }

    protected PlaneswalkAwayFromSourceTriggeredAbility(
            final PlaneswalkAwayFromSourceTriggeredAbility ability
    ) {
        super(ability);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.PLANESWALKED_AWAY;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return getSourceId().equals(event.getTargetId());
    }

    @Override
    public PlaneswalkAwayFromSourceTriggeredAbility copy() {
        return new PlaneswalkAwayFromSourceTriggeredAbility(this);
    }
}
