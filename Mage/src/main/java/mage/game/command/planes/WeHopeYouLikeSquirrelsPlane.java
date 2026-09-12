package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SpellCastAllTriggeredAbility;
import mage.abilities.effects.common.CreateTokenTargetEffect;
import mage.abilities.effects.common.counter.AddCountersAllEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.constants.Planes;
import mage.constants.SetTargetPointer;
import mage.constants.SubType;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.filter.common.FilterControlledPermanent;
import mage.game.command.Plane;
import mage.game.permanent.token.SquirrelToken;

/**
 * @author The XMage Developers
 */
public final class WeHopeYouLikeSquirrelsPlane extends Plane {

    private static final FilterControlledPermanent FILTER_SQUIRRELS
            = new FilterControlledPermanent(SubType.SQUIRREL, "Squirrels you control");

    public WeHopeYouLikeSquirrelsPlane() {
        this.setPlaneType(Planes.PLANE_WE_HOPE_YOU_LIKE_SQUIRRELS);

        // At the beginning of each player's upkeep and whenever a player casts a spell,
        // that player creates a 1/1 green Squirrel creature token.
        this.getAbilities().add(new BeginningOfUpkeepTriggeredAbility(
                Zone.COMMAND, TargetController.EACH_PLAYER,
                new CreateTokenTargetEffect(new SquirrelToken()), false
        ));
        this.getAbilities().add(new SpellCastAllTriggeredAbility(
                Zone.COMMAND, new CreateTokenTargetEffect(new SquirrelToken()),
                StaticFilters.FILTER_SPELL_A, false, SetTargetPointer.PLAYER
        ));

        // Whenever chaos ensues, put a +1/+1 counter on each Squirrel you control.
        Ability chaosAbility = new ChaosEnsuesTriggeredAbility(
                new AddCountersAllEffect(CounterType.P1P1.createInstance(), FILTER_SQUIRRELS), false
        );
        this.getAbilities().add(chaosAbility);
    }

    private WeHopeYouLikeSquirrelsPlane(final WeHopeYouLikeSquirrelsPlane plane) {
        super(plane);
    }

    @Override
    public WeHopeYouLikeSquirrelsPlane copy() {
        return new WeHopeYouLikeSquirrelsPlane(this);
    }
}
