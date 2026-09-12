package mage.watchers.common;

import mage.constants.WatcherScope;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.watchers.Watcher;

/** Tracks completed planeswalk operations for effects whose duration ends at the next planeswalk. */
public class PlaneswalkedWatcher extends Watcher {

    private int count;

    public PlaneswalkedWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.PLANESWALKED) {
            count++;
        }
    }

    public int getCount() {
        return count;
    }
}
