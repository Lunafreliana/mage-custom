package mage.game.command.planes;

import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.abilities.effects.keyword.DiscoverEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.game.command.Plane;

/**
 * @author The XMage Developers
 */
public final class OteclanPlane extends Plane {

    public OteclanPlane() {
        this.setPlaneType(Planes.PLANE_OTECLAN);

        // When you planeswalk to Oteclán and at the beginning of your upkeep, chaos ensues.
        this.getAbilities().add(new PlaneswalkToSourceTriggeredAbility(new ChaosEnsuesEffect()));
        this.getAbilities().add(new BeginningOfUpkeepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU, new ChaosEnsuesEffect(), false
        ));

        // Whenever chaos ensues, discover 3.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(new DiscoverEffect(3), false));
    }

    private OteclanPlane(final OteclanPlane plane) {
        super(plane);
    }

    @Override
    public OteclanPlane copy() {
        return new OteclanPlane(this);
    }
}
