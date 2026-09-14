package mage.watchers.common;

import mage.constants.WatcherScope;
import mage.game.Game;
import mage.game.events.DamagedEvent;
import mage.game.events.GameEvent;
import mage.watchers.Watcher;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks the players dealt combat damage during the current turn. This is a
 * default watcher so effects that become active partway through a turn can
 * still inspect combat damage dealt earlier that turn.
 */
public class PlayersDealtCombatDamageThisTurnWatcher extends Watcher {

    private final Set<UUID> players = new HashSet<>();

    public PlayersDealtCombatDamageThisTurnWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.DAMAGED_PLAYER
                && ((DamagedEvent) event).isCombatDamage()) {
            players.add(event.getTargetId());
        }
    }

    @Override
    public void reset() {
        super.reset();
        players.clear();
    }

    public int getCount() {
        return players.size();
    }
}
