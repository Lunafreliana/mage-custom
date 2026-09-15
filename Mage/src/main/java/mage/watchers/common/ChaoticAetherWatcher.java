package mage.watchers.common;

import mage.constants.CardType;
import mage.constants.WatcherScope;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.watchers.Watcher;

/** Tracks the rule-changing effect created by resolving Chaotic Aether. */
public class ChaoticAetherWatcher extends Watcher {

    public ChaoticAetherWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (condition
                && event.getType() == GameEvent.EventType.PLANESWALKED_AWAY
                && event.getAmount() == CardType.PLANE.ordinal()) {
            condition = false;
        }
    }

    public void activate() {
        condition = true;
    }
}
