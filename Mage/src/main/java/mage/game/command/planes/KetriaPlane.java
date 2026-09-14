package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.ExileUntilNonlandPermanentEffect;
import mage.abilities.effects.common.counter.AddCounterChoiceTargetEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.game.command.Plane;
import mage.target.common.TargetControlledCreaturePermanent;

/**
 * @author The XMage Developers
 */
public final class KetriaPlane extends Plane {

    public KetriaPlane() {
        this.setPlaneType(Planes.PLANE_KETRIA);

        // When you planeswalk to Ketria and at the beginning of your upkeep, put your choice of a
        // vigilance, menace, or trample counter on target creature you control.
        Effect counterEffect = new AddCounterChoiceTargetEffect(
                CounterType.VIGILANCE, CounterType.MENACE, CounterType.TRAMPLE
        ).setText("put your choice of a vigilance, menace, or trample counter on target creature you control");
        Ability arrivalAbility = new PlaneswalkToSourceTriggeredAbility(counterEffect);
        arrivalAbility.addTarget(new TargetControlledCreaturePermanent());
        this.getAbilities().add(arrivalAbility);
        Ability upkeepAbility = new BeginningOfUpkeepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU, counterEffect.copy(), false
        );
        upkeepAbility.addTarget(new TargetControlledCreaturePermanent());
        this.getAbilities().add(upkeepAbility);

        // Whenever chaos ensues, exile cards from the top of your library until you exile a nonland
        // permanent card. Put that card onto the battlefield or into your hand.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(
                new ExileUntilNonlandPermanentEffect(), false
        ));
    }

    private KetriaPlane(final KetriaPlane plane) {
        super(plane);
    }

    @Override
    public KetriaPlane copy() {
        return new KetriaPlane(this);
    }
}
