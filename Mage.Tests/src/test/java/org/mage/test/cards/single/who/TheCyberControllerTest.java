package org.mage.test.cards.single.who;

import mage.constants.CardType;
import mage.constants.EmptyNames;
import mage.constants.PhaseStep;
import mage.constants.SubType;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class TheCyberControllerTest extends CardTestPlayerBase {

    private static final String FACE_DOWN = EmptyNames.FACE_DOWN_CREATURE.getTestCommand();

    @Test
    public void testCreatureCardsBecomeArtifactCreatureCybermen() {
        addCard(Zone.HAND, playerA, "The Cyber-Controller");
        addCard(Zone.BATTLEFIELD, playerA, "Island", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 3);
        addCard(Zone.LIBRARY, playerB, "Mountain");
        addCard(Zone.LIBRARY, playerB, "Serra Angel");
        skipInitShuffling();

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "The Cyber-Controller");
        setChoice(playerA, "X=2");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, "The Cyber-Controller", 1);
        assertPermanentCount(playerA, FACE_DOWN, 1);
        assertPowerToughness(playerA, FACE_DOWN, 3, 3);
        assertType(FACE_DOWN, CardType.ARTIFACT, true);
        assertType(FACE_DOWN, CardType.CREATURE, true);
        assertSubtype(FACE_DOWN, SubType.CYBERMAN);
        assertNotSubtype(FACE_DOWN, SubType.ANGEL);
        assertGraveyardCount(playerB, "Mountain", 1);
    }
}
