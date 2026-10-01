package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.PlanarCardRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.player.TestPlayer;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class QuicksilverSeaTest extends CardTestPlayerBase {

    @Test
    public void planeswalkAndUpkeepScryFour() {
        removeAllCardsFromLibrary(playerA);
        skipInitShuffling();
        addPlane(playerA, Planes.PLANE_QUICKSILVER_SEA);
        addCard(Zone.LIBRARY, playerA, "Mountain", 8);

        setChoice(playerA, "When you planeswalk");
        addTarget(playerA, TestPlayer.TARGET_SKIP); // Planeswalk trigger: keep all four on top.
        addTarget(playerA, TestPlayer.TARGET_SKIP); // Upkeep trigger: keep all four on top.
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.DRAW);
        execute();

        assertLibraryCount(playerA, 8);
    }

    @Test
    public void chaosRevealsAndCastsTopCardForFree() {
        setUpChaosTest("Lightning Bolt");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        setChoice(playerA, true);
        addTarget(playerA, playerB);
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 17);
        assertGraveyardCount(playerA, "Lightning Bolt", 1);
    }

    @Test
    public void decliningChaosLeavesRevealedCardOnTop() {
        setUpChaosTest("Lightning Bolt");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        setChoice(playerA, false);
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLibraryCount(playerA, 1);
        assertGraveyardCount(playerA, "Lightning Bolt", 0);
    }

    @Test
    public void chaosCanPlayTopLand() {
        setUpChaosTest("Mountain");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        setChoice(playerA, true);
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Mountain", 1);
        assertLibraryCount(playerA, 0);
    }

    private void setUpChaosTest(String topCard) {
        removeAllCardsFromLibrary(playerA);
        skipInitShuffling();
        addPlane(playerA, Planes.PLANE_QUICKSILVER_SEA);
        addCard(Zone.LIBRARY, playerA, topCard);
        setChoice(playerA, "When you planeswalk");
        addTarget(playerA, TestPlayer.TARGET_SKIP); // Planeswalk scry.
        addTarget(playerA, TestPlayer.TARGET_SKIP); // Upkeep scry.

        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_QUICKSILVER_SEA)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Quicksilver Sea", metadata.getEnglishName());
        Assert.assertEquals("Plane - Quicksilver Sea", metadata.getImageName());
        Assert.assertEquals("PCA", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
