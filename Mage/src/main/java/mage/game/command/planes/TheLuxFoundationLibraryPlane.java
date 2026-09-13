package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.DealsDamageToAPlayerAllTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.continuous.MaximumHandSizeControllerEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.constants.Duration;
import mage.constants.Planes;
import mage.constants.SetTargetPointer;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.game.command.Plane;
import mage.target.common.TargetCreaturePermanent;

/**
 * @author The XMage Developers
 */
public final class TheLuxFoundationLibraryPlane extends Plane {

    public TheLuxFoundationLibraryPlane() {
        this.setPlaneType(Planes.PLANE_THE_LUX_FOUNDATION_LIBRARY);

        // Players have no maximum hand size.
        this.getAbilities().add(new SimpleStaticAbility(Zone.COMMAND, new MaximumHandSizeControllerEffect(
                Integer.MAX_VALUE, Duration.WhileOnBattlefield,
                MaximumHandSizeControllerEffect.HandSizeModification.SET, TargetController.ANY
        )));

        // Whenever a creature you control deals combat damage to a player, you may draw a card.
        this.getAbilities().add(new DealsDamageToAPlayerAllTriggeredAbility(
                Zone.COMMAND, new DrawCardSourceControllerEffect(1),
                StaticFilters.FILTER_CONTROLLED_A_CREATURE, true,
                SetTargetPointer.NONE, true, false
        ));

        // Whenever chaos ensues, put a shadow counter on target creature.
        Ability ability = new ChaosEnsuesTriggeredAbility(
                new AddCountersTargetEffect(CounterType.SHADOW.createInstance()), false
        );
        ability.addTarget(new TargetCreaturePermanent());
        this.getAbilities().add(ability);
    }

    private TheLuxFoundationLibraryPlane(final TheLuxFoundationLibraryPlane plane) {
        super(plane);
    }

    @Override
    public TheLuxFoundationLibraryPlane copy() {
        return new TheLuxFoundationLibraryPlane(this);
    }
}
