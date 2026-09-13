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

public class ImmersturmTest extends CardTestPlayerBase {

    @Test
    public void enteringCreaturesControllerMayDealDamageEqualToItsPower() {
        addPlane(playerA, Planes.PLANE_IMMERSTURM);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);
        addCard(Zone.HAND, playerA, "Grizzly Bears");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Grizzly Bears");
        addTarget(playerA, playerB);
        setChoice(playerA, true);
        setStrictChooseMode(true);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 18);
    }

    @Test
    public void enteringCreaturesControllerMayDeclineDamage() {
        addPlane(playerA, Planes.PLANE_IMMERSTURM);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);
        addCard(Zone.HAND, playerA, "Grizzly Bears");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Grizzly Bears");
        addTarget(playerA, playerB);
        setChoice(playerA, false);
        setStrictChooseMode(true);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 20);
    }

    @Test
    public void chaosExilesAndReturnsCreatureUnderOwnersControl() {
        addPlane(playerA, Planes.PLANE_IMMERSTURM);
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Hill Giant");
        addTarget(playerB, playerA); // Immersturm trigger from the returned Hill Giant
        setChoice(playerB, false);
        setStrictChooseMode(true);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerB, "Hill Giant", 1);
        assertPermanentCount(playerA, "Hill Giant", 0);
    }

    @Test
    public void registryExposesImmersturmMetadata() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_IMMERSTURM);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Immersturm", metadata.getEnglishName());
        Assert.assertEquals("Plane - Immersturm", metadata.getImageName());
        Assert.assertEquals("PCA", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }
}
