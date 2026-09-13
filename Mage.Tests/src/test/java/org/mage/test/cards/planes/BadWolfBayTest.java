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

public class BadWolfBayTest extends CardTestPlayerBase {

    @Test
    public void beginningOfCombatExilesCreatureUntilEndStep() {
        addPlane(playerA, Planes.PLANE_BAD_WOLF_BAY);
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");

        addTarget(playerA, "Grizzly Bears");
        checkExileCount("creature is exiled during combat", 1, PhaseStep.DECLARE_ATTACKERS,
                playerB, "Grizzly Bears", 1);

        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        assertPermanentCount(playerB, "Grizzly Bears", 1);
        assertExileCount(playerB, "Grizzly Bears", 0);
    }

    @Test
    public void chaosStopsCardsReturningFromExileThatTurn() {
        addPlane(playerA, Planes.PLANE_BAD_WOLF_BAY);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        addTarget(playerA, "Grizzly Bears");
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Cause Chaos");

        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        assertExileCount(playerA, "Grizzly Bears", 1);
        assertPermanentCount(playerA, "Grizzly Bears", 0);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_BAD_WOLF_BAY)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Bad Wolf Bay", metadata.getEnglishName());
        Assert.assertEquals("Plane - Bad Wolf Bay", metadata.getImageName());
        Assert.assertEquals("WHO", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
