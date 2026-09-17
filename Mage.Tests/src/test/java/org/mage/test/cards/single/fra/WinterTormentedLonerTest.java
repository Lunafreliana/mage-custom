package org.mage.test.cards.single.fra;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author TheElk801
 */
public class WinterTormentedLonerTest extends CardTestPlayerBase {

    private static final String winter = "Winter, Tormented Loner";

    @Test
    public void testSacrificeAndPowerBoost() {
        addCard(Zone.HAND, playerA, winter);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 3);
        addCard(Zone.BATTLEFIELD, playerB, "Runeclaw Bear");
        addCard(Zone.GRAVEYARD, playerA, "Memnite");
        addCard(Zone.GRAVEYARD, playerA, "Jace Beleren");
        addCard(Zone.GRAVEYARD, playerA, "Darksteel Relic");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, winter);
        setChoice(playerA, true);
        setChoice(playerA, "Grizzly Bears");
        addTarget(playerB, "Runeclaw Bear");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, winter, 1);
        assertPowerToughness(playerA, winter, 3, 3);
        assertGraveyardCount(playerA, "Grizzly Bears", 1);
        assertGraveyardCount(playerB, "Runeclaw Bear", 1);
    }

    @Test
    public void testDeclineDoesNotMakeOpponentSacrifice() {
        addCard(Zone.HAND, playerA, winter);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 3);
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, winter);
        setChoice(playerA, false);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, winter, 1);
        assertPowerToughness(playerA, winter, 0, 3);
        assertPermanentCount(playerB, "Grizzly Bears", 1);
    }
}
