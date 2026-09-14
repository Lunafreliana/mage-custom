package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.filter.common.FilterArtifactPermanent;
import mage.filter.predicate.permanent.TokenPredicate;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.permanent.token.BlackLotusToken;
import mage.players.Player;

import java.util.UUID;

/**
 * @author The XMage Developers
 */
public final class BlackLotusLoungePlane extends Plane {

    public BlackLotusLoungePlane() {
        this.setPlaneType(Planes.PLANE_BLACK_LOTUS_LOUNGE);

        // When you planeswalk here and at the beginning of your upkeep, create a Black Lotus
        // artifact token.
        CreateTokenEffect createBlackLotus = new CreateTokenEffect(new BlackLotusToken());
        this.getAbilities().add(new PlaneswalkToSourceTriggeredAbility(createBlackLotus));
        this.getAbilities().add(new BeginningOfUpkeepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU, createBlackLotus.copy(), false
        ));

        // Whenever chaos ensues, each player draws a card for each artifact token they control.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(
                new BlackLotusLoungeDrawEffect(), false
        ));
    }

    private BlackLotusLoungePlane(final BlackLotusLoungePlane plane) {
        super(plane);
    }

    @Override
    public BlackLotusLoungePlane copy() {
        return new BlackLotusLoungePlane(this);
    }
}

class BlackLotusLoungeDrawEffect extends OneShotEffect {

    private static final FilterArtifactPermanent FILTER
            = new FilterArtifactPermanent("artifact tokens they control");

    static {
        FILTER.add(TokenPredicate.TRUE);
    }

    BlackLotusLoungeDrawEffect() {
        super(Outcome.DrawCard);
        staticText = "each player draws a card for each artifact token they control";
    }

    private BlackLotusLoungeDrawEffect(final BlackLotusLoungeDrawEffect effect) {
        super(effect);
    }

    @Override
    public BlackLotusLoungeDrawEffect copy() {
        return new BlackLotusLoungeDrawEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        for (UUID playerId : game.getState().getPlayersInRange(controller.getId(), game)) {
            Player player = game.getPlayer(playerId);
            if (player != null) {
                int count = game.getBattlefield().countAll(FILTER, playerId, game);
                player.drawCards(count, source, game);
            }
        }
        return true;
    }
}
