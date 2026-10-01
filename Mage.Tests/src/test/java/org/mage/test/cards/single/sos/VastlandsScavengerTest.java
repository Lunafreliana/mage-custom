package org.mage.test.cards.single.sos;

import mage.abilities.keyword.DeathtouchAbility;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class VastlandsScavengerTest extends CardTestPlayerBase {

    private static final String SCAVENGER = "Vastlands Scavenger";
    private static final String PREPARE_SPELL = "Bind to Life";

    @Test
    public void entersPreparedAndBindToLifeReturnsChosenMilledCreature() {
        addCard(Zone.HAND, playerA, SCAVENGER);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 8);
        addCard(Zone.LIBRARY, playerA, "Colossal Dreadmaw");
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears");
        addCard(Zone.LIBRARY, playerA, "Island", 5);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, SCAVENGER);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPlayableAbility("prepared spell is available", 1, PhaseStep.PRECOMBAT_MAIN,
                playerA, "Cast " + PREPARE_SPELL, true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, PREPARE_SPELL);
        setChoice(playerA, "Colossal Dreadmaw");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, SCAVENGER, 1);
        assertPowerToughness(playerA, SCAVENGER, 4, 4);
        assertAbility(playerA, SCAVENGER, DeathtouchAbility.getInstance(), true);
        assertPermanentCount(playerA, "Colossal Dreadmaw", 1);
        assertGraveyardCount(playerA, "Grizzly Bears", 1);
        assertGraveyardCount(playerA, "Island", 5);
        assertExileCount(playerA, PREPARE_SPELL, 0);
    }

    @Test
    public void bindToLifeDoesNothingWhenNoCreatureIsMilled() {
        addCard(Zone.HAND, playerA, SCAVENGER);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 8);
        addCard(Zone.LIBRARY, playerA, "Island", 7);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, SCAVENGER);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, PREPARE_SPELL);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, SCAVENGER, 1);
        assertGraveyardCount(playerA, "Island", 7);
        assertExileCount(playerA, PREPARE_SPELL, 0);
    }
}
