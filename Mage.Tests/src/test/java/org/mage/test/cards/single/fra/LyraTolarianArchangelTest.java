package org.mage.test.cards.single.fra;

import mage.abilities.keyword.FlyingAbility;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.SubType;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author muz
 */
public class LyraTolarianArchangelTest extends CardTestPlayerBase {

    private static final String lyra = "Lyra, Tolarian Archangel";

    @Test
    public void createsTokenAfterDrawingThreeCards() {
        addCard(Zone.BATTLEFIELD, playerA, lyra);
        addCard(Zone.HAND, playerA, "Ancestral Recall");
        addCard(Zone.BATTLEFIELD, playerA, "Island");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Ancestral Recall", playerA);

        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        assertPermanentCount(playerA, "Angel Token", 1);
        assertPowerToughness(playerA, "Angel Token", 3, 3);
        assertColor(playerA, "Angel Token", "U", true);
        assertType("Angel Token", CardType.CREATURE, SubType.ANGEL);
        assertAbility(playerA, "Angel Token", FlyingAbility.getInstance(), true);
    }

    @Test
    public void doesNotCreateTokenAfterDrawingOnlyTwoCards() {
        addCard(Zone.BATTLEFIELD, playerA, lyra);
        addCard(Zone.HAND, playerA, "Divination");
        addCard(Zone.BATTLEFIELD, playerA, "Island", 3);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Divination");

        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        assertPermanentCount(playerA, "Angel Token", 0);
    }

    @Test
    public void activatedAbilityDrawsOnCombatDamage() {
        addCard(Zone.BATTLEFIELD, playerA, lyra);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 5);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{3}{U}{U}:");
        attack(1, playerA, lyra, playerB);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 17);
        assertHandCount(playerA, 2);
    }
}
