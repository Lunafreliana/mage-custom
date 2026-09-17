package org.mage.test.cards.single.fra;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author OpenAI
 */
public class TheorixCharmTest extends CardTestPlayerBase {

    private static final String charm = "Theorix Charm";

    @Test
    public void testCounterMode() {
        addCard(Zone.HAND, playerA, charm);
        addCard(Zone.HAND, playerA, "Opt");
        addCard(Zone.BATTLEFIELD, playerA, "Underground Sea", 3);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Opt");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, charm, "Opt", "Opt");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertGraveyardCount(playerA, "Opt", 1);
        assertGraveyardCount(playerA, charm, 1);
    }

    @Test
    public void testShrinkMode() {
        addCard(Zone.HAND, playerA, charm);
        addCard(Zone.BATTLEFIELD, playerA, "Underground Sea", 2);
        addCard(Zone.BATTLEFIELD, playerB, "Centaur Courser");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, charm, "Centaur Courser");
        setModeChoice(playerA, "2");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPowerToughness(playerB, "Centaur Courser", 1, 1);
    }

    @Test
    public void testMillThenDrawMode() {
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.LIBRARY, playerA, "Forest", 5);
        addCard(Zone.HAND, playerA, charm);
        addCard(Zone.BATTLEFIELD, playerA, "Underground Sea", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, charm);
        setModeChoice(playerA, "3");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertLibraryCount(playerA, 1);
        assertGraveyardCount(playerA, "Forest", 3);
        assertHandCount(playerA, "Forest", 1);
    }
}
