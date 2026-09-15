package mage.game.command.phenomena;

import mage.abilities.Ability;
import mage.abilities.DelayedTriggeredAbility;
import mage.abilities.common.EncounterPhenomenonTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Phenomena;
import mage.game.Game;
import mage.game.command.Phenomenon;
import mage.game.events.GameEvent;
import mage.players.Player;
import mage.players.PlayerList;

import java.util.UUID;

/**
 * @author Codex
 */
public final class TeamUpPhenomenon extends Phenomenon {

    public TeamUpPhenomenon() {
        super(Phenomena.TEAM_UP.getFullName());

        // Whenever you encounter this Phenomenon, for the rest of the game, each
        // player wins the game if the players to their left and right are eliminated.
        this.getAbilities().add(new EncounterPhenomenonTriggeredAbility(new TeamUpEffect())
                .setTriggerPhrase("Whenever you encounter this Phenomenon, "));
    }

    private TeamUpPhenomenon(final TeamUpPhenomenon phenomenon) {
        super(phenomenon);
    }

    @Override
    public TeamUpPhenomenon copy() {
        return new TeamUpPhenomenon(this);
    }
}

class TeamUpEffect extends OneShotEffect {

    TeamUpEffect() {
        super(Outcome.Win);
        staticText = "for the rest of the game, each player wins the game if the players "
                + "to their left and right are eliminated. (Multiple players can win the game this way.)";
    }

    private TeamUpEffect(final TeamUpEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        game.addDelayedTriggeredAbility(new TeamUpDelayedTriggeredAbility(), source);
        return TeamUpWinEffect.checkForWinner(game);
    }

    @Override
    public TeamUpEffect copy() {
        return new TeamUpEffect(this);
    }
}

class TeamUpDelayedTriggeredAbility extends DelayedTriggeredAbility {

    TeamUpDelayedTriggeredAbility() {
        super(new TeamUpWinEffect(), Duration.EndOfGame, false);
        setTriggerPhrase("Whenever a player is eliminated, ");
    }

    private TeamUpDelayedTriggeredAbility(final TeamUpDelayedTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.LOST;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return true;
    }

    @Override
    public TeamUpDelayedTriggeredAbility copy() {
        return new TeamUpDelayedTriggeredAbility(this);
    }

    @Override
    public String getRule() {
        return "Whenever a player is eliminated, each player wins the game if the players "
                + "to their left and right are eliminated.";
    }
}

class TeamUpWinEffect extends OneShotEffect {

    TeamUpWinEffect() {
        super(Outcome.Win);
        staticText = "each player wins the game if the players to their left and right are eliminated";
    }

    private TeamUpWinEffect(final TeamUpWinEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return checkForWinner(game);
    }

    static boolean checkForWinner(Game game) {
        PlayerList seats = game.getPlayerList().copy();
        for (UUID playerId : game.getPlayerList()) {
            Player player = game.getPlayer(playerId);
            if (player == null || !player.isInGame()) {
                continue;
            }
            seats.setCurrent(playerId);
            Player left = game.getPlayer(seats.getNext());
            seats.setCurrent(playerId);
            Player right = game.getPlayer(seats.getPrevious());
            if (isEliminated(left) && isEliminated(right)) {
                player.won(game);
                return true;
            }
        }
        return false;
    }

    private static boolean isEliminated(Player player) {
        return player == null || player.hasLost() || player.hasLeft();
    }

    @Override
    public TeamUpWinEffect copy() {
        return new TeamUpWinEffect(this);
    }
}
