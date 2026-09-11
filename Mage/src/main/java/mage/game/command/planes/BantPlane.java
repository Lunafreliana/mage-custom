package mage.game.command.planes;

import mage.ObjectColor;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.IsStillOnPlaneCondition;
import mage.abilities.condition.common.TargetHasCounterCondition;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.continuous.GainAbilityAllEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.abilities.keyword.ExaltedAbility;
import mage.abilities.keyword.IndestructibleAbility;
import mage.constants.Duration;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.ColorPredicate;
import mage.game.command.Plane;
import mage.target.Target;
import mage.target.TargetPermanent;

/**
 * @author spjspj
 */
public class BantPlane extends Plane {

    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent("Creatures");
    private static final FilterCreaturePermanent filter2 = new FilterCreaturePermanent("Green, White or Blue creatures");

    static {
        filter2.add(Predicates.or(new ColorPredicate(ObjectColor.GREEN), new ColorPredicate(ObjectColor.WHITE), new ColorPredicate(ObjectColor.BLUE)));
    }

    private static final String rule = "{this} has indestructible as long as it has a divinity counter on it";
    private static final String exaltedRule = "All creatures have exalted";

    public BantPlane() {
        this.setPlaneType(Planes.PLANE_BANT);

        // All creatures have exalted
        SimpleStaticAbility ability
                = new SimpleStaticAbility(Zone.COMMAND, new ConditionalContinuousEffect(
                new GainAbilityAllEffect(new ExaltedAbility(), Duration.Custom, StaticFilters.FILTER_PERMANENT_CREATURE),
                new IsStillOnPlaneCondition(this.getName()),
                exaltedRule));

        this.getAbilities().add(ability);

        // Whenever chaos ensues, put a divinity counter on target green, white, or blue creature.  That creature gains indestructible for as long as it has a divinity counter on it.
        Effect chaosEffect = new ConditionalContinuousEffect(new GainAbilityTargetEffect(IndestructibleAbility.getInstance(), Duration.Custom), new TargetHasCounterCondition(CounterType.DIVINITY), rule);
        Target chaosTarget = new TargetPermanent(filter2);
        Effect chaosEffect2 = new AddCountersTargetEffect(CounterType.DIVINITY.createInstance());

        ChaosEnsuesTriggeredAbility chaosAbility = new ChaosEnsuesTriggeredAbility(chaosEffect2, false);
        chaosAbility.addEffect(chaosEffect);
        chaosAbility.addTarget(chaosTarget);
        this.getAbilities().add(chaosAbility);
    }

    private BantPlane(final BantPlane plane) {
        super(plane);
    }

    @Override
    public BantPlane copy() {
        return new BantPlane(this);
    }
}
