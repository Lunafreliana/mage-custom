package mage.game.command;

import mage.cards.decks.SupplementalDeckCard;
import mage.cards.decks.SupplementalDeckType;
import mage.game.Game;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Typed dispatch boundary; future variants register without changing Commander parsing. */
public final class SupplementalDeckRuntimeHandlers {

    private final Map<SupplementalDeckType, SupplementalDeckRuntimeHandler> handlers
            = new EnumMap<>(SupplementalDeckType.class);

    public SupplementalDeckRuntimeHandlers() {
        register(new PlanarSupplementalDeckRuntimeHandler());
    }

    public void register(SupplementalDeckRuntimeHandler handler) {
        if (handlers.put(handler.getType(), handler) != null) {
            throw new IllegalArgumentException("Duplicate supplemental handler: " + handler.getType());
        }
    }

    public void initialize(SupplementalDeckType type, UUID playerId,
                           List<SupplementalDeckCard> cards, Game game) {
        SupplementalDeckRuntimeHandler handler = handlers.get(type);
        if (handler == null) {
            throw new IllegalStateException("No runtime handler registered for " + type);
        }
        handler.initialize(playerId, cards, game);
    }
}
