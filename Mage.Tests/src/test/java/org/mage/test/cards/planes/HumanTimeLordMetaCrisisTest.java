package org.mage.test.cards.planes;

import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Phenomena;
import mage.constants.Planes;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.game.command.PlanarCardRegistry;
import mage.game.command.phenomena.HumanTimeLordMetaCrisisPhenomenon;
import mage.game.permanent.Permanent;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Collections;

public class HumanTimeLordMetaCrisisTest extends CardTestPlayerBase {

    @Test
    public void eachPlayerCopiesFirstChoiceAndUsesSecondChoicesPower() {
        prepareStartedPlanechaseGame();
        addCard(Zone.BATTLEFIELD, playerA, "Isamaru, Hound of Konda");
        addCard(Zone.BATTLEFIELD, playerA, "Hill Giant");
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");

        setChoice(playerA, "Isamaru, Hound of Konda^Hill Giant");
        setChoice(playerB, "Grizzly Bears");
        runCode("encounter Human—Time Lord Meta-Crisis", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> {
                    Assert.assertTrue(info, game.addPhenomenon(
                            new HumanTimeLordMetaCrisisPhenomenon(), player.getId()
                    ));
                    game.checkStateAndTriggered();
                    game.getStack().resolve(game);
                    game.checkStateAndTriggered();
                });
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        Permanent isamaruToken = currentGame.getBattlefield().getAllActivePermanents(playerA.getId()).stream()
                .filter(permanent -> permanent.isToken() && permanent.getName().equals("Isamaru, Hound of Konda"))
                .findFirst()
                .orElse(null);
        Assert.assertNotNull(isamaruToken);
        Assert.assertFalse(isamaruToken.getSuperType().contains(SuperType.LEGENDARY));
        Assert.assertEquals(3, isamaruToken.getCounters(currentGame).getCount(CounterType.P1P1));

        Permanent bearsToken = currentGame.getBattlefield().getAllActivePermanents(playerB.getId()).stream()
                .filter(permanent -> permanent.isToken() && permanent.getName().equals("Grizzly Bears"))
                .findFirst()
                .orElse(null);
        Assert.assertNotNull(bearsToken);
        Assert.assertEquals(0, bearsToken.getCounters(currentGame).getCount(CounterType.P1P1));
    }

    @Test
    public void registryExposesDoctorWhoMetadata() {
        String id = PlanarCardRegistry.getId(Phenomena.HUMAN_TIME_LORD_META_CRISIS);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals(CardType.PHENOMENON, metadata.getType());
        Assert.assertEquals("Human—Time Lord Meta-Crisis", metadata.getEnglishName());
        Assert.assertEquals("Phenomenon - Human—Time Lord Meta-Crisis", metadata.getImageName());
        Assert.assertEquals("WHO", metadata.getSetCode());
        Assert.assertTrue(PlanarCardRegistry.create(id) instanceof HumanTimeLordMetaCrisisPhenomenon);
    }

    private void prepareStartedPlanechaseGame() {
        addCard(Zone.LIBRARY, playerA, "Mountain", 20);
        addCard(Zone.LIBRARY, playerB, "Mountain", 20);
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Collections.singletonList(Planes.PLANE_FIELDS_OF_SUMMER);
    }
}
