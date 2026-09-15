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

public class TheHippodromeTest extends CardTestPlayerBase {

    @Test
    public void allCreaturesGetMinusFiveMinusZero() {
        addPlane(playerA, Planes.PLANE_THE_HIPPODROME);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPowerToughness(playerA, "Grizzly Bears", -3, 2);
        assertPowerToughness(playerB, "Hill Giant", -2, 3);
    }

    @Test
    public void chaosDestroysCreatureWithNonpositivePower() {
        addPlane(playerA, Planes.PLANE_THE_HIPPODROME);
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");
        addChaosSpell();

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Hill Giant");
        setChoice(playerA, "Yes");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertGraveyardCount(playerB, "Hill Giant", 1);
    }

    @Test
    public void chaosChecksPowerAtResolution() {
        addPlane(playerA, Planes.PLANE_THE_HIPPODROME);
        addCard(Zone.BATTLEFIELD, playerB, "Scaled Wurm"); // 7/6, reduced to 2/6
        addChaosSpell();

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Scaled Wurm");
        setChoice(playerA, "Yes");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerB, "Scaled Wurm", 1);
        assertPowerToughness(playerB, "Scaled Wurm", 2, 6);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_THE_HIPPODROME)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("The Hippodrome", metadata.getEnglishName());
        Assert.assertEquals("Plane - The Hippodrome", metadata.getImageName());
        Assert.assertEquals("PCA", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }

    private void addChaosSpell() {
        SpellAbility ability = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        ability.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, ability, null, CardType.SORCERY);
    }
}
