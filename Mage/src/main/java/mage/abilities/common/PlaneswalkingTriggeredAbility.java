package mage.abilities.common;

import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.common.PlaneswalkEffect;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;

/** Source-less inherent Planechase ability created by a planeswalker result. */
public class PlaneswalkingTriggeredAbility extends TriggeredAbilityImpl {

    public PlaneswalkingTriggeredAbility() {
        super(Zone.ALL, new PlaneswalkEffect(false));
        setTriggerPhrase("Whenever you roll the planeswalker symbol on the planar die, ");
    }

    protected PlaneswalkingTriggeredAbility(final PlaneswalkingTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        // This source-less ability is created after a planar result rather than
        // registered as an event listener.
        return false;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return false;
    }

    @Override
    public PlaneswalkingTriggeredAbility copy() {
        return new PlaneswalkingTriggeredAbility(this);
    }
}
