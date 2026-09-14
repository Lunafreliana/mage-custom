package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.constants.CardType;
import mage.constants.EmptyNames;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.command.PlanarCardRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class MondassianColonyShipTest extends CardTestPlayerBase {

    @Test
    public void attackingCreatureCountsItsControllersOtherCreatures() {
        addPlane(playerA, Planes.PLANE_MONDASSIAN_COLONY_SHIP);
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Balduvian Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Runeclaw Bear");

        attack(2, playerB, "Grizzly Bears");

        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPowerToughness(playerB, "Grizzly Bears", 3, 3);
        assertPowerToughness(playerB, "Balduvian Bears", 2, 2);
        assertPowerToughness(playerA, "Runeclaw Bear", 2, 2);
    }

    @Test
    public void chaosTurnsTargetIntoCyberman() {
        addPlane(playerA, Planes.PLANE_MONDASSIAN_COLONY_SHIP);
        addCard(Zone.BATTLEFIELD, playerB, "Serra Angel");
        addCustomCardWithSpell(playerA, createCauseChaosAbility(), null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Serra Angel");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        String faceDown = EmptyNames.FACE_DOWN_CREATURE.getTestCommand();
        assertPermanentCount(playerB, faceDown, 1);
        assertPowerToughness(playerB, faceDown, 2, 2);
        assertType(faceDown, CardType.ARTIFACT, true);
        assertType(faceDown, CardType.CREATURE, true);
        assertSubtype(faceDown, SubType.CYBERMAN);
        assertNotSubtype(faceDown, SubType.ANGEL);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_MONDASSIAN_COLONY_SHIP)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Mondassian Colony Ship", metadata.getEnglishName());
        Assert.assertEquals("Plane - Mondassian Colony Ship", metadata.getImageName());
        Assert.assertEquals("WHO", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }

    private static SpellAbility createCauseChaosAbility() {
        SpellAbility ability = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        ability.addEffect(new ChaosEnsuesEffect());
        return ability;
    }
}
