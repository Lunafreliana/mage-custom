package org.mage.test.cards.single.sos;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class EmeritusOfAbundanceTest extends CardTestPlayerBase {

    private static final String EMERITUS = "Emeritus of Abundance";
    private static final String REGROWTH = "Regrowth";

    @Test
    public void entersPreparedAndRegrowthReturnsAnyCard() {
        addCard(Zone.HAND, playerA, EMERITUS);
        addCard(Zone.GRAVEYARD, playerA, "Memnite");
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 5);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, EMERITUS);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPlayableAbility("prepared Regrowth is castable", 1, PhaseStep.PRECOMBAT_MAIN,
                playerA, "Cast " + REGROWTH, true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, REGROWTH, "Memnite");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, EMERITUS, 1);
        assertPowerToughness(playerA, EMERITUS, 3, 4);
        assertHandCount(playerA, "Memnite", 1);
        assertGraveyardCount(playerA, "Memnite", 0);
        assertExileCount(playerA, REGROWTH, 0);
    }

    @Test
    public void attackWithEightLandsPreparesEmeritusAgain() {
        addCard(Zone.HAND, playerA, EMERITUS);
        addCard(Zone.GRAVEYARD, playerA, "Memnite");
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 8);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, EMERITUS);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, REGROWTH, "Memnite");
        attack(3, playerA, EMERITUS, playerB);
        checkPlayableAbility("attack trigger prepared Emeritus again", 3, PhaseStep.POSTCOMBAT_MAIN,
                playerA, "Cast " + REGROWTH, true);

        setStrictChooseMode(true);
        setStopAt(3, PhaseStep.END_TURN);
        execute();

        assertExileCount(playerA, REGROWTH, 1);
    }

    @Test
    public void attackWithSevenLandsDoesNotPrepareEmeritusAgain() {
        addCard(Zone.HAND, playerA, EMERITUS);
        addCard(Zone.GRAVEYARD, playerA, "Memnite");
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 7);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, EMERITUS);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, REGROWTH, "Memnite");
        attack(3, playerA, EMERITUS, playerB);
        checkPlayableAbility("seven lands do not satisfy the attack trigger", 3, PhaseStep.POSTCOMBAT_MAIN,
                playerA, "Cast " + REGROWTH, false);

        setStrictChooseMode(true);
        setStopAt(3, PhaseStep.END_TURN);
        execute();

        assertExileCount(playerA, REGROWTH, 0);
    }
}
