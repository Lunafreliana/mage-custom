package org.mage.test.cards.single.fra;

import mage.abilities.keyword.HexproofAbility;
import mage.abilities.keyword.IndestructibleAbility;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class VigorbloomCharmTest extends CardTestPlayerBase {

    private static final String charm = "Vigorbloom Charm";
    private static final String bear = "Grizzly Bears";
    private static final String hillGiant = "Hill Giant";

    @Test
    public void protectsControlledPermanentUntilEndOfTurn() {
        setStrictChooseMode(true);
        addCard(Zone.HAND, playerA, charm);
        addCard(Zone.BATTLEFIELD, playerA, bear);
        addCard(Zone.BATTLEFIELD, playerA, "Forest");
        addCard(Zone.BATTLEFIELD, playerA, "Plains");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, charm, bear);
        setModeChoice(playerA, "1");

        checkAbility("hexproof granted", 1, PhaseStep.POSTCOMBAT_MAIN,
                playerA, bear, HexproofAbility.class, true);
        checkAbility("indestructible granted", 1, PhaseStep.POSTCOMBAT_MAIN,
                playerA, bear, IndestructibleAbility.class, true);
        checkAbility("hexproof expires", 2, PhaseStep.UPKEEP,
                playerA, bear, HexproofAbility.class, false);
        checkAbility("indestructible expires", 2, PhaseStep.UPKEEP,
                playerA, bear, IndestructibleAbility.class, false);

        setStopAt(2, PhaseStep.UPKEEP);
        execute();
    }

    @Test
    public void drawsCardAndGainsLife() {
        setStrictChooseMode(true);
        skipInitShuffling();
        addCard(Zone.HAND, playerA, charm);
        addCard(Zone.LIBRARY, playerA, "Island");
        addCard(Zone.BATTLEFIELD, playerA, "Forest");
        addCard(Zone.BATTLEFIELD, playerA, "Plains");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, charm);
        setModeChoice(playerA, "2");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, "Island", 1);
        assertLife(playerA, 23);
    }

    @Test
    public void addsCounterBeforeCreatureFights() {
        setStrictChooseMode(true);
        addCard(Zone.HAND, playerA, charm);
        addCard(Zone.BATTLEFIELD, playerA, bear);
        addCard(Zone.BATTLEFIELD, playerB, hillGiant);
        addCard(Zone.BATTLEFIELD, playerA, "Forest");
        addCard(Zone.BATTLEFIELD, playerA, "Plains");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, charm);
        setModeChoice(playerA, "3");
        addTarget(playerA, bear);
        addTarget(playerA, hillGiant);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        // The counter makes the Bear 3/3 before it fights, so both 3/3 creatures die.
        assertGraveyardCount(playerA, bear, 1);
        assertGraveyardCount(playerB, hillGiant, 1);
    }
}
