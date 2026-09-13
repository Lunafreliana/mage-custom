package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.continuous.BoostAllCreaturesForSharedCreatureTypesEffect;
import mage.abilities.effects.common.continuous.GainAllCreatureTypesTargetEffect;
import mage.constants.Duration;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.Plane;
import mage.target.common.TargetCreaturePermanent;

/**
 * @author The XMage Developers
 */
public final class VelisVelPlane extends Plane {

    public VelisVelPlane() {
        setPlaneType(Planes.PLANE_VELIS_VEL);

        // Each creature gets +1/+1 for each other creature on the battlefield that shares at least one creature type with it.
        getAbilities().add(new SimpleStaticAbility(
                Zone.COMMAND, new BoostAllCreaturesForSharedCreatureTypesEffect()
        ));

        // Whenever chaos ensues, target creature gains all creature types until end of turn.
        Ability ability = new ChaosEnsuesTriggeredAbility(
                new GainAllCreatureTypesTargetEffect(Duration.EndOfTurn), false
        );
        ability.addTarget(new TargetCreaturePermanent());
        getAbilities().add(ability);
    }

    private VelisVelPlane(final VelisVelPlane plane) {
        super(plane);
    }

    @Override
    public VelisVelPlane copy() {
        return new VelisVelPlane(this);
    }
}
