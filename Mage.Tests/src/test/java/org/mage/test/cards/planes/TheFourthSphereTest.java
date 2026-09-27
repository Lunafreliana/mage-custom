package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.PlanarCardRegistry;
import mage.game.command.planes.TheFourthSpherePlane;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Collections;

public class TheFourthSphereTest extends CardTestPlayerBase {

    @Test
    public void eachPlanarControllerSacrificesANonblackCreatureDuringTheirUpkeep() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Collections.singletonList(Planes.PLANE_THE_FOURTH_SPHERE);
        addCard(Zone.BATTLEFIELD, playerA, "Silvercoat Lion");
        addCard(Zone.BATTLEFIELD, playerA, "Walking Corpse");
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");

        setStopAt(2, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Silvercoat Lion", 0);
        assertPermanentCount(playerA, "Walking Corpse", 1);
        assertPermanentCount(playerB, "Grizzly Bears", 0);
    }

    @Test
    public void chaosCreatesAZombieForThePlanarController() {
        addPlane(playerA, Planes.PLANE_THE_FOURTH_SPHERE);
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Zombie Token", 1);
        assertPermanentCount(playerB, "Zombie Token", 0);
    }

    @Test
    public void isAvailableThroughPlanarCardRegistry() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_THE_FOURTH_SPHERE);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals("The Fourth Sphere", metadata.getEnglishName());
        Assert.assertEquals("Plane - The Fourth Sphere", metadata.getImageName());
        Assert.assertEquals("PCA", metadata.getSetCode());
        Assert.assertTrue(PlanarCardRegistry.create(id) instanceof TheFourthSpherePlane);
    }
}
