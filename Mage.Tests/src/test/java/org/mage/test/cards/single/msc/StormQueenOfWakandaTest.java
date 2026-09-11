package org.mage.test.cards.single.msc;

import mage.abilities.keyword.FlyingAbility;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class StormQueenOfWakandaTest extends CardTestPlayerBase {

    @Test
    public void boostsAnotherAttackingCreature() {
        addCard(Zone.BATTLEFIELD, playerA, "Storm, Queen of Wakanda");
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");

        setStrictChooseMode(true);
        attack(1, playerA, "Storm, Queen of Wakanda");
        attack(1, playerA, "Grizzly Bears");
        addTarget(playerA, "Grizzly Bears");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPowerToughness(playerA, "Grizzly Bears", 6, 2);
        assertAbility(playerA, "Grizzly Bears", FlyingAbility.getInstance(), true);
    }

    @Test
    public void damagesFlyingCreatureAttackingController() {
        addCard(Zone.BATTLEFIELD, playerA, "Storm, Queen of Wakanda");
        addCard(Zone.BATTLEFIELD, playerB, "Wind Drake");

        attack(2, playerB, "Wind Drake", playerA);

        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertGraveyardCount(playerB, "Wind Drake", 1);
        assertLife(playerA, 20);
    }

    @Test
    public void doesNotDamageFlyingCreatureAttackingPlaneswalker() {
        addCard(Zone.BATTLEFIELD, playerA, "Storm, Queen of Wakanda");
        addCard(Zone.BATTLEFIELD, playerA, "Jace Beleren");
        addCard(Zone.BATTLEFIELD, playerB, "Wind Drake");

        attack(2, playerB, "Wind Drake", "Jace Beleren");

        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerB, "Wind Drake", 1);
    }
}
