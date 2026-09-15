package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.abilities.effects.common.ChooseCreatureTypeEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.counter.AddCountersAllEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.mageobject.ChosenSubtypePredicate;
import mage.game.command.Plane;
import mage.game.permanent.token.ShapeshifterBlueToken;

/**
 * @author The XMage Developers
 */
public final class LittjaraPlane extends Plane {

    private static final FilterControlledCreaturePermanent FILTER_CHOSEN_TYPE
            = new FilterControlledCreaturePermanent("creatures you control of the chosen type");

    static {
        FILTER_CHOSEN_TYPE.add(ChosenSubtypePredicate.TRUE);
    }

    public LittjaraPlane() {
        this.setPlaneType(Planes.PLANE_LITTJARA);

        // When you planeswalk to Littjara and at the beginning of your upkeep, create a 2/2 blue
        // Shapeshifter creature token with changeling.
        this.getAbilities().add(new PlaneswalkToSourceTriggeredAbility(
                new CreateTokenEffect(new ShapeshifterBlueToken())
        ));
        this.getAbilities().add(new BeginningOfUpkeepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU,
                new CreateTokenEffect(new ShapeshifterBlueToken()), false
        ));

        // Whenever chaos ensues, choose a creature type. Put a +1/+1 counter on each creature you
        // control of that type.
        Ability ability = new ChaosEnsuesTriggeredAbility(
                new ChooseCreatureTypeEffect(Outcome.BoostCreature), false
        );
        ability.addEffect(new AddCountersAllEffect(
                CounterType.P1P1.createInstance(), FILTER_CHOSEN_TYPE
        ));
        this.getAbilities().add(ability);
    }

    private LittjaraPlane(final LittjaraPlane plane) {
        super(plane);
    }

    @Override
    public LittjaraPlane copy() {
        return new LittjaraPlane(this);
    }
}
