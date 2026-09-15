package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.PlanarCardRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class MegafloraJungleTest extends CardTestPlayerBase {

    @Test
    public void boostsAllCreaturesWithManaValueTwoOrLess() {
        addPlane(playerA, Planes.PLANE_MEGAFLORA_JUNGLE);
        addCard(Zone.BATTLEFIELD, playerA, "Memnite");
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Hill Giant");
        addCard(Zone.BATTLEFIELD, playerB, "Silvercoat Lion");

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPowerToughness(playerA, "Memnite", 3, 3);
        assertPowerToughness(playerA, "Grizzly Bears", 4, 4);
        assertPowerToughness(playerA, "Hill Giant", 3, 3);
        assertPowerToughness(playerB, "Silvercoat Lion", 4, 4);
    }

    @Test
    public void chaosCreatesButterflyForPlanarController() {
        addPlane(playerA, Planes.PLANE_MEGAFLORA_JUNGLE);
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Butterfly", 1);
        assertPermanentCount(playerB, "Butterfly", 0);
        assertPowerToughness(playerA, "Butterfly", 3, 3);
        assertAbility(playerA, "Butterfly", FlyingAbility.getInstance(), true);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_MEGAFLORA_JUNGLE)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Megaflora Jungle", metadata.getEnglishName());
        Assert.assertEquals("Plane - Megaflora Jungle", metadata.getImageName());
        Assert.assertEquals("MOC", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
