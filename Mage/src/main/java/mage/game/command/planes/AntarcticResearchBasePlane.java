package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.abilities.dynamicvalue.common.PermanentsOnBattlefieldCount;
import mage.abilities.effects.common.continuous.AddCardSubTypeTargetEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.abilities.effects.keyword.InvestigateEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.constants.Duration;
import mage.constants.Planes;
import mage.constants.SubType;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.game.command.Plane;
import mage.target.common.TargetControlledCreaturePermanent;

/**
 * @author The XMage Developers
 */
public final class AntarcticResearchBasePlane extends Plane {

    public AntarcticResearchBasePlane() {
        this.setPlaneType(Planes.PLANE_ANTARCTIC_RESEARCH_BASE);

        // When you planeswalk to Antarctic Research Base and at the beginning of your upkeep, investigate.
        this.getAbilities().add(new PlaneswalkToSourceTriggeredAbility(new InvestigateEffect()));
        this.getAbilities().add(new BeginningOfUpkeepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU, new InvestigateEffect(), false
        ));

        // Whenever chaos ensues, put X +1/+1 counters on target creature you control, where X is the
        // number of artifacts you control. It becomes a Plant in addition to its other types.
        Ability ability = new ChaosEnsuesTriggeredAbility(new AddCountersTargetEffect(
                CounterType.P1P1.createInstance(),
                new PermanentsOnBattlefieldCount(StaticFilters.FILTER_CONTROLLED_PERMANENT_ARTIFACT)
        ), false);
        ability.addEffect(new AddCardSubTypeTargetEffect(SubType.PLANT, Duration.WhileOnBattlefield));
        ability.addTarget(new TargetControlledCreaturePermanent());
        this.getAbilities().add(ability);
    }

    private AntarcticResearchBasePlane(final AntarcticResearchBasePlane plane) {
        super(plane);
    }

    @Override
    public AntarcticResearchBasePlane copy() {
        return new AntarcticResearchBasePlane(this);
    }
}
