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

public class WeHopeYouLikeSquirrelsTest extends CardTestPlayerBase {

    @Test
    public void eachPlayersUpkeepCreatesSquirrelForThatPlayer() {
        addPlane(playerA, Planes.PLANE_WE_HOPE_YOU_LIKE_SQUIRRELS);

        setStopAt(2, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Squirrel Token", 1);
        assertPermanentCount(playerB, "Squirrel Token", 1);
    }

    @Test
    public void castingPlayerCreatesSquirrel() {
        addPlane(playerA, Planes.PLANE_WE_HOPE_YOU_LIKE_SQUIRRELS);
        addCard(Zone.HAND, playerA, "Grizzly Bears");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Grizzly Bears");
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        // One from the upkeep and one from casting Grizzly Bears.
        assertPermanentCount(playerA, "Squirrel Token", 2);
        assertPermanentCount(playerB, "Squirrel Token", 0);
    }

    @Test
    public void chaosAddsCountersOnlyToPlanarControllersSquirrels() {
        addPlane(playerA, Planes.PLANE_WE_HOPE_YOU_LIKE_SQUIRRELS);
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerB, causeChaos, null, CardType.SORCERY);

        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, "Cause Chaos");
        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertCounterCount(playerA, "Squirrel Token", CounterType.P1P1, 0);
        assertCounterCount(playerB, "Squirrel Token", CounterType.P1P1, 1);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_WE_HOPE_YOU_LIKE_SQUIRRELS)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("We Hope You Like Squirrels", metadata.getEnglishName());
        Assert.assertEquals("Plane - We Hope You Like Squirrels", metadata.getImageName());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
