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

/** Focused behavior tests for Minamo. */
public class MinamoTest extends CardTestPlayerBase {

    @Test
    public void spellCasterMayDraw() {
        addPlane(playerA, Planes.PLANE_MINAMO);
        addCard(Zone.HAND, playerB, "Memnite");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerB, "Memnite");
        setChoice(playerB, true);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, 0);
        assertHandCount(playerB, 1);
    }

    @Test
    public void chaosLetsEachPlayerReturnOneBlueCard() {
        addPlane(playerA, Planes.PLANE_MINAMO);
        addCard(Zone.GRAVEYARD, playerA, "Merfolk of the Pearl Trident");
        addCard(Zone.GRAVEYARD, playerA, "Grizzly Bears");
        addCard(Zone.GRAVEYARD, playerB, "Coral Merfolk");
        addCard(Zone.GRAVEYARD, playerB, "Hill Giant");

        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Merfolk of the Pearl Trident");
        addTarget(playerB, "Coral Merfolk");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, "Merfolk of the Pearl Trident", 1);
        assertHandCount(playerB, "Coral Merfolk", 1);
        assertGraveyardCount(playerA, "Grizzly Bears", 1);
        assertGraveyardCount(playerB, "Hill Giant", 1);
    }

    @Test
    public void registryExposesMinamoMetadataAndFactory() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_MINAMO);

        Assert.assertEquals("Minamo", PlanarCardRegistry.getMetadata(id).getEnglishName());
        Assert.assertEquals("PCA", PlanarCardRegistry.getMetadata(id).getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }
}
