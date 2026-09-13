package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.LeavesBattlefieldAllTriggeredAbility;
import mage.abilities.effects.common.DrawCardTargetEffect;
import mage.abilities.effects.common.ReturnToHandTargetEffect;
import mage.constants.Planes;
import mage.constants.SetTargetPointer;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.command.Plane;
import mage.target.common.TargetControlledCreaturePermanent;

/**
 * @author Susucr
 */
public final class TakenumaPlane extends Plane {

    public TakenumaPlane() {
        this.setPlaneType(Planes.PLANE_TAKENUMA);

        // Whenever a creature leaves the battlefield, its controller draws a card.
        this.getAbilities().add(new LeavesBattlefieldAllTriggeredAbility(
                Zone.COMMAND,
                new DrawCardTargetEffect(1).setText("its controller draws a card"),
                StaticFilters.FILTER_PERMANENT_CREATURE,
                false,
                SetTargetPointer.PLAYER
        ));

        // Whenever chaos ensues, return target creature you control to its owner's hand.
        Ability ability = new ChaosEnsuesTriggeredAbility(new ReturnToHandTargetEffect(), false);
        ability.addTarget(new TargetControlledCreaturePermanent());
        this.getAbilities().add(ability);
    }

    private TakenumaPlane(final TakenumaPlane plane) {
        super(plane);
    }

    @Override
    public TakenumaPlane copy() {
        return new TakenumaPlane(this);
    }
}
