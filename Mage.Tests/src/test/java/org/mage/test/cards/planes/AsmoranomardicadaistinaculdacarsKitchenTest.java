package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.abilities.effects.common.GainLifeEffect;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.Plane;
import mage.game.command.PlanarCardRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Collections;

public class AsmoranomardicadaistinaculdacarsKitchenTest extends CardTestPlayerBase {

    @Test
    public void creatureEnteringUnderPlanarControllersControlCreatesFood() {
        addPlane(playerA, Planes.PLANE_ASMORANOMARDICADAISTINACULDACARS_KITCHEN);
        addCard(Zone.HAND, playerA, "Memnite");
        addCard(Zone.HAND, playerB, "Memnite");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Memnite");
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerB, "Memnite");

        setStopAt(2, PhaseStep.END_TURN);
        execute();

        assertPermanentCount(playerA, "Food Token", 1);
        assertPermanentCount(playerB, "Food Token", 0);
    }

    @Test
    public void gainingLifeMakesTargetOpponentLoseSameAmount() {
        addPlane(playerA, Planes.PLANE_ASMORANOMARDICADAISTINACULDACARS_KITCHEN);
        SpellAbility gainLife = new SpellAbility(new ManaCostsImpl<>("{0}"), "Gain life");
        gainLife.addEffect(new GainLifeEffect(4));
        addCustomCardWithSpell(playerA, gainLife, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Gain life");
        addTarget(playerA, playerB);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerA, 24);
        assertLife(playerB, 16);
    }

    @Test
    public void chaosSacrificesArtifactsAndCreaturesThenDrawsAndLosesLife() {
        addPlane(playerA, Planes.PLANE_ASMORANOMARDICADAISTINACULDACARS_KITCHEN);
        addCard(Zone.BATTLEFIELD, playerA, "Memnite");
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.LIBRARY, playerA, "Island", 2);
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Memnite^Grizzly Bears");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Memnite", 0);
        assertPermanentCount(playerA, "Grizzly Bears", 0);
        assertGraveyardCount(playerA, "Memnite", 1);
        assertGraveyardCount(playerA, "Grizzly Bears", 1);
        assertHandCount(playerA, 2);
        assertLife(playerA, 18);
    }

    @Test
    public void planeswalkingHereFromSharedDeckCreatesFood() {
        addPlane(playerA, Planes.PLANE_FIELDS_OF_SUMMER);

        runCode("put Kitchen on top and planeswalk", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> {
                    Plane kitchen = Plane.createPlane(
                            Planes.PLANE_ASMORANOMARDICADAISTINACULDACARS_KITCHEN
                    );
                    game.getState().getSharedPlanarDeck().setPlanes(
                            Collections.singletonList(kitchen), false
                    );
                    Assert.assertTrue(info, game.planeswalk(player.getId()));
                });

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Food Token", 1);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(
                        Planes.PLANE_ASMORANOMARDICADAISTINACULDACARS_KITCHEN
                )
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Asmoranomardicadaistinaculdacar's Kitchen", metadata.getEnglishName());
        Assert.assertEquals(
                "Plane - Asmoranomardicadaistinaculdacar's Kitchen", metadata.getImageName()
        );
        Assert.assertEquals("PUNK", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
