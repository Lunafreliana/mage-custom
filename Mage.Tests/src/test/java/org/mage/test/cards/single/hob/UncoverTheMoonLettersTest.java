package org.mage.test.cards.single.hob;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class UncoverTheMoonLettersTest extends CardTestPlayerBase {

    private static final String uncover = "Uncover the Moon-Letters";

    @Test
    public void drawsForManaSpentThenDiscardsTwo() {
        addCard(Zone.BATTLEFIELD, playerA, uncover);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 3);
        addCard(Zone.HAND, playerA, "Divination");
        addCard(Zone.HAND, playerA, "Memnite", 2);
        addCard(Zone.LIBRARY, playerA, "Mountain", 5);
        skipInitShuffling();

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Divination");
        setChoice(playerA, true);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        // Three cards from Uncover, less two discarded, then two from Divination.
        assertHandCount(playerA, 5);
        assertGraveyardCount(playerA, 3);
    }

    @Test
    public void mayDeclineEntireEffect() {
        addCard(Zone.BATTLEFIELD, playerA, uncover);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 3);
        addCard(Zone.HAND, playerA, "Divination");
        addCard(Zone.HAND, playerA, "Memnite", 2);
        addCard(Zone.LIBRARY, playerA, "Mountain", 5);
        skipInitShuffling();

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Divination");
        setChoice(playerA, false);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        // Only Divination draws cards; declining also skips the discard.
        assertHandCount(playerA, 4);
        assertGraveyardCount(playerA, 1);
    }

    @Test
    public void creatureSpellDoesNotTrigger() {
        addCard(Zone.BATTLEFIELD, playerA, uncover);
        addCard(Zone.BATTLEFIELD, playerA, "Forest");
        addCard(Zone.HAND, playerA, "Llanowar Elves");
        addCard(Zone.HAND, playerA, "Memnite", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Llanowar Elves");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertHandCount(playerA, 2);
        assertPermanentCount(playerA, "Llanowar Elves", 1);
    }
}
