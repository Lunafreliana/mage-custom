package mage.abilities.effects.common;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.filter.FilterCard;
import mage.game.Game;
import mage.players.Player;

import java.util.UUID;

/** Returns the matching cards in each player's graveyard under that player's control. */
public class ReturnFromEachGraveyardToBattlefieldAllEffect extends OneShotEffect {

    private final FilterCard filter;
    private final boolean tapped;

    public ReturnFromEachGraveyardToBattlefieldAllEffect(FilterCard filter, boolean tapped) {
        super(Outcome.PutCardInPlay);
        this.filter = filter;
        this.tapped = tapped;
        staticText = "each player returns all " + filter.getMessage()
                + " from their graveyard to the battlefield" + (tapped ? " tapped" : "");
    }

    protected ReturnFromEachGraveyardToBattlefieldAllEffect(
            final ReturnFromEachGraveyardToBattlefieldAllEffect effect) {
        super(effect);
        this.filter = effect.filter;
        this.tapped = effect.tapped;
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
                player.moveCards(player.getGraveyard().getCards(filter, playerId, source, game),
                        Zone.BATTLEFIELD, source, game, tapped, false, false, null);
            }
        }
        return true;
    }

    @Override
    public ReturnFromEachGraveyardToBattlefieldAllEffect copy() {
        return new ReturnFromEachGraveyardToBattlefieldAllEffect(this);
    }
}
