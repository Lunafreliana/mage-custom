package mage.game.command.planes;

import mage.abilities.common.AttacksAllTriggeredAbility;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.effects.common.BoostTargetEffect;
import mage.abilities.effects.common.RevealCardsFromLibraryUntilEffect;
import mage.constants.Duration;
import mage.constants.Planes;
import mage.constants.PutCards;
import mage.constants.SetTargetPointer;
import mage.constants.Zone;
import mage.filter.FilterCard;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.mageobject.HistoricPredicate;
import mage.game.command.Plane;

/**
 * @author The XMage Developers
 */
public final class NewArgivePlane extends Plane {

    private static final FilterControlledCreaturePermanent historicCreatureFilter
            = new FilterControlledCreaturePermanent("historic creature you control");
    private static final FilterCard historicCardFilter = new FilterCard("historic card");

    static {
        historicCreatureFilter.add(HistoricPredicate.instance);
        historicCardFilter.add(HistoricPredicate.instance);
    }

    public NewArgivePlane() {
        this.setPlaneType(Planes.PLANE_NEW_ARGIVE);

        // Whenever a historic creature you control attacks, it gets +2/+2 until end of turn.
        this.getAbilities().add(new AttacksAllTriggeredAbility(
                Zone.COMMAND, new BoostTargetEffect(2, 2, Duration.EndOfTurn)
                        .setText("it gets +2/+2 until end of turn"), false,
                historicCreatureFilter, SetTargetPointer.PERMANENT, false, false
        ));

        // Whenever chaos ensues, reveal cards from the top of your library until you reveal a historic card.
        // Put that card into your hand and the rest on the bottom of your library in a random order.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(
                new RevealCardsFromLibraryUntilEffect(
                        historicCardFilter, PutCards.HAND, PutCards.BOTTOM_RANDOM
                ), false
        ));
    }

    private NewArgivePlane(final NewArgivePlane plane) {
        super(plane);
    }

    @Override
    public NewArgivePlane copy() {
        return new NewArgivePlane(this);
    }
}
