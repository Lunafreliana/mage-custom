package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.abilities.effects.common.RollDiceEffect;
import mage.abilities.effects.common.RollPlanarDieEffect;
import mage.abilities.keyword.FlyingAbility;
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

public class TenWizardsMountainTest extends CardTestPlayerBase {

    @Test
    public void planarDieRollAddsCounterToUpToOneCreature() {
        addPlane(playerA, Planes.PLANE_TEN_WIZARDS_MOUNTAIN);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");
        addRollSpell(playerA, new RollPlanarDieEffect(), "Roll Planar Die");

        setDieRollResult(playerA, 4); // blank result
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Roll Planar Die");
        addTarget(playerA, "Hill Giant");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertCounterCount(playerA, "Grizzly Bears", CounterType.P1P1, 0);
        assertCounterCount(playerB, "Hill Giant", CounterType.P1P1, 1);
    }

    @Test
    public void numericalDieRollDoesNotTrigger() {
        addPlane(playerA, Planes.PLANE_TEN_WIZARDS_MOUNTAIN);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addRollSpell(playerA, new RollDiceEffect(6), "Roll Numerical Die");

        setDieRollResult(playerA, 4);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Roll Numerical Die");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertCounterCount(playerA, "Grizzly Bears", CounterType.P1P1, 0);
    }

    @Test
    public void chaosGrantsFlyingOnlyToPlanarControllersCreaturesUntilEndOfTurn() {
        addPlane(playerA, Planes.PLANE_TEN_WIZARDS_MOUNTAIN);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");
        addRollSpell(playerA, new ChaosEnsuesEffect(), "Cause Chaos");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        checkAbility("controller's creature gains flying", 1, PhaseStep.POSTCOMBAT_MAIN,
                playerA, "Grizzly Bears", FlyingAbility.class, true);
        checkAbility("opponent's creature does not gain flying", 1, PhaseStep.POSTCOMBAT_MAIN,
                playerB, "Hill Giant", FlyingAbility.class, false);
        checkAbility("flying expires", 2, PhaseStep.UPKEEP,
                playerA, "Grizzly Bears", FlyingAbility.class, false);

        setStopAt(2, PhaseStep.UPKEEP);
        execute();
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_TEN_WIZARDS_MOUNTAIN)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Ten Wizards Mountain", metadata.getEnglishName());
        Assert.assertEquals("Plane - Ten Wizards Mountain", metadata.getImageName());
        Assert.assertEquals("MOC", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }

    private void addRollSpell(TestPlayer player, Effect effect, String name) {
        SpellAbility ability = new SpellAbility(new ManaCostsImpl<>("{0}"), name);
        ability.addEffect(effect);
        addCustomCardWithSpell(player, ability, null, CardType.SORCERY);
    }
}
