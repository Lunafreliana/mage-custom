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

import java.util.Arrays;

public class TardisBayTest extends CardTestPlayerBase {

    @Test
    public void firstQualifyingSpellOnYourTurnHasCascade() {
        skipInitShuffling();
        addPlane(playerA, Planes.PLANE_TARDIS_BAY);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 4);
        addCard(Zone.HAND, playerA, "Aven Skirmisher"); // mana value 1 does not use the effect
        addCard(Zone.HAND, playerA, "Hill Giant");
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Aven Skirmisher");
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Hill Giant");
        setChoice(playerA, true); // cast Grizzly Bears with cascade

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertPermanentCount(playerA, "Aven Skirmisher", 1);
        assertPermanentCount(playerA, "Hill Giant", 1);
        assertPermanentCount(playerA, "Grizzly Bears", 1);
    }

    @Test
    public void onlyFirstQualifyingSpellEachTurnHasCascade() {
        skipInitShuffling();
        addPlane(playerA, Planes.PLANE_TARDIS_BAY);
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 4);
        addCard(Zone.HAND, playerA, "Goblin Piker", 2);
        addCard(Zone.LIBRARY, playerA, "Memnite", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Goblin Piker");
        setChoice(playerA, true); // first Goblin Piker cascades into one Memnite
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Goblin Piker");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertPermanentCount(playerA, "Goblin Piker", 2);
        assertPermanentCount(playerA, "Memnite", 1);
    }

    @Test
    public void chaosGainsArtifactThenPlaneswalksInSharedMode() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(
                Planes.PLANE_TARDIS_BAY,
                Planes.PLANE_FIELDS_OF_SUMMER
        );
        addCard(Zone.BATTLEFIELD, playerB, "Hedron Archive");

        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Hedron Archive");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Hedron Archive", 1);
        Assert.assertEquals("Plane - Fields of Summer",
                currentGame.getState().getFaceUpPlanes().get(0).getName());
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_TARDIS_BAY)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("TARDIS Bay", metadata.getEnglishName());
        Assert.assertEquals("Plane - TARDIS Bay", metadata.getImageName());
        Assert.assertEquals("WHO", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
