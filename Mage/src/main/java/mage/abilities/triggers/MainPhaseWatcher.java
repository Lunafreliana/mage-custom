package mage.abilities.triggers;

import mage.constants.WatcherScope;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.watchers.Watcher;

/**
 * Tracks the number of main phases that have begun during the current turn.
 * Registered as a default game watcher because abilities that need it can be
 * introduced after initial watcher collection, including by planar cards.
 */
public final class MainPhaseWatcher extends Watcher {

    private int mainPhaseCount = 0;

    public MainPhaseWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        switch (event.getType()) {
            case PRECOMBAT_MAIN_PHASE_PRE:
            case POSTCOMBAT_MAIN_PHASE_PRE:
                mainPhaseCount++;
        }
    }

    @Override
    public void reset() {
        super.reset();
        this.mainPhaseCount = 0;
    }

    static boolean checkCount(Game game) {
        MainPhaseWatcher watcher = game.getState().getWatcher(MainPhaseWatcher.class);
        return watcher != null && watcher.mainPhaseCount == 2;
    }
}
