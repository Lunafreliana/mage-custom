package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.PlanarDieRollResult;
import mage.constants.Planes;
import mage.constants.RollDieType;
import mage.constants.Zone;
import mage.game.command.Plane;
import mage.game.command.PlanarCard;
import mage.game.command.PlanarCardRegistry;
import mage.game.events.DieRolledEvent;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Arrays;

public class StairsToInfinityTest extends CardTestPlayerBase {

    @Test
    public void playersHaveNoMaximumHandSize() {
        addPlane(playerA, Planes.PLANE_STAIRS_TO_INFINITY);

        runCode("check maximum hand sizes", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> {
                    Assert.assertEquals(info, Integer.MAX_VALUE, playerA.getMaxHandSize());
                    Assert.assertEquals(info, Integer.MAX_VALUE, playerB.getMaxHandSize());
                });
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();
    }

    @Test
    public void planarDieRollDrawsCard() {
        addPlane(playerA, Planes.PLANE_STAIRS_TO_INFINITY);
        addCard(Zone.LIBRARY, playerA, "Mountain");
        skipInitShuffling();

        runCode("roll planar die", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> {
                    SpellAbility source = new SpellAbility(new ManaCostsImpl<>("{0}"), "test planar roll");
                    source.setControllerId(playerA.getId());
                    game.fireEvent(new DieRolledEvent(source, playerA.getId(), RollDieType.PLANAR,
                            6, 0, 0, PlanarDieRollResult.BLANK_ROLL));
                });
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, "Mountain", 1);
    }

    @Test
    public void chaosMayPutRevealedCardOnBottom() {
        addPlane(playerA, Planes.PLANE_STAIRS_TO_INFINITY);
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        runCode("install known planar deck", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> game.getState().getSharedPlanarDeck().setPlanes(Arrays.asList(
                        Plane.createPlane(Planes.PLANE_AGYREM), Plane.createPlane(Planes.PLANE_BANT)), false));
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        setChoice(playerA, true);
        runCode("check revealed card was bottomed", 1, PhaseStep.POSTCOMBAT_MAIN, playerA,
                (info, player, game) -> {
                    PlanarCard top = game.getState().getSharedPlanarDeck().draw();
                    Assert.assertNotNull(info, top);
                    Assert.assertEquals(info, "Plane - Bant", top.getName());
                });
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();
    }

    @Test
    public void registryExposesStairsToInfinityMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_STAIRS_TO_INFINITY));

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Stairs to Infinity", metadata.getEnglishName());
        Assert.assertEquals("Plane - Stairs to Infinity", metadata.getImageName());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
