package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.game.command.PlanarCardRegistry;
import mage.game.command.Plane;
import mage.game.command.planes.AretopolisPlane;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class AretopolisTest extends CardTestPlayerBase {

    @Test
    public void planeswalkUpkeepAndChaosUseCurrentScrollCount() {
        addPlane(playerA, Planes.PLANE_ARETOPOLIS);
        addCard(Zone.LIBRARY, playerA, "Forest", 10);
        skipInitShuffling();

        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        setChoice(playerA, "When you planeswalk"); // Order the initial planeswalk and upkeep triggers.
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        runCode("check counters", 1, PhaseStep.POSTCOMBAT_MAIN, playerA, (info, player, game) -> {
            Plane plane = game.getState().getFaceUpPlanes().get(0);
            Assert.assertEquals(info, 3, plane.getCounters().getCount(CounterType.SCROLL));
        });

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        // Arrival gains 1, upkeep gains 2, and chaos draws 3.
        assertLife(playerA, 23);
        assertHandCount(playerA, "Forest", 4); // One turn draw plus three cards from chaos.
    }

    @Test
    public void copyPreservesScrollCounters() {
        AretopolisPlane original = new AretopolisPlane();
        original.addCounter(CounterType.SCROLL.createInstance(4));

        AretopolisPlane copy = original.copy();

        Assert.assertEquals(4, copy.getCounters().getCount(CounterType.SCROLL));
        copy.addCounter(CounterType.SCROLL.createInstance());
        Assert.assertEquals("The copied counter collection must be independent", 4,
                original.getCounters().getCount(CounterType.SCROLL));
    }

    @Test
    public void registryExposesMetadata() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_ARETOPOLIS);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Aretopolis", metadata.getEnglishName());
        Assert.assertEquals("Plane - Aretopolis", metadata.getImageName());
        Assert.assertEquals("PCA", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }
}
