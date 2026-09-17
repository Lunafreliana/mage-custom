package org.mage.test.cards.single.fra;

import mage.abilities.keyword.FirstStrikeAbility;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author muz
 */
public class DanithaSwordOfHopeTest extends CardTestPlayerBase {

    private static final String danitha = "Danitha, Sword of Hope";

    @Test
    public void testCharacteristics() {
        addCard(Zone.BATTLEFIELD, playerA, danitha);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPowerToughness(playerA, danitha, 2, 2);
        assertAbility(playerA, danitha, FirstStrikeAbility.getInstance(), true);
    }

    @Test
    public void testEquipmentSpellDrawsCard() {
        addCard(Zone.BATTLEFIELD, playerA, danitha);
        addCard(Zone.HAND, playerA, "Bone Saw");
        addCard(Zone.LIBRARY, playerA, "Island");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Bone Saw");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, "Island", 1);
    }

    @Test
    public void testSpellTargetingControlledCreatureDrawsCard() {
        addCard(Zone.BATTLEFIELD, playerA, danitha);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Forest");
        addCard(Zone.HAND, playerA, "Giant Growth");
        addCard(Zone.LIBRARY, playerA, "Island");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Giant Growth", "Grizzly Bears");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, "Island", 1);
    }

    @Test
    public void testSpellTargetingOpponentsCreatureDoesNotDraw() {
        addCard(Zone.BATTLEFIELD, playerA, danitha);
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Forest");
        addCard(Zone.HAND, playerA, "Giant Growth");
        addCard(Zone.LIBRARY, playerA, "Island");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Giant Growth", "Grizzly Bears");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, "Island", 0);
    }

    @Test
    public void testTriggersOnlyOnceEachTurnAcrossBothConditions() {
        addCard(Zone.BATTLEFIELD, playerA, danitha);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Forest");
        addCard(Zone.HAND, playerA, "Bone Saw");
        addCard(Zone.HAND, playerA, "Giant Growth");
        addCard(Zone.LIBRARY, playerA, "Island", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Bone Saw");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Giant Growth", "Grizzly Bears");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, "Island", 1);
    }
}
