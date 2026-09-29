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

/** Focused behavior tests for New Argive. */
public class NewArgiveTest extends CardTestPlayerBase {

    @Test
    public void boostsEachAttackingHistoricCreatureOnly() {
        addPlane(playerA, Planes.PLANE_NEW_ARGIVE);
        addCard(Zone.BATTLEFIELD, playerA, "Memnite");
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Memnite");

        attack(1, playerA, "Memnite", playerB);
        attack(1, playerA, "Grizzly Bears", playerB);
        attack(2, playerB, "Memnite", playerA);

        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 15);
        assertLife(playerA, 17);
    }

    @Test
    public void chaosFindsHistoricCardForPlanarController() {
        addPlane(playerA, Planes.PLANE_NEW_ARGIVE);
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears");
        addCard(Zone.LIBRARY, playerA, "Memnite");
        addCard(Zone.LIBRARY, playerA, "Island");
        skipInitShuffling();

        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, "Memnite", 1);
        assertLibraryCount(playerA, 2);
        assertGraveyardCount(playerA, 0);
    }

    @Test
    public void registryExposesNewArgiveMetadata() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_NEW_ARGIVE);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals("New Argive", metadata.getEnglishName());
        Assert.assertEquals("Plane - New Argive", metadata.getImageName());
        Assert.assertEquals("MOC", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }
}
