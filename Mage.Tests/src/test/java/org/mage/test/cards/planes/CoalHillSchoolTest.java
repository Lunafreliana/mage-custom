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
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Collections;

public class CoalHillSchoolTest extends CardTestPlayerBase {

    private void setupPlanechase() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Collections.singletonList(Planes.PLANE_COAL_HILL_SCHOOL);

        removeAllCardsFromLibrary(playerA);
        removeAllCardsFromLibrary(playerB);
        addCard(Zone.LIBRARY, playerA, "Silvercoat Lion", 10);
        addCard(Zone.LIBRARY, playerB, "Memnite", 10);
        skipInitShuffling();
        setStrictChooseMode(true);
    }

    @Test
    public void historicSpellDrawsForItsCasterOnly() {
        setupPlanechase();
        addCard(Zone.HAND, playerB, "Memnite");

        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, "Memnite");
        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, "Silvercoat Lion", 0);
        // Player B's normal draw plus Coal Hill School's draw.
        assertHandCount(playerB, "Memnite", 2);
        assertLibraryCount(playerA, 10);
        assertLibraryCount(playerB, 8);
    }

    @Test
    public void nonhistoricSpellDoesNotDraw() {
        setupPlanechase();
        addCard(Zone.HAND, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Grizzly Bears");
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, "Silvercoat Lion", 0);
        assertLibraryCount(playerA, 10);
    }

    @Test
    public void chaosReturnsOnlyHistoricCard() {
        setupPlanechase();
        addCard(Zone.GRAVEYARD, playerA, "Memnite");
        addCard(Zone.GRAVEYARD, playerA, "Grizzly Bears");

        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Memnite");
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, "Memnite", 1);
        assertGraveyardCount(playerA, "Memnite", 0);
        assertGraveyardCount(playerA, "Grizzly Bears", 1);
    }

    @Test
    public void registryExposesCoalHillSchoolMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_COAL_HILL_SCHOOL));

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Coal Hill School", metadata.getEnglishName());
        Assert.assertEquals("Plane - Coal Hill School", metadata.getImageName());
        Assert.assertEquals("WHO", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
