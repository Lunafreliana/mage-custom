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

public class WelcomeToValleyTest extends CardTestPlayerBase {

    @Test
    public void animalSpellDrawsButHumanSpellDoesNot() {
        addPlane(playerA, Planes.PLANE_WELCOME_TO_VALLEY);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 4);
        addCard(Zone.HAND, playerA, "Grizzly Bears");
        addCard(Zone.HAND, playerA, "Norwood Ranger");
        addCard(Zone.LIBRARY, playerA, "Mountain", 3);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Grizzly Bears");
        // The plane's draw trigger must resolve before another sorcery-speed creature can be cast.
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Norwood Ranger");
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, "Mountain", 1);
    }

    @Test
    public void chaosCreatesOneOneCopyOfControlledCreature() {
        addPlane(playerA, Planes.PLANE_WELCOME_TO_VALLEY);
        addCard(Zone.BATTLEFIELD, playerA, "Hill Giant");
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Hill Giant");
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Hill Giant", 2);
        assertPowerToughness(playerA, "Hill Giant", 1, 1);
    }

    @Test
    public void registryExposesMetadata() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_WELCOME_TO_VALLEY);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Welcome to Valley", metadata.getEnglishName());
        Assert.assertEquals("Plane - Welcome to Valley", metadata.getImageName());
        Assert.assertEquals("PUNK", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }
}
