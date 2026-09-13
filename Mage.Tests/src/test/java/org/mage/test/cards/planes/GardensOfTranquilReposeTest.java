package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.abilities.effects.common.DestroyTargetEffect;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.PlanarCardRegistry;
import mage.target.common.TargetCreaturePermanent;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.player.TestPlayer;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class GardensOfTranquilReposeTest extends CardTestPlayerBase {

    @Test
    public void deadCreatureIsExiledAndItsControllerScries() {
        addPlane(playerA, Planes.PLANE_GARDENS_OF_TRANQUIL_REPOSE);
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
        addCard(Zone.LIBRARY, playerB, "Island");

        SpellAbility destroy = new SpellAbility(new ManaCostsImpl<>("{0}"), "Destroy creature");
        destroy.addEffect(new DestroyTargetEffect());
        destroy.addTarget(new TargetCreaturePermanent());
        addCustomCardWithSpell(playerA, destroy, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Destroy creature", "Grizzly Bears");
        addTarget(playerB, TestPlayer.TARGET_SKIP);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertExileCount(playerB, "Grizzly Bears", 1);
        assertGraveyardCount(playerB, "Grizzly Bears", 0);
    }

    @Test
    public void chaosCreatesOneDalekPlusOneForEachLinkedExiledCard() {
        addPlane(playerA, Planes.PLANE_GARDENS_OF_TRANQUIL_REPOSE);
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");

        SpellAbility destroy = new SpellAbility(new ManaCostsImpl<>("{0}"), "Destroy creature");
        destroy.addEffect(new DestroyTargetEffect());
        destroy.addTarget(new TargetCreaturePermanent());
        addCustomCardWithSpell(playerA, destroy, null, CardType.SORCERY);

        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Destroy creature", "Grizzly Bears");
        addTarget(playerB, TestPlayer.TARGET_SKIP);
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Cause Chaos");

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertTokenCount(playerA, "Dalek Token", 2);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_GARDENS_OF_TRANQUIL_REPOSE)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Gardens of Tranquil Repose", metadata.getEnglishName());
        Assert.assertEquals("Plane - Gardens of Tranquil Repose", metadata.getImageName());
        Assert.assertEquals("WHO", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
