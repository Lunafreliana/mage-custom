package mage.game.command.planes;

import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.continuous.BoostAllEffect;
import mage.constants.ComparisonType;
import mage.constants.Duration;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.mageobject.ManaValuePredicate;
import mage.game.command.Plane;
import mage.game.permanent.token.ButterflyToken;

/**
 * @author The XMage Developers
 */
public final class MegafloraJunglePlane extends Plane {

    private static final FilterCreaturePermanent FILTER
            = new FilterCreaturePermanent("creatures with mana value 2 or less");

    static {
        FILTER.add(new ManaValuePredicate(ComparisonType.OR_LESS, 2));
    }

    public MegafloraJunglePlane() {
        this.setPlaneType(Planes.PLANE_MEGAFLORA_JUNGLE);

        // Each creature with mana value 2 or less gets +2/+2.
        this.getAbilities().add(new SimpleStaticAbility(Zone.COMMAND, new BoostAllEffect(
                2, 2, Duration.WhileOnBattlefield, FILTER, false
        )));

        // Whenever chaos ensues, create a 1/1 green Insect creature token with flying named Butterfly.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(
                new CreateTokenEffect(new ButterflyToken()), false
        ));
    }

    private MegafloraJunglePlane(final MegafloraJunglePlane plane) {
        super(plane);
    }

    @Override
    public MegafloraJunglePlane copy() {
        return new MegafloraJunglePlane(this);
    }
}
