package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlaneswalkAwayFromSourceTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.abilities.effects.common.MillCardsControllerEffect;
import mage.abilities.effects.common.ReturnFromEachGraveyardToBattlefieldAllEffect;
import mage.abilities.effects.common.ReturnFromGraveyardToHandTargetEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.command.Plane;
import mage.target.common.TargetCardInYourGraveyard;

/**
 * @author VibecodingQueens
 */
public class InysHaenPlane extends Plane {

    public InysHaenPlane() {
        this.setPlaneType(Planes.PLANE_INYS_HAEN);

        // When you planeswalk to Inys Haen and at the beginning of your upkeep, mill three cards.
        this.getAbilities().add(new PlaneswalkToSourceTriggeredAbility(new MillCardsControllerEffect(3)));
        this.getAbilities().add(new BeginningOfUpkeepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU, new MillCardsControllerEffect(3), false));

        // When you planeswalk away from Inys Haen, each player returns all land cards
        // from their graveyard to the battlefield tapped.
        this.getAbilities().add(new PlaneswalkAwayFromSourceTriggeredAbility(
                new ReturnFromEachGraveyardToBattlefieldAllEffect(StaticFilters.FILTER_CARD_LANDS, true)));

        // Whenever chaos ensues, return target nonland card from your graveyard to your hand.
        Ability chaosAbility = new ChaosEnsuesTriggeredAbility(new ReturnFromGraveyardToHandTargetEffect(), false);
        chaosAbility.addTarget(new TargetCardInYourGraveyard(StaticFilters.FILTER_CARD_NON_LAND));
        this.getAbilities().add(chaosAbility);
    }

    private InysHaenPlane(final InysHaenPlane plane) {
        super(plane);
    }

    @Override
    public InysHaenPlane copy() {
        return new InysHaenPlane(this);
    }
}
