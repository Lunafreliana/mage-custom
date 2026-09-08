package org.mage.test.cards.single.who;

import mage.abilities.keyword.FlyingAbility;
import mage.constants.CardType;
import mage.constants.EmptyNames;
import mage.constants.PhaseStep;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.filter.Filter;
import mage.game.permanent.Permanent;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class CybershipTest extends CardTestPlayerBase {

    private static final String FACE_DOWN = EmptyNames.FACE_DOWN_CREATURE.getTestCommand();

    @Test
    public void testCombatDamagePutsTopTwoCardsOntoBattlefieldAsCybermen() {
        addCard(Zone.BATTLEFIELD, playerA, "Cybership");
        addCard(Zone.BATTLEFIELD, playerA, "Serra Angel");
        addCard(Zone.LIBRARY, playerB, "Mountain");
        addCard(Zone.LIBRARY, playerB, "Air Elemental");
        skipInitShuffling();

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Crew 4");
        setChoice(playerA, "Serra Angel");
        attack(1, playerA, "Cybership", playerB);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLibraryCount(playerB, 0);
        assertPermanentCount(playerA, FACE_DOWN, 2);
        int cybermenChecked = 0;
        for (Permanent permanent : currentGame.getBattlefield().getAllActivePermanents(playerA.getId())) {
            if (!permanent.isFaceDown(currentGame)) {
                continue;
            }
            cybermenChecked++;
            Assert.assertEquals("A Cyberman should have no name", "", permanent.getName());
            Assert.assertTrue("A Cyberman should be colorless", permanent.getColor(currentGame).isColorless());
            Assert.assertTrue("A Cyberman should have no mana cost", permanent.getManaCost().isEmpty());
            Assert.assertTrue("A Cyberman should have no supertypes", permanent.getSuperType(currentGame).isEmpty());
            Assert.assertEquals("Cyberman should be the only subtype", 1, permanent.getSubtype(currentGame).size());
        }
        Assert.assertEquals("Both cards should be face-down Cybermen", 2, cybermenChecked);
        assertPowerToughness(playerA, FACE_DOWN, 2, 2, Filter.ComparisonScope.All);
        assertType(FACE_DOWN, CardType.ARTIFACT, true);
        assertType(FACE_DOWN, CardType.CREATURE, true);
        assertSubtype(FACE_DOWN, SubType.CYBERMAN);
        assertNotSubtype(FACE_DOWN, SubType.ANGEL);
        assertAbility(playerA, FACE_DOWN, FlyingAbility.getInstance(), false);
    }

    @Test
    public void testUsesDamagedPlayersLibraryAndHandlesFewerThanTwoCards() {
        addCard(Zone.BATTLEFIELD, playerA, "Cybership");
        addCard(Zone.BATTLEFIELD, playerA, "Serra Angel");
        addCard(Zone.LIBRARY, playerA, "Mountain");
        addCard(Zone.LIBRARY, playerB, "Island");
        skipInitShuffling();

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Crew 4");
        setChoice(playerA, "Serra Angel");
        attack(1, playerA, "Cybership", playerB);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLibraryCount(playerA, 1);
        assertLibraryCount(playerB, 0);
        assertPermanentCount(playerA, FACE_DOWN, 1);
        assertPermanentCount(playerB, FACE_DOWN, 0);
    }
}
