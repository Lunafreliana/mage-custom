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
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class TheLuxFoundationLibraryTest extends CardTestPlayerBase {

    @Test
    public void playersHaveNoMaximumHandSize() {
        addPlane(playerA, Planes.PLANE_THE_LUX_FOUNDATION_LIBRARY);

        runCode("check maximum hand sizes", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> {
                    Assert.assertEquals(info, Integer.MAX_VALUE, playerA.getMaxHandSize());
                    Assert.assertEquals(info, Integer.MAX_VALUE, playerB.getMaxHandSize());
                });
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();
    }

    @Test
    public void controlledCreatureCombatDamageMayDraw() {
        removeAllCardsFromHand(playerA);
        addPlane(playerA, Planes.PLANE_THE_LUX_FOUNDATION_LIBRARY);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.LIBRARY, playerA, "Mountain");
        skipInitShuffling();

        attack(1, playerA, "Grizzly Bears");
        setChoice(playerA, true);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 18);
        assertHandCount(playerA, "Mountain", 1);
    }

    @Test
    public void chaosPutsShadowCounterOnAnyTargetCreature() {
        addPlane(playerA, Planes.PLANE_THE_LUX_FOUNDATION_LIBRARY);
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Hill Giant");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertCounterCount("Hill Giant", CounterType.SHADOW, 1);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_THE_LUX_FOUNDATION_LIBRARY)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("The Lux Foundation Library", metadata.getEnglishName());
        Assert.assertEquals("Plane - The Lux Foundation Library", metadata.getImageName());
        Assert.assertEquals("WHO", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
