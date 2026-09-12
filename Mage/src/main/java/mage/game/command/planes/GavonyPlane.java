package mage.game.command.planes;

import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.continuous.GainAbilityAllEffect;
import mage.abilities.effects.common.continuous.GainAbilityControlledEffect;
import mage.abilities.keyword.IndestructibleAbility;
import mage.abilities.keyword.VigilanceAbility;
import mage.constants.Duration;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.command.Plane;

/**
 * @author TheElk801
 */
public final class GavonyPlane extends Plane {

    public GavonyPlane() {
        this.setPlaneType(Planes.PLANE_GAVONY);

        // All creatures have vigilance.
        this.getAbilities().add(new SimpleStaticAbility(Zone.COMMAND, new GainAbilityAllEffect(
                VigilanceAbility.getInstance(), Duration.WhileOnBattlefield,
                StaticFilters.FILTER_PERMANENT_ALL_CREATURES
        )));

        // Whenever chaos ensues, creatures you control gain indestructible until end of turn.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(new GainAbilityControlledEffect(
                IndestructibleAbility.getInstance(), Duration.EndOfTurn,
                StaticFilters.FILTER_CONTROLLED_CREATURES
        ), false));
    }

    private GavonyPlane(final GavonyPlane plane) {
        super(plane);
    }

    @Override
    public GavonyPlane copy() {
        return new GavonyPlane(this);
    }
}
