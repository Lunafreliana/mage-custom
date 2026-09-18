package mage.sets;

import mage.cards.ExpansionSet;
import mage.constants.Rarity;
import mage.constants.SetType;

/**
 * Technical home for original custom cards without an official printing.
 * Add SetCardInfo entries when the first CLUN cards are implemented.
 */
public final class CLUN extends ExpansionSet {

    private static final CLUN instance = new CLUN();

    public static CLUN getInstance() {
        return instance;
    }

    private CLUN() {
        // Technical registration date; CLUN has no official release date.
        super("CLUN", "CLUN", ExpansionSet.buildDate(2026, 9, 18), SetType.CUSTOM_SET);
        this.hasBoosters = false;
        this.hasBasicLands = false;

        cards.add(new SetCardInfo("Vulpes, Cursed Tail", 1, Rarity.RARE,
                mage.cards.v.VulpesCursedTail.class));
    }
}
