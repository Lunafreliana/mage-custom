package org.mage.test.cards.single.who;

import mage.abilities.keyword.FlyingAbility;
import mage.constants.CardType;
import mage.constants.EmptyNames;
import mage.constants.PhaseStep;
import mage.constants.SubType;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class CyberConversionTest extends CardTestPlayerBase {

    private static final String FACE_DOWN = EmptyNames.FACE_DOWN_CREATURE.getTestCommand();

    @Test
    public void testNormalCreatureBecomesCyberman() {
        addCard(Zone.BATTLEFIELD, playerA, "Island", 2);
        addCard(Zone.HAND, playerA, "Cyber Conversion");
        addCard(Zone.BATTLEFIELD, playerB, "Serra Angel");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cyber Conversion", "Serra Angel");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerB, FACE_DOWN, 1);
        assertPowerToughness(playerB, FACE_DOWN, 2, 2);
        assertType(FACE_DOWN, CardType.CREATURE, true);
        assertType(FACE_DOWN, CardType.ARTIFACT, true);
        assertSubtype(FACE_DOWN, SubType.CYBERMAN);
        assertNotSubtype(FACE_DOWN, SubType.ANGEL);
        assertColor(playerB, FACE_DOWN, "WUBRG", false);
        assertAbility(playerB, FACE_DOWN, FlyingAbility.getInstance(), false);
    }

    @Test
    public void testMorphCanTurnFaceUpAndLosesCybermanCharacteristics() {
        addCard(Zone.BATTLEFIELD, playerA, "Island", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 5);
        addCard(Zone.HAND, playerA, "Cyber Conversion");
        addCard(Zone.BATTLEFIELD, playerA, "Pine Walker");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cyber Conversion", "Pine Walker");
        activateAbility(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "{4}{G}: Turn this face-down permanent face up.");

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertPermanentCount(playerA, "Pine Walker", 1);
        assertPowerToughness(playerA, "Pine Walker", 5, 5);
        assertType("Pine Walker", CardType.ARTIFACT, false);
        assertNotSubtype("Pine Walker", SubType.CYBERMAN);
        assertSubtype("Pine Walker", SubType.ELEMENTAL);
    }

    @Test
    public void testDoubleFacedCreatureDoesNotTurnFaceDown() {
        addCard(Zone.BATTLEFIELD, playerA, "Island", 2);
        addCard(Zone.HAND, playerA, "Cyber Conversion");
        addCard(Zone.BATTLEFIELD, playerB, "Delver of Secrets");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cyber Conversion", "Delver of Secrets");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerB, "Delver of Secrets", 1);
        assertPermanentCount(playerB, FACE_DOWN, 0);
        assertPowerToughness(playerB, "Delver of Secrets", 1, 1);
    }
}
