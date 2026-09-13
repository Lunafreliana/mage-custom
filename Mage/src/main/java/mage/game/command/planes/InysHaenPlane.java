package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlaneswalkAwayFromSourceTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.MillCardsControllerEffect;
import mage.abilities.effects.common.ReturnFromGraveyardToHandTargetEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.command.Plane;
import mage.players.Player;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.UUID;

/**
 * @author The XMage Developers
 */
public final class InysHaenPlane extends Plane {

    public InysHaenPlane() {
        this.setPlaneType(Planes.PLANE_INYS_HAEN);

        // When you planeswalk to Inys Haen and at the beginning of your upkeep, mill three cards.
        this.getAbilities().add(new PlaneswalkToSourceTriggeredAbility(new MillCardsControllerEffect(3)));
        this.getAbilities().add(new BeginningOfUpkeepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU, new MillCardsControllerEffect(3), false
        ));

        // When you planeswalk away from Inys Haen, each player returns all land cards from their graveyard to the battlefield tapped.
        this.getAbilities().add(new PlaneswalkAwayFromSourceTriggeredAbility(new InysHaenReturnLandsEffect()));

        // Whenever chaos ensues, return target nonland card from your graveyard to your hand.
        Ability ability = new ChaosEnsuesTriggeredAbility(new ReturnFromGraveyardToHandTargetEffect(), false);
        ability.addTarget(new TargetCardInYourGraveyard(StaticFilters.FILTER_CARD_NON_LAND));
        this.getAbilities().add(ability);
    }

    private InysHaenPlane(final InysHaenPlane plane) {
        super(plane);
    }

    @Override
    public InysHaenPlane copy() {
        return new InysHaenPlane(this);
    }
}

class InysHaenReturnLandsEffect extends OneShotEffect {

    InysHaenReturnLandsEffect() {
        super(Outcome.PutLandInPlay);
        staticText = "each player returns all land cards from their graveyard to the battlefield tapped";
    }

    private InysHaenReturnLandsEffect(final InysHaenReturnLandsEffect effect) {
        super(effect);
    }

    @Override
    public InysHaenReturnLandsEffect copy() {
        return new InysHaenReturnLandsEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Cards lands = new CardsImpl();
        for (UUID playerId : game.getState().getPlayersInRange(controller.getId(), game)) {
            Player player = game.getPlayer(playerId);
            if (player != null) {
                lands.addAllCards(player.getGraveyard().getCards(
                        StaticFilters.FILTER_CARD_LANDS, playerId, source, game
                ));
            }
        }
        return controller.moveCards(lands.getCards(game), Zone.BATTLEFIELD, source, game,
                true, false, true, null);
    }
}
