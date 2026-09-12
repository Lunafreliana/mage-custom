package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.IsStillOnPlaneCondition;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.effects.common.continuous.GainAbilityAllEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.abilities.keyword.BoastAbility;
import mage.constants.Duration;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.permanent.AttackedThisTurnPredicate;
import mage.game.command.Plane;
import mage.target.common.TargetCreaturePermanent;

/**
 * @author The XMage Developers
 */
public final class BesiegedVikingVillagePlane extends Plane {

    private static final FilterControlledCreaturePermanent filter
            = new FilterControlledCreaturePermanent("creature you control that attacked this turn");

    static {
        filter.add(AttackedThisTurnPredicate.instance);
    }

    public BesiegedVikingVillagePlane() {
        this.setPlaneType(Planes.PLANE_BESIEGED_VIKING_VILLAGE);

        // All creatures have "Boast — {1}: Put a +1/+1 counter on this creature."
        Ability boastAbility = new BoastAbility(
                new AddCountersSourceEffect(CounterType.P1P1.createInstance()).setText(
                        "put a +1/+1 counter on this creature"
                ), new GenericManaCost(1)
        );
        this.getAbilities().add(new SimpleStaticAbility(Zone.COMMAND, new ConditionalContinuousEffect(
                new GainAbilityAllEffect(
                        boastAbility, Duration.WhileOnBattlefield, StaticFilters.FILTER_PERMANENT_ALL_CREATURES
                ).withForceQuotes(),
                new IsStillOnPlaneCondition(this.getName()),
                "All creatures have \"Boast — {1}: Put a +1/+1 counter on this creature.\""
        )));

        // Whenever chaos ensues, put an indestructible counter on target creature you control that attacked this turn.
        Ability chaosAbility = new ChaosEnsuesTriggeredAbility(
                new AddCountersTargetEffect(CounterType.INDESTRUCTIBLE.createInstance()), false
        );
        chaosAbility.addTarget(new TargetCreaturePermanent(filter));
        this.getAbilities().add(chaosAbility);
    }

    private BesiegedVikingVillagePlane(final BesiegedVikingVillagePlane plane) {
        super(plane);
    }

    @Override
    public BesiegedVikingVillagePlane copy() {
        return new BesiegedVikingVillagePlane(this);
    }
}
