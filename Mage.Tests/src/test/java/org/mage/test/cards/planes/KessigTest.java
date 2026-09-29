package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.abilities.keyword.TrampleAbility;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.command.PlanarCardRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/** Focused behavior tests for Kessig. */
public class KessigTest extends CardTestPlayerBase {

    @Test
    public void preventsCombatDamageFromNonWerewolvesOnly() {
        addPlane(playerA, Planes.PLANE_KESSIG);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Woodland Changeling");

        attack(1, playerA, "Grizzly Bears", playerB);
        attack(1, playerA, "Woodland Changeling", playerB);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 18);
    }

    @Test
    public void chaosAffectsOnlyCreaturesControlledAsItResolves() {
        addPlane(playerA, Planes.PLANE_KESSIG);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 2);
        addCard(Zone.HAND, playerA, "Raise the Alarm");
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Raise the Alarm");
        runCode("verify chaos bonuses", 1, PhaseStep.POSTCOMBAT_MAIN, playerA, (info, player, game) -> {
            assertPowerToughness(playerA, "Grizzly Bears", 4, 4);
            assertAbility(playerA, "Grizzly Bears", TrampleAbility.getInstance(), true);
            Assert.assertTrue(info, game.getBattlefield().getAllActivePermanents(player.getId()).stream()
                    .filter(permanent -> permanent.getName().equals("Grizzly Bears"))
                    .allMatch(permanent -> permanent.hasSubtype(SubType.WEREWOLF, game)));
            assertPowerToughness(playerA, "Soldier Token", 1, 1);
            Assert.assertTrue(info, game.getBattlefield().getAllActivePermanents(player.getId()).stream()
                    .filter(permanent -> permanent.getName().equals("Soldier Token"))
                    .noneMatch(permanent -> permanent.hasSubtype(SubType.WEREWOLF, game)));
            assertPowerToughness(playerB, "Hill Giant", 3, 3);
        });

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();
    }

    @Test
    public void registryExposesKessigMetadata() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_KESSIG);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Kessig", metadata.getEnglishName());
        Assert.assertEquals("Plane - Kessig", metadata.getImageName());
        Assert.assertEquals("PCA", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }
}
