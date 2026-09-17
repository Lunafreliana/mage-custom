package mage.sets;

import mage.cards.ExpansionSet;
import mage.constants.SetType;

/**
 * Technical home for original custom cards without an official printing.
 * Add SetCardInfo entries when the first CLUN cards are implemented.
 */
public final class Clun extends ExpansionSet {

    private static final Clun instance = new Clun();

    public static Clun getInstance() {
        return instance;
    }

    private Clun() {
        // Technical registration date; CLUN has no official release date.
        super("CLUN", "CLUN", ExpansionSet.buildDate(2026, 9, 18), SetType.CUSTOM_SET);
        this.hasBoosters = false;
        this.hasBasicLands = false;
    }
}
