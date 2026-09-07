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

public class TheCyberControllerTest extends CardTestPlayerBase {

    private static final String FACE_DOWN = EmptyNames.FACE_DOWN_CREATURE.getTestCommand();

    @Test
    public void testMillsAndConvertsCreatureCards() {
        addCard(Zone.HAND, playerA, "The Cyber-Controller");
        addCard(Zone.BATTLEFIELD, playerA, "Island", 4);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp");
        addCard(Zone.LIBRARY, playerB, "Serra Angel");
        addCard(Zone.LIBRARY, playerB, "Forest");
        skipInitShuffling();

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "The Cyber-Controller");
        setChoice(playerA, "X=2");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, "The Cyber-Controller", 1);
        assertPermanentCount(playerA, FACE_DOWN, 1);
        assertGraveyardCount(playerB, "Forest", 1);
        assertGraveyardCount(playerB, "Serra Angel", 0);

        Permanent cyberman = getPermanent(FACE_DOWN, playerA);
        Assert.assertEquals("The Cyberman should have no name", "", cyberman.getName());
        Assert.assertTrue("The Cyberman should be colorless", cyberman.getColor(currentGame).isColorless());
        Assert.assertTrue("The Cyberman should have no mana cost", cyberman.getManaCost().isEmpty());
        Assert.assertTrue("The Cyberman should have no supertypes", cyberman.getSuperType(currentGame).isEmpty());
        assertPowerToughness(playerA, FACE_DOWN, 3, 3);
        assertType(FACE_DOWN, CardType.ARTIFACT, true);
        assertType(FACE_DOWN, CardType.CREATURE, true);
        assertSubtype(FACE_DOWN, SubType.CYBERMAN);
        assertNotSubtype(FACE_DOWN, SubType.ANGEL);
        assertAbility(playerA, FACE_DOWN, FlyingAbility.getInstance(), false);
    }

    @Test
    public void testBoostsOnlyOtherArtifactCreatures() {
        addCard(Zone.BATTLEFIELD, playerA, "The Cyber-Controller");
        addCard(Zone.BATTLEFIELD, playerA, "Memnite");
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPowerToughness(playerA, "The Cyber-Controller", 3, 3);
        assertPowerToughness(playerA, "Memnite", 2, 2);
        assertPowerToughness(playerA, "Grizzly Bears", 2, 2);
    }
}
