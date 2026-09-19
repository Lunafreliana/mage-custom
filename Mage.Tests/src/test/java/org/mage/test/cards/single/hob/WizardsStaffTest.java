package org.mage.test.cards.single.hob;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class WizardsStaffTest extends CardTestPlayerBase {

    @Test
    public void testEquipWizardAndDoubleProwess() {
        addCard(Zone.BATTLEFIELD, playerA, "Wizard's Staff");
        addCard(Zone.BATTLEFIELD, playerA, "Prodigal Sorcerer"); // 1/1 Wizard
        addCard(Zone.BATTLEFIELD, playerA, "Island");
        addCard(Zone.BATTLEFIELD, playerA, "Mountain");
        addCard(Zone.HAND, playerA, "Shock");

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Equip Wizard {1}", "Prodigal Sorcerer");
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Shock", playerB);

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertPowerToughness(playerA, "Prodigal Sorcerer", 3, 3);
        assertLife(playerB, 18);
    }

    @Test
    public void testRegularEquipAndDoubleInnateTrigger() {
        addCard(Zone.BATTLEFIELD, playerA, "Wizard's Staff");
        addCard(Zone.BATTLEFIELD, playerA, "Ajani's Pridemate"); // 2/2; gets a +1/+1 counter on life gain
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 4);
        addCard(Zone.HAND, playerA, "Healing Salve");

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Equip {3}", "Ajani's Pridemate");
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Healing Salve", playerA);

        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        // Prowess has worn off; the Pridemate's own trigger resolved twice.
        assertPowerToughness(playerA, "Ajani's Pridemate", 4, 4);
        assertLife(playerA, 23);
    }
}
