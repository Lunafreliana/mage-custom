package mage.abilities.common;

import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.Effect;
import mage.constants.RollDieType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.DieRolledEvent;
import mage.game.events.GameEvent;

/** Triggers when the planar controller rolls the planar die. */
public final class PlanarDieRolledTriggeredAbility extends TriggeredAbilityImpl {

    public PlanarDieRolledTriggeredAbility(Effect effect) {
        super(Zone.COMMAND, effect);
        setTriggerPhrase("Whenever you roll the planar die, ");
    }

    private PlanarDieRolledTriggeredAbility(final PlanarDieRolledTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DIE_ROLLED;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return event instanceof DieRolledEvent
                && ((DieRolledEvent) event).getRollDieType() == RollDieType.PLANAR
                && isControlledBy(event.getTargetId());
    }

    @Override
    public PlanarDieRolledTriggeredAbility copy() {
        return new PlanarDieRolledTriggeredAbility(this);
    }
}
