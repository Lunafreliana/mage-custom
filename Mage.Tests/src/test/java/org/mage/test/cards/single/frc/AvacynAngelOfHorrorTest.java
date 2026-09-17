package org.mage.test.cards.single.frc;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class AvacynAngelOfHorrorTest extends CardTestPlayerBase {

    @Test
    public void returnsAnotherNontokenCreatureAtNextEndStep() {
        addCard(Zone.BATTLEFIELD, playerA, "Avacyn, Angel of Horror");
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.HAND, playerB, "Murder");
        addCard(Zone.BATTLEFIELD, playerB, "Swamp", 3);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerB, "Murder", "Grizzly Bears");

        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        assertPermanentCount(playerA, "Grizzly Bears", 1);
        assertGraveyardCount(playerA, "Grizzly Bears", 0);
    }

    @Test
    public void returnsItselfAtNextEndStep() {
        addCard(Zone.BATTLEFIELD, playerA, "Avacyn, Angel of Horror");
        addCard(Zone.HAND, playerB, "Murder");
        addCard(Zone.BATTLEFIELD, playerB, "Swamp", 3);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerB, "Murder", "Avacyn, Angel of Horror");

        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        assertPermanentCount(playerA, "Avacyn, Angel of Horror", 1);
        assertGraveyardCount(playerA, "Avacyn, Angel of Horror", 0);
    }

    @Test
    public void doesNotReturnTokenCreature() {
        addCard(Zone.BATTLEFIELD, playerA, "Avacyn, Angel of Horror");
        addCard(Zone.HAND, playerA, "Raise the Alarm");
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 2);
        addCard(Zone.HAND, playerB, "Murder");
        addCard(Zone.BATTLEFIELD, playerB, "Swamp", 3);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Raise the Alarm");
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerB, "Murder", "Soldier Token");

        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        assertPermanentCount(playerA, "Soldier Token", 1);
    }
}
