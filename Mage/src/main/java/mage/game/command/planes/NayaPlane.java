package mage.game.command.planes;

import mage.ObjectColor;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.PermanentsOnBattlefieldCount;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.continuous.BoostTargetEffect;
import mage.abilities.effects.common.continuous.PlayAdditionalLandsAllEffect;
import mage.constants.Duration;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.ColorPredicate;
import mage.game.command.Plane;
import mage.target.Target;
import mage.target.TargetPermanent;

/**
 * @author spjspj
 */
public class NayaPlane extends Plane {

    private static final FilterControlledCreaturePermanent filter = new FilterControlledCreaturePermanent("Red, Green or White creature");

    static {
        filter.add(Predicates.or(new ColorPredicate(ObjectColor.RED), new ColorPredicate(ObjectColor.GREEN), new ColorPredicate(ObjectColor.WHITE)));
    }

    public NayaPlane() {
        this.setPlaneType(Planes.PLANE_NAYA);

        // You may play any number of lands on each of your turns
        Ability ability = new SimpleStaticAbility(Zone.COMMAND, new PlayAdditionalLandsAllEffect(Integer.MAX_VALUE));
        this.getAbilities().add(ability);

        // Whenever chaos ensues, target red, green or white creature you control gets +1/+1 until end of turn for each land you control
        DynamicValue dynamicValue = new PermanentsOnBattlefieldCount(StaticFilters.FILTER_CONTROLLED_PERMANENT_LANDS);
        Effect chaosEffect = new BoostTargetEffect(dynamicValue, dynamicValue, Duration.EndOfTurn);
        Target chaosTarget = new TargetPermanent(filter);

        ChaosEnsuesTriggeredAbility chaosAbility = new ChaosEnsuesTriggeredAbility(chaosEffect, false);
        chaosAbility.addTarget(chaosTarget);
        this.getAbilities().add(chaosAbility);
    }

    private NayaPlane(final NayaPlane plane) {
        super(plane);
    }

    @Override
    public NayaPlane copy() {
        return new NayaPlane(this);
    }
}
