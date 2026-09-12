package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.CompoundAbility;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.DealsCombatDamageToAPlayerOrPlaneswalkerTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.IsStillOnPlaneCondition;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.continuous.GainAbilityControlledEffect;
import mage.abilities.effects.common.counter.DistributeCountersEffect;
import mage.abilities.keyword.TrampleAbility;
import mage.constants.Duration;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.permanent.ModifiedPredicate;
import mage.game.command.Plane;
import mage.target.common.TargetCreaturePermanentAmount;

/**
 * @author Susucr
 */
public class TowashiPlane extends Plane {

    private static final FilterControlledCreaturePermanent filter
            = new FilterControlledCreaturePermanent("modified creatures you control");

    static {
        filter.add(ModifiedPredicate.instance);
    }

    public TowashiPlane() {
        this.setPlaneType(Planes.PLANE_TOWASHI);

        // Modified creatures you control have trample and "Whenever this creature deals combat damage to a player or planeswalker, draw a card."
        Ability drawAbility = new DealsCombatDamageToAPlayerOrPlaneswalkerTriggeredAbility(
                new DrawCardSourceControllerEffect(1), false
        );
        this.getAbilities().add(new SimpleStaticAbility(Zone.COMMAND, new ConditionalContinuousEffect(
                new GainAbilityControlledEffect(
                        new CompoundAbility(TrampleAbility.getInstance(), drawAbility),
                        Duration.WhileOnBattlefield, filter
                ),
                new IsStillOnPlaneCondition(this.getName()),
                "Modified creatures you control have trample and \"Whenever this creature deals combat damage "
                        + "to a player or planeswalker, draw a card.\""
        )));

        // Whenever chaos ensues, distribute three +1/+1 counters among one, two, or three target creatures you control.
        Ability chaosAbility = new ChaosEnsuesTriggeredAbility(new DistributeCountersEffect(), false);
        chaosAbility.addTarget(new TargetCreaturePermanentAmount(3, StaticFilters.FILTER_CONTROLLED_CREATURES));
        this.getAbilities().add(chaosAbility);
    }

    private TowashiPlane(final TowashiPlane plane) {
        super(plane);
    }

    @Override
    public TowashiPlane copy() {
        return new TowashiPlane(this);
    }
}
