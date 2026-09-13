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

public class AretopolisTest extends CardTestPlayerBase {

    @Test
    public void planeswalkAndUpkeepTriggersAddCountersAndGainLife() {
        addPlane(playerA, Planes.PLANE_ARETOPOLIS);
        setChoice(playerA, "When you planeswalk");
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        // The planeswalk trigger gives 1 life; the upkeep trigger then gives 2.
        assertLife(playerA, 23);
    }

    @Test
    public void chaosAddsCounterThenDrawsForAllScrollCounters() {
        addPlane(playerA, Planes.PLANE_ARETOPOLIS);
        addCard(Zone.LIBRARY, playerA, "Mountain", 3);
        skipInitShuffling();
        SpellAbility chaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause chaos");
        chaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, chaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause chaos");
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        // One counter from arriving and one from chaos means chaos draws two.
        assertHandCount(playerA, "Mountain", 2);
    }

    @Test
    public void registryExposesAretopolisMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_ARETOPOLIS));

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Aretopolis", metadata.getEnglishName());
        Assert.assertEquals("Plane - Aretopolis", metadata.getImageName());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
