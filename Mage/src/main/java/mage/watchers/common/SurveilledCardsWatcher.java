package mage.watchers.common;

import mage.MageObjectReference;
import mage.cards.Card;
import mage.constants.WatcherScope;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.watchers.Watcher;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks the cards each player put into their graveyard while surveilling this turn.
 *
 * @author OpenAI
 */
public class SurveilledCardsWatcher extends Watcher {

    private final Map<UUID, Set<MageObjectReference>> cardsByPlayer = new HashMap<>();

    /**
     * This is a default game watcher so that cards surveilled before a relevant
     * permanent enters the battlefield are still known.
     */
    public SurveilledCardsWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        // Cards are registered directly by Player.doSurveil after the zone move.
    }

    public void addCard(UUID playerId, Card card, Game game) {
        cardsByPlayer
                .computeIfAbsent(playerId, key -> new HashSet<>())
                .add(new MageObjectReference(card, game));
    }

    public boolean wasSurveilled(UUID playerId, Card card, Game game) {
        return cardsByPlayer
                .getOrDefault(playerId, Collections.emptySet())
                .contains(new MageObjectReference(card, game));
    }

    @Override
    public void reset() {
        super.reset();
        cardsByPlayer.clear();
    }
}
