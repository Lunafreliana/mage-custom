package org.mage.test.cards.single.who;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class RiverSongsDiaryTest extends CardTestPlayerBase {

    @Test
    public void exilesResolvedSpellsCastFromHandButNotCounteredSpells() {
        addCard(Zone.BATTLEFIELD, playerA, "River Song's Diary");
        addCard(Zone.BATTLEFIELD, playerA, "Island", 2);
        addCard(Zone.HAND, playerA, "Opt");
        addCard(Zone.HAND, playerA, "Counterspell");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Opt");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Counterspell", "Opt");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertExileCount("Counterspell", 1);
        assertGraveyardCount(playerA, "Counterspell", 0);
        assertGraveyardCount(playerA, "Opt", 1);
    }

    @Test
    public void randomlyChoosesAndOffersToCastAtFourImprintedCards() {
        addCard(Zone.BATTLEFIELD, playerA, "River Song's Diary");
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 4);
        addCard(Zone.HAND, playerA, "Lightning Bolt", 4);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Lightning Bolt", playerB);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Lightning Bolt", playerB);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Lightning Bolt", playerB);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Lightning Bolt", playerB);
        setChoice(3, PhaseStep.UPKEEP, playerA, true);
        addTarget(3, PhaseStep.UPKEEP, playerA, playerB);

        setStopAt(3, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertLife(playerB, 5);
        assertExileCount("Lightning Bolt", 3);
        assertGraveyardCount(playerA, "Lightning Bolt", 1);
    }
}
