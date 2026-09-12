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

public class TheMaelstromTest extends CardTestPlayerBase {

    @Test
    public void upkeepRevealCanPutPermanentOntoBattlefield() {
        addPlane(playerA, Planes.PLANE_THE_MAELSTROM);
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears");

        setChoice(playerA, "When you planeswalk"); // Order the initial planeswalk and upkeep triggers.
        setChoice(playerA, true); // Reveal the top card.
        setChoice(playerA, true); // Put the permanent onto the battlefield.
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.DRAW);
        execute();

        assertPermanentCount(playerA, "Grizzly Bears", 1);
        assertLibraryCount(playerA, 0);
    }

    @Test
    public void revealedNonPermanentGoesToBottom() {
        addPlane(playerA, Planes.PLANE_THE_MAELSTROM);
        addCard(Zone.LIBRARY, playerA, "Lightning Bolt");

        setChoice(playerA, "When you planeswalk"); // Order the initial planeswalk and upkeep triggers.
        setChoice(playerA, true); // Reveal the top card.
        setChoice(playerA, false); // Do not reveal it again for the other trigger.
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.DRAW);
        execute();

        assertLibraryCount(playerA, 1);
        assertGraveyardCount(playerA, "Lightning Bolt", 0);
        assertHandCount(playerA, 0);
    }

    @Test
    public void chaosReturnsPermanentCardFromPlanarControllersGraveyard() {
        addPlane(playerA, Planes.PLANE_THE_MAELSTROM);
        addCard(Zone.GRAVEYARD, playerA, "Grizzly Bears");
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        setChoice(playerA, "When you planeswalk"); // Order the initial planeswalk and upkeep triggers.
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Grizzly Bears");
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Grizzly Bears", 1);
        assertGraveyardCount(playerA, "Grizzly Bears", 0);
    }

    @Test
    public void registryExposesTheMaelstromMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_THE_MAELSTROM)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("The Maelstrom", metadata.getEnglishName());
        Assert.assertEquals("Plane - The Maelstrom", metadata.getImageName());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
