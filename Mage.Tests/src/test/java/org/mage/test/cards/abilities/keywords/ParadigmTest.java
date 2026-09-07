package org.mage.test.cards.abilities.keywords;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * Tests for paradigm from Secrets of Strixhaven.
 *
 * @author TheElk801
 */
public class ParadigmTest extends CardTestPlayerBase {

    @Test
    public void testExilesAndRepeatsEveryFirstMainPhase() {
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 5);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.HAND, playerA, "Germination Practicum");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Germination Practicum");
        setChoice(playerA, false); // Declining once must not remove the lasting trigger.
        setChoice(playerA, true);

        setStrictChooseMode(true);
        setStopAt(5, PhaseStep.BEGIN_COMBAT);
        execute();

        assertExileCount("Germination Practicum", 1);
        assertGraveyardCount(playerA, "Germination Practicum", 0);
        assertPowerToughness(playerA, "Grizzly Bears", 6, 6);
    }

    @Test
    public void testOnlyFirstResolutionWithSameNameCreatesTrigger() {
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 10);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.HAND, playerA, "Germination Practicum", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Germination Practicum");
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Germination Practicum");
        setChoice(playerA, true);

        setStrictChooseMode(true);
        setStopAt(3, PhaseStep.BEGIN_COMBAT);
        execute();

        assertExileCount("Germination Practicum", 2);
        assertPowerToughness(playerA, "Grizzly Bears", 8, 8);
    }

    @Test
    public void testCounteredSpellDoesNotStartParadigm() {
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 5);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.HAND, playerA, "Germination Practicum");
        addCard(Zone.BATTLEFIELD, playerB, "Island", 2);
        addCard(Zone.HAND, playerB, "Counterspell");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Germination Practicum");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerB, "Counterspell", "Germination Practicum");

        setStrictChooseMode(true);
        setStopAt(3, PhaseStep.BEGIN_COMBAT);
        execute();

        assertExileCount("Germination Practicum", 0);
        assertGraveyardCount(playerA, "Germination Practicum", 1);
        assertPowerToughness(playerA, "Grizzly Bears", 2, 2);
    }
}
