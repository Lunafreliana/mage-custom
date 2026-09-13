package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.abilities.keyword.IslandwalkAbility;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.PlanarCardRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class BowieBaseOneTest extends CardTestPlayerBase {

    @Test
    public void endStepGoadsCreatureControlledByPlayerToLeft() {
        addPlane(playerA, Planes.PLANE_BOWIE_BASE_ONE);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");

        addTarget(playerA, "Hill Giant");

        setStrictChooseMode(true);
        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerA, 17);
    }

    @Test
    public void chaosGrantsIslandwalkUntilEndOfTurn() {
        addPlane(playerA, Planes.PLANE_BOWIE_BASE_ONE);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addChaosSpell();

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Grizzly Bears");
        checkAbility("islandwalk granted", 1, PhaseStep.POSTCOMBAT_MAIN,
                playerA, "Grizzly Bears", IslandwalkAbility.class, true);
        checkAbility("islandwalk expired", 2, PhaseStep.UPKEEP,
                playerA, "Grizzly Bears", IslandwalkAbility.class, false);

        setStrictChooseMode(true);
        setStopAt(2, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_BOWIE_BASE_ONE)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Bowie Base One", metadata.getEnglishName());
        Assert.assertEquals("Plane - Bowie Base One", metadata.getImageName());
        Assert.assertEquals("WHO", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }

    private void addChaosSpell() {
        SpellAbility ability = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        ability.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, ability, null, CardType.SORCERY);
    }
}
