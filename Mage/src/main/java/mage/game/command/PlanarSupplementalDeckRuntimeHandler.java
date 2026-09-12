package mage.game.command;

import mage.cards.decks.SupplementalDeckCard;
import mage.cards.decks.SupplementalDeckType;
import mage.game.Game;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/** Builds one independently shuffled planar deck for the contributing player. */
public final class PlanarSupplementalDeckRuntimeHandler implements SupplementalDeckRuntimeHandler {

    @Override
    public SupplementalDeckType getType() {
        return SupplementalDeckType.PLANAR;
    }

    @Override
    public void initialize(UUID playerId, List<SupplementalDeckCard> cards, Game game) {
        List<PlanarCard> planarCards = cards.stream()
                .map(SupplementalDeckCard::getSupplementalDeckId)
                .map(PlanarCardRegistry::create)
                .filter(Objects::nonNull)
                .peek(card -> card.setPlanarDeckOwnerId(playerId))
                .collect(Collectors.toList());
        game.getState().setPlayerPlanarDeck(playerId, planarCards, true);
    }
}
