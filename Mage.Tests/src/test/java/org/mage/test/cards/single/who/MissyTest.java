package org.mage.test.cards.single.who;

import mage.abilities.keyword.FlyingAbility;
import mage.constants.CardType;
import mage.constants.EmptyNames;
import mage.constants.PhaseStep;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.permanent.Permanent;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class MissyTest extends CardTestPlayerBase {

    private static final String FACE_DOWN = EmptyNames.FACE_DOWN_CREATURE.getTestCommand();

    @Test
    public void testReturnsNonartifactCreatureAsTappedCyberman() {
        addCard(Zone.BATTLEFIELD, playerA, "Missy");
        addCard(Zone.BATTLEFIELD, playerB, "Serra Angel");
        addCard(Zone.HAND, playerA, "Murder");
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 3);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Murder", "Serra Angel");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        Permanent permanent = getPermanent(FACE_DOWN, playerA);
        Assert.assertEquals("The Cyberman should have no name", "", permanent.getName());
        Assert.assertTrue("The Cyberman should be colorless", permanent.getColor(currentGame).isColorless());
        Assert.assertTrue("The Cyberman should have no mana cost", permanent.getManaCost().isEmpty());
        assertTapped(FACE_DOWN, true);
        assertPowerToughness(playerA, FACE_DOWN, 2, 2);
        assertType(FACE_DOWN, CardType.ARTIFACT, true);
        assertType(FACE_DOWN, CardType.CREATURE, true);
        assertSubtype(FACE_DOWN, SubType.CYBERMAN);
        assertNotSubtype(FACE_DOWN, SubType.ANGEL);
        assertAbility(playerA, FACE_DOWN, FlyingAbility.getInstance(), false);
    }

    @Test
    public void testDoesNotReturnArtifactCreature() {
        addCard(Zone.BATTLEFIELD, playerA, "Missy");
        addCard(Zone.BATTLEFIELD, playerB, "Memnite");
        addCard(Zone.HAND, playerA, "Murder");
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 3);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Murder", "Memnite");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertGraveyardCount(playerB, "Memnite", 1);
        assertPermanentCount(playerA, FACE_DOWN, 0);
    }

    @Test
    public void testVillainousDamageChoice() {
        addCard(Zone.BATTLEFIELD, playerA, "Missy");
        addCard(Zone.BATTLEFIELD, playerA, "Memnite", 2);
        setChoice(playerB, true);

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertLife(playerB, 18);
        assertHandCount(playerA, 0);
    }

    @Test
    public void testVillainousDrawChoice() {
        addCard(Zone.BATTLEFIELD, playerA, "Missy");
        addCard(Zone.LIBRARY, playerA, "Island");
        skipInitShuffling();
        setChoice(playerB, false);

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertLife(playerB, 20);
        assertHandCount(playerA, 1);
    }
}
