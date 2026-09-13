package mage.watchers.common;

import mage.MageObjectReference;
import mage.constants.WatcherScope;
import mage.game.Game;
import mage.game.events.DamagedPlayerEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.watchers.Watcher;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks which player each creature dealt combat damage to during the current
 * combat. Unlike {@link DamagedPlayerThisCombatWatcher}, this retains damage
 * from every combat damage step until combat ends.
 */
public class CombatDamageToPlayerThisCombatWatcher extends Watcher {

    private final Map<MageObjectReference, UUID> damagedPlayers = new HashMap<>();

    public CombatDamageToPlayerThisCombatWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.END_COMBAT_STEP_POST) {
            damagedPlayers.clear();
            return;
        }
        if (event.getType() != GameEvent.EventType.DAMAGED_PLAYER
                || !((DamagedPlayerEvent) event).isCombatDamage()) {
            return;
        }
        Permanent permanent = game.getPermanent(event.getSourceId());
        if (permanent != null && permanent.isCreature(game)) {
            damagedPlayers.put(new MageObjectReference(permanent, game), event.getPlayerId());
        }
    }

    public UUID getDamagedPlayer(UUID permanentId, Game game) {
        return damagedPlayers.entrySet().stream()
                .filter(entry -> entry.getKey().refersTo(permanentId, game))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}
