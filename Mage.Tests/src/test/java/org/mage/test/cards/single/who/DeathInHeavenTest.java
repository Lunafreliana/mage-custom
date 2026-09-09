package org.mage.test.cards.single.who;

import mage.constants.CardType;
import mage.constants.EmptyNames;
import mage.constants.PhaseStep;
import mage.constants.SubType;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class DeathInHeavenTest extends CardTestPlayerBase {

    private static final String deathInHeaven = "Death in Heaven";
    private static final String faceDown = EmptyNames.FACE_DOWN_CREATURE.getTestCommand();

    @Test
    public void testExilesGraveyardsAndReturnsOnlyCreatureCardsAsCybermen() {
        addCard(Zone.HAND, playerA, deathInHeaven);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 4);
        addCard(Zone.GRAVEYARD, playerA, "Bear Cub");
        addCard(Zone.LIBRARY, playerA, "Mountain", 10);
        addCard(Zone.GRAVEYARD, playerB, "Serra Angel");
        addCard(Zone.LIBRARY, playerB, "Island", 10);
        skipInitShuffling();

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, deathInHeaven);
        addTarget(playerA, playerB); // chapter I
        addTarget(playerA, playerA); // chapter II

        checkExileCount("First graveyard is exiled", 1, PhaseStep.POSTCOMBAT_MAIN, playerB, "Serra Angel", 1);
        checkExileCount("First player's milled cards are exiled", 1, PhaseStep.POSTCOMBAT_MAIN, playerB, "Island", 2);
        checkExileCount("Second graveyard is exiled", 3, PhaseStep.POSTCOMBAT_MAIN, playerA, "Bear Cub", 1);
        checkExileCount("Second player's milled cards are exiled", 3, PhaseStep.POSTCOMBAT_MAIN, playerA, "Mountain", 2);

        setStrictChooseMode(true);
        setStopAt(5, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, faceDown, 2);
        assertPowerToughness(playerA, faceDown, 2, 2);
        assertType(faceDown, CardType.ARTIFACT, true);
        assertType(faceDown, CardType.CREATURE, true);
        assertSubtype(faceDown, SubType.CYBERMAN);
        assertExileCount(playerA, "Mountain", 2);
        assertExileCount(playerB, "Island", 2);
        assertPermanentCount(playerA, deathInHeaven, 0);
    }
}
