package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.game.command.PlanarCardRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.player.TestPlayer;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Collections;

public class TheGreatAerieTest extends CardTestPlayerBase {

    @Test
    public void planeswalkToAndUpkeepBothBolster() {
        addPlane(playerA, Planes.PLANE_THE_GREAT_AERIE);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");

        setChoice(playerA, "When you planeswalk"); // Order the planeswalk and upkeep triggers.
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertCounterCount(playerA, "Grizzly Bears", CounterType.P1P1, 6);
    }

    @Test
    public void eachPlanarControllersUpkeepBolsters() {
        useTheGreatAeriePlanechase();
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");

        setStrictChooseMode(true);
        setStopAt(2, PhaseStep.PRECOMBAT_MAIN);
        execute();

        // Starting-plane initialization is not a planeswalk, so player A bolsters only during their upkeep.
        assertCounterCount(playerA, "Grizzly Bears", CounterType.P1P1, 3);
        // Planar control follows the active player, so player B bolsters during their upkeep.
        assertCounterCount(playerB, "Hill Giant", CounterType.P1P1, 3);
    }

    @Test
    public void chaosCreaturesDealDamageEqualToToughness() {
        addPlane(playerA, Planes.PLANE_THE_GREAT_AERIE);
        addCard(Zone.BATTLEFIELD, playerA, "Wall of Frost"); // 0/7
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant"); // 3/3
        addChaosSpell();

        setChoice(playerA, "When you planeswalk"); // Order the initial planeswalk and upkeep triggers.
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Wall of Frost");
        addTarget(playerA, "Hill Giant");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Wall of Frost", 1);
        assertDamageReceived(playerA, "Wall of Frost", 3);
        assertGraveyardCount(playerB, "Hill Giant", 1);
    }

    @Test
    public void chaosMayChooseOnlyOneCreatureAndDealsNoDamage() {
        addPlane(playerA, Planes.PLANE_THE_GREAT_AERIE);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");
        addChaosSpell();

        setChoice(playerA, "When you planeswalk"); // Order the initial planeswalk and upkeep triggers.
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Grizzly Bears");
        addTarget(playerA, TestPlayer.TARGET_SKIP);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertDamageReceived(playerA, "Grizzly Bears", 0);
        assertDamageReceived(playerB, "Hill Giant", 0);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_THE_GREAT_AERIE)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("The Great Aerie", metadata.getEnglishName());
        Assert.assertEquals("Plane - The Great Aerie", metadata.getImageName());
        Assert.assertEquals("MOC", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }

    private void addChaosSpell() {
        SpellAbility ability = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        ability.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, ability, null, CardType.SORCERY);
    }

    private void useTheGreatAeriePlanechase() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Collections.singletonList(Planes.PLANE_THE_GREAT_AERIE);
    }
}
