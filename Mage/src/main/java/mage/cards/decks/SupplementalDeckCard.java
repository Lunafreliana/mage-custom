package mage.cards.decks;

/**
 * Stable deck-building identity carried by a non-castable supplemental card.
 */
public interface SupplementalDeckCard {

    SupplementalDeckType getSupplementalDeckType();

    String getSupplementalDeckId();
}
