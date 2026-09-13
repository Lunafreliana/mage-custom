package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.PlanarCardRegistry;
import mage.game.permanent.Permanent;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * Tests for Plane - Astral Arena.
 */
public class AstralArenaTest extends CardTestPlayerBase {

    @Test
    public void onlyOneCreatureCanAttackEachCombat() {
        addPlane(playerA, Planes.PLANE_ASTRAL_ARENA);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Glory Seeker");

        attack(1, playerA, "Grizzly Bears", playerB);
        runCode("second creature cannot attack", 1, PhaseStep.DECLARE_BLOCKERS, playerA,
                (info, player, game) -> {
                    Permanent secondCreature = getPermanent("Glory Seeker", playerA);
                    Assert.assertFalse(info, secondCreature.canAttack(playerB.getId(), game));
                });

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 18);
    }

    @Test
    public void onlyOneCreatureCanBlockEachCombat() {
        addPlane(playerA, Planes.PLANE_ASTRAL_ARENA);
        addCard(Zone.BATTLEFIELD, playerA, "Craw Wurm");
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Glory Seeker");

        attack(1, playerA, "Craw Wurm", playerB);
        block(1, playerB, "Grizzly Bears", "Craw Wurm");
        runCode("second creature cannot block", 1, PhaseStep.DECLARE_BLOCKERS, playerB,
                (info, player, game) -> {
                    Permanent secondCreature = getPermanent("Glory Seeker", playerB);
                    Assert.assertFalse(info,
                            game.getCombat().getGroups().get(0).canBlock(secondCreature, game));
                });

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertGraveyardCount(playerB, "Grizzly Bears", 1);
        assertPermanentCount(playerB, "Glory Seeker", 1);
    }

    @Test
    public void chaosDealsTwoDamageToEveryCreature() {
        addPlane(playerA, Planes.PLANE_ASTRAL_ARENA);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");

        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertGraveyardCount(playerA, "Grizzly Bears", 1);
        assertGraveyardCount(playerB, "Grizzly Bears", 1);
    }

    @Test
    public void isAvailableThroughPlanarCardRegistry() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_ASTRAL_ARENA);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Astral Arena", metadata.getEnglishName());
        Assert.assertEquals("PCA", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }
}
