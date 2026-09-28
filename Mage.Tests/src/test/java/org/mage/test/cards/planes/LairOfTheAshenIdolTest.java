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

import java.util.Arrays;

public class LairOfTheAshenIdolTest extends CardTestPlayerBase {

    @Test
    public void upkeepSacrificesCreatureInsteadOfPlaneswalking() {
        useLairAsStartingPlane();
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");

        setChoice(playerA, "Grizzly Bears");
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertGraveyardCount(playerA, "Grizzly Bears", 1);
        Assert.assertEquals("Plane - Lair of the Ashen Idol",
                currentGame.getState().getFaceUpPlanes().get(0).getName());
    }

    @Test
    public void upkeepPlaneswalksWhenNoCreatureCanBeSacrificed() {
        useLairAsStartingPlane();

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        Assert.assertEquals("Plane - Akoum", currentGame.getState().getFaceUpPlanes().get(0).getName());
    }

    @Test
    public void chaosCreatesZombieForEachTargetedPlayer() {
        addPlane(playerA, Planes.PLANE_LAIR_OF_THE_ASHEN_IDOL);
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, playerA);
        addTarget(playerA, playerB);
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Zombie Token", 1);
        assertPermanentCount(playerB, "Zombie Token", 1);
    }

    @Test
    public void registryExposesLairMetadata() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_LAIR_OF_THE_ASHEN_IDOL);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Lair of the Ashen Idol", metadata.getEnglishName());
        Assert.assertEquals("Plane - Lair of the Ashen Idol", metadata.getImageName());
        Assert.assertEquals("PCA", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }

    private void useLairAsStartingPlane() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(
                Planes.PLANE_LAIR_OF_THE_ASHEN_IDOL,
                Planes.PLANE_AKOUM
        );
        skipInitShuffling();
    }
}
