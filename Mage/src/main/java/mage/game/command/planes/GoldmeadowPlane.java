package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.EntersBattlefieldAllTriggeredAbility;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.CreateTokenTargetEffect;
import mage.constants.Planes;
import mage.constants.SetTargetPointer;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.command.Plane;
import mage.game.permanent.token.GoatToken;

/**
 * @author The XMage Developers
 */
public final class GoldmeadowPlane extends Plane {

    public GoldmeadowPlane() {
        setPlaneType(Planes.PLANE_GOLDMEADOW);

        // Whenever a land enters, that land's controller creates three 0/1 white Goat creature tokens.
        getAbilities().add(new EntersBattlefieldAllTriggeredAbility(
                Zone.COMMAND,
                new CreateTokenTargetEffect(new GoatToken(), 3)
                        .setText("that land's controller creates three 0/1 white Goat creature tokens"),
                StaticFilters.FILTER_LAND, false, SetTargetPointer.PLAYER
        ));

        // Whenever chaos ensues, create a 0/1 white Goat creature token.
        Ability ability = new ChaosEnsuesTriggeredAbility(new CreateTokenEffect(new GoatToken()), false);
        getAbilities().add(ability);
    }

    private GoldmeadowPlane(final GoldmeadowPlane plane) {
        super(plane);
    }

    @Override
    public GoldmeadowPlane copy() {
        return new GoldmeadowPlane(this);
    }
}
