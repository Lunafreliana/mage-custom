package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.continuous.AddCardSubTypeTargetEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.constants.Duration;
import mage.constants.Planes;
import mage.constants.SubType;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.game.command.Plane;
import mage.game.permanent.token.ShyTownWarriorToken;
import mage.target.common.TargetCreaturePermanent;
import mage.target.targetadjustment.ForEachPlayerTargetsAdjuster;
import mage.target.targetpointer.EachTargetPointer;

/**
 * @author The XMage Developers
 */
public final class ShyTownPlane extends Plane {

    public ShyTownPlane() {
        this.setPlaneType(Planes.PLANE_SHY_TOWN);

        // Whenever you planeswalk here and whenever chaos ensues, for each opponent, put a shy
        // counter on up to one target creature they control. That creature becomes a Coward in
        // addition to its other types.
        this.getAbilities().add(createPlaneswalkAbility());
        this.getAbilities().add(createChaosAbility());

        // At the beginning of your upkeep, create a 2/2 red Warrior creature token with haste and
        // "Cowards can't block Warriors."
        this.getAbilities().add(new BeginningOfUpkeepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU,
                new CreateTokenEffect(new ShyTownWarriorToken()), false
        ));
    }

    private static Ability createPlaneswalkAbility() {
        return configureTargets(new PlaneswalkToSourceTriggeredAbility(createCounterEffect()));
    }

    private static Ability createChaosAbility() {
        return configureTargets(new ChaosEnsuesTriggeredAbility(createCounterEffect(), false));
    }

    private static AddCountersTargetEffect createCounterEffect() {
        AddCountersTargetEffect effect = new AddCountersTargetEffect(CounterType.SHY.createInstance());
        effect.setTargetPointer(new EachTargetPointer());
        return effect;
    }

    private static Ability configureTargets(Ability ability) {
        ability.addEffect(new AddCardSubTypeTargetEffect(SubType.COWARD, Duration.WhileOnBattlefield)
                .setTargetPointer(new EachTargetPointer()));
        ability.addTarget(new TargetCreaturePermanent(0, 1));
        ability.setTargetAdjuster(new ForEachPlayerTargetsAdjuster(false, true));
        return ability;
    }

    private ShyTownPlane(final ShyTownPlane plane) {
        super(plane);
    }

    @Override
    public ShyTownPlane copy() {
        return new ShyTownPlane(this);
    }
}
