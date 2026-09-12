package mage.cards.decks;

/**
 * Rules identity of a deck which is stored with, but is not part of, a player's
 * Magic deck or sideboard.
 */
public enum SupplementalDeckType {
    PLANAR("Planar Deck"),
    ATTRACTION("Attraction Deck");

    private final String displayName;

    SupplementalDeckType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
