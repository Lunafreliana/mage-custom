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

public class GrixisTest extends CardTestPlayerBase {

    @Test
    public void coloredCreatureInControllerGraveyardHasUnearthForItsManaCost() {
        addPlane(playerA, Planes.PLANE_GRIXIS);
        addCard(Zone.GRAVEYARD, playerA, "Air Elemental"); // {3}{U}{U}
        addCard(Zone.BATTLEFIELD, playerA, "Island", 5);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Unearth {3}{U}{U}");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Air Elemental", 1);
        assertGraveyardCount(playerA, "Air Elemental", 0);
    }

    @Test
    public void colorlessCreatureDoesNotGainUnearth() {
        addPlane(playerA, Planes.PLANE_GRIXIS);
        addCard(Zone.GRAVEYARD, playerA, "Ornithopter");

        checkPlayableAbility("colorless card", 1, PhaseStep.PRECOMBAT_MAIN,
                playerA, "Unearth", false);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();
    }

    @Test
    public void chaosReanimatesFromAnyGraveyardUnderPlanarControllersControl() {
        addPlane(playerA, Planes.PLANE_GRIXIS);
        addCard(Zone.GRAVEYARD, playerB, "Hill Giant");
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Hill Giant");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Hill Giant", 1);
        assertGraveyardCount(playerB, "Hill Giant", 0);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_GRIXIS)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Grixis", metadata.getEnglishName());
        Assert.assertEquals("Plane - Grixis", metadata.getImageName());
        Assert.assertEquals("PCA", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
