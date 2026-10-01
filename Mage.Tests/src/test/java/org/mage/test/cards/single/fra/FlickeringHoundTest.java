package org.mage.test.cards.single.fra;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author muz
 */
public class FlickeringHoundTest extends CardTestPlayerBase {

    @Test
    public void testCreatureSpellTriggersFlicker() {
        addCard(Zone.BATTLEFIELD, playerA, "Flickering Hound");
        addCard(Zone.BATTLEFIELD, playerA, "Wall of Omens");
        addCard(Zone.LIBRARY, playerA, "Island");
        addCard(Zone.HAND, playerA, "Memnite");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Memnite");
        addTarget(playerA, "Wall of Omens");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Wall of Omens", 1);
        assertHandCount(playerA, "Island", 1);
    }

    @Test
    public void testNoncreatureSpellDoesNotTrigger() {
        addCard(Zone.BATTLEFIELD, playerA, "Flickering Hound");
        addCard(Zone.BATTLEFIELD, playerA, "Wall of Omens");
        addCard(Zone.LIBRARY, playerA, "Island");
        addCard(Zone.HAND, playerA, "Mox Amber");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Mox Amber");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Wall of Omens", 1);
        assertHandCount(playerA, "Island", 0);
    }
}
