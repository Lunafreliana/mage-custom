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

public class GrandOssuaryTest extends CardTestPlayerBase {

    @Test
    public void deadCreaturesControllerDistributesCountersUsingLastKnownPower() {
        addPlane(playerA, Planes.PLANE_GRAND_OSSUARY);
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");
        addCard(Zone.BATTLEFIELD, playerB, "Silvercoat Lion");
        addCard(Zone.HAND, playerA, "Murder");
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 3);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Murder", "Hill Giant");
        addTargetAmount(playerB, "Silvercoat Lion", 3);
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertGraveyardCount(playerB, "Hill Giant", 1);
        assertPowerToughness(playerB, "Silvercoat Lion", 5, 5);
    }

    @Test
    public void chaosReplacesEachPlayersCreaturesBasedOnTheirTotalPower() {
        addPlane(playerA, Planes.PLANE_GRAND_OSSUARY);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertExileCount(playerA, "Grizzly Bears", 1);
        assertExileCount(playerB, "Hill Giant", 1);
        assertPermanentCount(playerA, "Saproling Token", 2);
        assertPermanentCount(playerB, "Saproling Token", 3);
    }

    @Test
    public void registryExposesGrandOssuaryMetadata() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_GRAND_OSSUARY);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Grand Ossuary", metadata.getEnglishName());
        Assert.assertEquals("Plane - Grand Ossuary", metadata.getImageName());
        Assert.assertEquals("PCA", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }
}
