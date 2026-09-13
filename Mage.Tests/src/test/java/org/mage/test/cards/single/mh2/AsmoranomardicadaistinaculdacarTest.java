package org.mage.test.cards.single.mh2;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * Tests Asmoranomardicadaistinaculdacar and her signature Underworld Cookbook.
 */
public class AsmoranomardicadaistinaculdacarTest extends CardTestPlayerBase {

    private static final String ASMO = "Asmoranomardicadaistinaculdacar";
    private static final String COOKBOOK = "The Underworld Cookbook";

    @Test
    public void testCookbookDiscardEnablesAsmoAndAsmoFindsAnotherCookbook() {
        addCard(Zone.BATTLEFIELD, playerA, COOKBOOK);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp");
        addCard(Zone.HAND, playerA, "Grizzly Bears");
        addCard(Zone.HAND, playerA, ASMO);
        addCard(Zone.LIBRARY, playerA, COOKBOOK);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}, Discard a card");
        setChoice(playerA, "Grizzly Bears");
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, ASMO);
        setChoice(playerA, true); // Search for The Underworld Cookbook.
        addTarget(playerA, COOKBOOK);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertPermanentCount(playerA, ASMO, 1);
        assertPermanentCount(playerA, "Food Token", 1);
        assertHandCount(playerA, COOKBOOK, 1);
        assertGraveyardCount(playerA, "Grizzly Bears", 1);
    }

    @Test
    public void testSacrificedFoodsMakeTargetDealDamageToItself() {
        addCard(Zone.BATTLEFIELD, playerA, ASMO);
        addCard(Zone.BATTLEFIELD, playerA, "Gingerbrute");
        addCard(Zone.BATTLEFIELD, playerA, "Golden Egg");
        addCard(Zone.BATTLEFIELD, playerB, "Vampire Nighthawk");

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Sacrifice two Foods", "Vampire Nighthawk");
        setChoice(playerA, "Gingerbrute^Golden Egg");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Gingerbrute", 0);
        assertPermanentCount(playerA, "Golden Egg", 0);
        assertGraveyardCount(playerA, "Gingerbrute", 1);
        assertGraveyardCount(playerA, "Golden Egg", 1);
        assertGraveyardCount(playerB, "Vampire Nighthawk", 1);
        assertLife(playerB, 26); // Nighthawk's lifelink applies because it is the damage source.
    }

    @Test
    public void testCookbookReturnsAnyCreatureCard() {
        addCard(Zone.BATTLEFIELD, playerA, COOKBOOK);
        addCard(Zone.BATTLEFIELD, playerA, "Wastes", 4);
        addCard(Zone.GRAVEYARD, playerA, "Grizzly Bears");

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{4}, {T}, Sacrifice", "Grizzly Bears");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, "Grizzly Bears", 1);
        assertGraveyardCount(playerA, COOKBOOK, 1);
    }
}
