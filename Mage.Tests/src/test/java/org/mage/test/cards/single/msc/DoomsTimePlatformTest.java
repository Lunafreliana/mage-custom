package org.mage.test.cards.single.msc;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * {@link mage.cards.d.DoomsTimePlatform Doom's Time Platform}
 */
public class DoomsTimePlatformTest extends CardTestPlayerBase {

    @Test
    public void testAttackSuspendsNonlandCardFromGraveyard() {
        addCard(Zone.BATTLEFIELD, playerA, "Doom's Time Platform");
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.GRAVEYARD, playerA, "Lightning Bolt");
        addCard(Zone.GRAVEYARD, playerA, "Mountain");

        attack(1, playerA, "Grizzly Bears");
        addTarget(playerA, "Lightning Bolt");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertExileCount(playerA, "Lightning Bolt", 1);
        assertCounterOnExiledCardCount("Lightning Bolt", CounterType.TIME, 2);
        assertGraveyardCount(playerA, "Mountain", 1);
    }
}
