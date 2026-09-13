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

public class CityOfTheDaleksTest extends CardTestPlayerBase {

    @Test
    public void attackingOpponentLosesLifeForControlledArtifacts() {
        addPlane(playerA, Planes.PLANE_CITY_OF_THE_DALEKS);
        addCard(Zone.BATTLEFIELD, playerA, "Memnite", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");

        attack(1, playerA, "Grizzly Bears", playerB);
        addTarget(playerA, playerB);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        // Grizzly Bears deals 2 combat damage in addition to the 2 life lost to the trigger.
        assertLife(playerB, 16);
    }

    @Test
    public void chaosCreatesHastyDalekThatMustAttackAndIsSacrificed() {
        addPlane(playerA, Planes.PLANE_CITY_OF_THE_DALEKS);
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        attack(1, playerA, "Dalek Token", playerB);
        addTarget(playerA, playerB); // City's attack trigger.

        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        // Three combat damage plus one life from City's attack trigger (the Dalek is an artifact).
        assertLife(playerB, 16);
        assertPermanentCount(playerA, "Dalek Token", 0);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_CITY_OF_THE_DALEKS)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("City of the Daleks", metadata.getEnglishName());
        Assert.assertEquals("Plane - City of the Daleks", metadata.getImageName());
        Assert.assertEquals("WHO", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
