package mage.game.command.planes;

import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.dynamicvalue.common.StaticValue;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.DrawCardTargetEffect;
import mage.abilities.effects.common.continuous.MaximumHandSizeControllerEffect;
import mage.abilities.effects.common.continuous.MaximumHandSizeControllerEffect.HandSizeModification;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.events.DamagedPlayerEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.target.targetpointer.FixedTarget;

/**
 * @author VibecodingQueens
 */
public class UndercityReachesPlane extends Plane {

    private static final FilterCard filter = new FilterCard("creature spells");

    static {
        filter.add(CardType.CREATURE.getPredicate());
    }

    public UndercityReachesPlane() {
        this.setPlaneType(Planes.PLANE_UNDERCITY_REACHES);

        // Whenever a creature deals combat damage to a player, its controller may draw a card.
        Ability ability = new UndercityReachesTriggeredAbility();

        this.getAbilities().add(ability);

        // Whenever chaos ensues, reveal the top three cards of your library. Put all creature cards revealed this way into your hand and the rest into your graveyard.
        Effect chaosEffect = new MaximumHandSizeControllerEffect(Integer.MAX_VALUE, Duration.EndOfGame, HandSizeModification.SET);

        ChaosEnsuesTriggeredAbility chaosAbility = new ChaosEnsuesTriggeredAbility(chaosEffect, false);
        this.getAbilities().add(chaosAbility);
    }

    private UndercityReachesPlane(final UndercityReachesPlane plane) {
        super(plane);
    }

    @Override
    public UndercityReachesPlane copy() {
        return new UndercityReachesPlane(this);
    }
}

class UndercityReachesTriggeredAbility extends TriggeredAbilityImpl {

    public UndercityReachesTriggeredAbility() {
        super(Zone.COMMAND, null, false); // effect must be optional
    }

    protected UndercityReachesTriggeredAbility(final UndercityReachesTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public UndercityReachesTriggeredAbility copy() {
        return new UndercityReachesTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DAMAGED_PLAYER;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!game.getState().hasFaceUpPlane(Planes.PLANE_UNDERCITY_REACHES)) {
            return false;
        }

        if (((DamagedPlayerEvent) event).isCombatDamage()) {
            Permanent creature = game.getPermanent(event.getSourceId());
            if (creature != null) {
                Effect effect = new DrawCardTargetEffect(StaticValue.get(1), true, false);
                effect.setTargetPointer(new FixedTarget(creature.getControllerId()));
                effect.apply(game, this);
                return true;
            }
        }
        return false;
    }

    @Override
    public String getRule() {
        return "Whenever a creature deals combat damage to a player, its controller may a draw a card";
    }
}
