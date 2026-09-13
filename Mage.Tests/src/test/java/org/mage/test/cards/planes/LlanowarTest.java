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

/** Focused behavior tests for Llanowar. */
public class LlanowarTest extends CardTestPlayerBase {

    @Test
    public void allCreaturesCanTapForTwoGreenMana() {
        addPlane(playerA, Planes.PLANE_LLANOWAR);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Upwelling");
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");
        addCard(Zone.BATTLEFIELD, playerB, "Upwelling");

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {G}{G}");
        checkManaPool("Llanowar mana ability", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "G", 2);
        activateAbility(2, PhaseStep.PRECOMBAT_MAIN, playerB, "{T}: Add {G}{G}");
        checkManaPool("Llanowar mana ability for nonowner", 2, PhaseStep.PRECOMBAT_MAIN, playerB, "G", 2);

        setStopAt(2, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertTapped("Grizzly Bears", true);
        assertTapped("Hill Giant", true);
    }

    @Test
    public void chaosUntapsOnlyPlanarControllersCreatures() {
        addPlane(playerA, Planes.PLANE_LLANOWAR);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears", 1, true);
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant", 1, true);
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertTapped("Grizzly Bears", false);
        assertTapped("Hill Giant", true);
    }

    @Test
    public void registryExposesLlanowarMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_LLANOWAR));

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Llanowar", metadata.getEnglishName());
        Assert.assertEquals("Plane - Llanowar", metadata.getImageName());
        Assert.assertEquals("PCA", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
