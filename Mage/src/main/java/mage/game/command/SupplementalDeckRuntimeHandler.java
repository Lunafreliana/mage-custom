package mage.game.command;

import mage.cards.decks.SupplementalDeckCard;
import mage.cards.decks.SupplementalDeckType;
import mage.game.Game;

import java.util.List;
import java.util.UUID;

/** Variant-owned conversion from deck-building carriers to runtime state. */
public interface SupplementalDeckRuntimeHandler {

    SupplementalDeckType getType();

    void initialize(UUID playerId, List<SupplementalDeckCard> cards, Game game);
}
