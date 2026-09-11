package mage.abilities.common;

import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.Effect;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;

/** Triggers when the completed planeswalk event names this planar object. */
public class PlaneswalkToSourceTriggeredAbility extends TriggeredAbilityImpl {

    public PlaneswalkToSourceTriggeredAbility(Effect effect) {
        super(Zone.COMMAND, effect);
        setTriggerPhrase("When you planeswalk to {this}, ");
    }

    protected PlaneswalkToSourceTriggeredAbility(final PlaneswalkToSourceTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.PLANESWALKED;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return getSourceId().equals(event.getTargetId());
    }

    @Override
    public PlaneswalkToSourceTriggeredAbility copy() {
        return new PlaneswalkToSourceTriggeredAbility(this);
    }
}
