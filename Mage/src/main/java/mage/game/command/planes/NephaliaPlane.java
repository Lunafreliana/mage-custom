package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.effects.common.MillCardsControllerEffect;
import mage.abilities.effects.common.ReturnFromGraveyardAtRandomEffect;
import mage.abilities.effects.common.ReturnFromGraveyardToHandTargetEffect;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.command.Plane;
import mage.target.common.TargetCardInYourGraveyard;

/**
 * @author TheElk801
 */
public final class NephaliaPlane extends Plane {

    public NephaliaPlane() {
        this.setPlaneType(Planes.PLANE_NEPHALIA);

        // At the beginning of your end step, mill seven cards. Then return a card at random from your graveyard to your hand.
        Ability ability = new BeginningOfEndStepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU,
                new MillCardsControllerEffect(7).concatBy(". Then"), false, null
        );
        ability.addEffect(new ReturnFromGraveyardAtRandomEffect(StaticFilters.FILTER_CARD, Zone.HAND));
        this.getAbilities().add(ability);

        // Whenever chaos ensues, return target card from your graveyard to your hand.
        ability = new ChaosEnsuesTriggeredAbility(new ReturnFromGraveyardToHandTargetEffect(), false);
        ability.addTarget(new TargetCardInYourGraveyard());
        this.getAbilities().add(ability);
    }

    private NephaliaPlane(final NephaliaPlane plane) {
        super(plane);
    }

    @Override
    public NephaliaPlane copy() {
        return new NephaliaPlane(this);
    }
}
