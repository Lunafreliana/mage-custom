package org.mage.test.cards.single.sos;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class VastlandsScavengerTest extends CardTestPlayerBase {

    private static final String SCAVENGER = "Vastlands Scavenger";
    private static final String BIND_TO_LIFE = "Bind to Life";

    @Test
    public void preparedSpellMillsSevenAndPutsChosenCreatureOntoBattlefield() {
        skipInitShuffling();
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.HAND, playerA, "SOS-" + SCAVENGER);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 8);
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears");
        addCard(Zone.LIBRARY, playerA, "Hill Giant");
        addCard(Zone.LIBRARY, playerA, "Darksteel Relic", 5);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, SCAVENGER);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPlayableAbility("prepared spell is castable", 1, PhaseStep.PRECOMBAT_MAIN,
                playerA, "Cast " + BIND_TO_LIFE, true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, BIND_TO_LIFE);
        setChoice(playerA, "Grizzly Bears");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, SCAVENGER, 1);
        assertPermanentCount(playerA, "Grizzly Bears", 1);
        assertGraveyardCount(playerA, "Hill Giant", 1);
        assertGraveyardCount(playerA, "Darksteel Relic", 5);
        assertExileCount(playerA, BIND_TO_LIFE, 0);
    }

    @Test
    public void preparedSpellDoesNothingBeyondMillingWhenNoCreatureIsMilled() {
        skipInitShuffling();
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.HAND, playerA, "SOS-" + SCAVENGER);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 8);
        addCard(Zone.LIBRARY, playerA, "Darksteel Relic", 7);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, SCAVENGER);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, BIND_TO_LIFE);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, SCAVENGER, 1);
        assertGraveyardCount(playerA, "Darksteel Relic", 7);
        assertExileCount(playerA, BIND_TO_LIFE, 0);
    }
}
