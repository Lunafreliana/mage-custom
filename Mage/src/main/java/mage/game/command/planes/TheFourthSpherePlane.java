package mage.game.command.planes;

import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.SacrificeEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.command.Plane;
import mage.game.permanent.token.ZombieToken;

/**
 * @author The XMage Developers
 */
public final class TheFourthSpherePlane extends Plane {

    public TheFourthSpherePlane() {
        this.setPlaneType(Planes.PLANE_THE_FOURTH_SPHERE);

        // At the beginning of your upkeep, sacrifice a nonblack creature.
        this.getAbilities().add(new BeginningOfUpkeepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU,
                new SacrificeEffect(StaticFilters.FILTER_PERMANENT_CREATURE_NON_BLACK, 1, ""), false
        ));

        // Whenever chaos ensues, create a 2/2 black Zombie creature token.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(
                new CreateTokenEffect(new ZombieToken()), false
        ));
    }

    private TheFourthSpherePlane(final TheFourthSpherePlane plane) {
        super(plane);
    }

    @Override
    public TheFourthSpherePlane copy() {
        return new TheFourthSpherePlane(this);
    }
}
