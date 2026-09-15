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

public class OrochiColonyTest extends CardTestPlayerBase {

    @Test
    public void combatDamageMayFindBasicLandTapped() {
        addPlane(playerA, Planes.PLANE_OROCHI_COLONY);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.LIBRARY, playerA, "Forest");
        skipInitShuffling();

        attack(1, playerA, "Grizzly Bears");
        setChoice(playerA, true);
        addTarget(playerA, "Forest");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 18);
        assertPermanentCount(playerA, "Forest", 1);
        assertTapped("Forest", true);
    }

    @Test
    public void noncombatDamageDoesNotFindLand() {
        addPlane(playerA, Planes.PLANE_OROCHI_COLONY);
        addCard(Zone.BATTLEFIELD, playerA, "Prodigal Sorcerer");
        addCard(Zone.LIBRARY, playerA, "Forest");
        skipInitShuffling();

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}:", playerB);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 19);
        assertPermanentCount(playerA, "Forest", 0);
    }

    @Test
    public void chaosMakesTargetCreatureUnblockable() {
        addPlane(playerA, Planes.PLANE_OROCHI_COLONY);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Grizzly Bears");
        attack(1, playerA, "Grizzly Bears");
        block(1, playerB, "Hill Giant", "Grizzly Bears");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 18);
        assertPermanentCount(playerA, "Grizzly Bears", 1);
        assertPermanentCount(playerB, "Hill Giant", 1);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_OROCHI_COLONY)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Orochi Colony", metadata.getEnglishName());
        Assert.assertEquals("Plane - Orochi Colony", metadata.getImageName());
        Assert.assertEquals("MOC", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
