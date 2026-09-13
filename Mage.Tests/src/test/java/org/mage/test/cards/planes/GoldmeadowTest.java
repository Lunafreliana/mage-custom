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

public class GoldmeadowTest extends CardTestPlayerBase {

    @Test
    public void eachLandsControllerCreatesThreeGoats() {
        addPlane(playerA, Planes.PLANE_GOLDMEADOW);
        addCard(Zone.HAND, playerA, "Forest");
        addCard(Zone.HAND, playerB, "Island");

        playLand(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Forest");
        playLand(2, PhaseStep.PRECOMBAT_MAIN, playerB, "Island");

        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Goat Token", 3);
        assertPermanentCount(playerB, "Goat Token", 3);
    }

    @Test
    public void chaosCreatesOneGoatForPlanarController() {
        addPlane(playerA, Planes.PLANE_GOLDMEADOW);
        addCustomCardWithSpell(playerA, createCauseChaosAbility(), null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Goat Token", 1);
        assertPermanentCount(playerB, "Goat Token", 0);
    }

    @Test
    public void isAvailableThroughPlanarCardRegistry() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_GOLDMEADOW);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Goldmeadow", metadata.getEnglishName());
        Assert.assertEquals("Plane - Goldmeadow", metadata.getImageName());
        Assert.assertEquals("PCA", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }

    private static SpellAbility createCauseChaosAbility() {
        SpellAbility ability = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        ability.addEffect(new ChaosEnsuesEffect());
        return ability;
    }
}
