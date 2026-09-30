package org.mage.test.cards.single.clb;

import mage.abilities.keyword.HasteAbility;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class AlaundoTheSeerTest extends CardTestPlayerBase {

    @Test
    public void exilesCardWithManaValueTimeCounters() {
        addCard(Zone.BATTLEFIELD, playerA, "Alaundo the Seer");
        addCard(Zone.HAND, playerA, "Grizzly Bears");

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Draw a card");
        setChoice(playerA, "Grizzly Bears");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertExileCount(playerA, "Grizzly Bears", 1);
        assertCounterOnExiledCardCount("Grizzly Bears", CounterType.TIME, 2);
    }

    @Test
    public void removesCountersFromOtherCardsAndCastsCreatureWithHaste() {
        addCard(Zone.BATTLEFIELD, playerA, "Alaundo the Seer");
        addCard(Zone.HAND, playerA, "Grizzly Bears");
        addCard(Zone.HAND, playerA, "Hill Giant");
        addCard(Zone.HAND, playerA, "Alpine Grizzly");

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Draw a card");
        setChoice(playerA, "Grizzly Bears");
        activateAbility(3, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Draw a card");
        setChoice(playerA, "Hill Giant");
        activateAbility(5, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Draw a card");
        setChoice(playerA, "Alpine Grizzly");
        setChoice(playerA, true); // Cast Grizzly Bears when its last counter is removed.

        setStrictChooseMode(true);
        setStopAt(5, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, "Grizzly Bears", 1);
        assertAbility(playerA, "Grizzly Bears", HasteAbility.getInstance(), true);
        assertCounterOnExiledCardCount("Hill Giant", CounterType.TIME, 3);
        assertCounterOnExiledCardCount("Alpine Grizzly", CounterType.TIME, 3);
    }
}
