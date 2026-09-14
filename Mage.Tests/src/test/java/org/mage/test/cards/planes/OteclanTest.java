package org.mage.test.cards.planes;

import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.PlanarCardRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class OteclanTest extends CardTestPlayerBase {

    @Test
    public void planeswalkingAndUpkeepCauseChaosAndDiscover() {
        addPlane(playerA, Planes.PLANE_OTECLAN);
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears", 2);
        skipInitShuffling();

        setChoice(playerA, "When you planeswalk"); // Order the initial planeswalk and upkeep triggers.
        setChoice(playerA, false); // Put the first discovered card into hand.
        setChoice(playerA, false); // Put the second discovered card into hand.

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertHandCount(playerA, "Grizzly Bears", 2);
        assertHandCount(playerB, 0);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_OTECLAN)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Oteclán", metadata.getEnglishName());
        Assert.assertEquals("Plane - Oteclán", metadata.getImageName());
        Assert.assertEquals("PUNK", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
