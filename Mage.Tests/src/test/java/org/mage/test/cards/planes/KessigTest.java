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

public class KessigTest extends CardTestPlayerBase {

    @Test
    public void preventsCombatDamageFromNonWerewolves() {
        addPlane(playerA, Planes.PLANE_KESSIG);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Kessig Wolf");

        attack(1, playerA, "Grizzly Bears", playerB);
        attack(1, playerA, "Kessig Wolf", playerB);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 17);
    }

    @Test
    public void chaosBoostsAndChangesOnlyControllersCreaturesUntilEndOfTurn() {
        addPlane(playerA, Planes.PLANE_KESSIG);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        runCode("check Kessig chaos effect", 1, PhaseStep.POSTCOMBAT_MAIN,
                playerA, (info, player, game) -> {
                    assertPowerToughness(playerA, "Grizzly Bears", 4, 4);
                    assertAbility(playerA, "Grizzly Bears", TrampleAbility.getInstance(), true);
                    Assert.assertTrue(getPermanent("Grizzly Bears", playerA)
                            .hasSubtype(SubType.WEREWOLF, game));
                    assertPowerToughness(playerB, "Hill Giant", 3, 3);
                    assertAbility(playerB, "Hill Giant", TrampleAbility.getInstance(), false);
                    Assert.assertFalse(getPermanent("Hill Giant", playerB)
                            .hasSubtype(SubType.WEREWOLF, game));
                });

        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        assertPowerToughness(playerA, "Grizzly Bears", 2, 2);
        assertAbility(playerA, "Grizzly Bears", TrampleAbility.getInstance(), false);
        Assert.assertFalse(getPermanent("Grizzly Bears", playerA)
                .hasSubtype(SubType.WEREWOLF, currentGame));
    }

    @Test
    public void registryExposesKessigMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_KESSIG)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Kessig", metadata.getEnglishName());
        Assert.assertEquals("Plane - Kessig", metadata.getImageName());
        Assert.assertEquals("PCA", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
