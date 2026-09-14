package org.mage.test.cards.planes;

import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.PlanarCardRegistry;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class CelestineReefTest extends CardTestPlayerBase {

    @Test
    public void creaturesNeedFlyingOrIslandwalkToAttack() {
        addPlane(playerA, Planes.PLANE_CELESTINE_REEF);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Wind Drake");
        addCard(Zone.BATTLEFIELD, playerA, "Merfolk Spy");

        runCode("check attack restrictions", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> {
                    Permanent bears = getPermanent("Grizzly Bears", playerA);
                    Permanent drake = getPermanent("Wind Drake", playerA);
                    Permanent spy = getPermanent("Merfolk Spy", playerA);
                    Assert.assertFalse(info + " -- vanilla creature", bears.canAttack(playerB.getId(), game));
                    Assert.assertTrue(info + " -- creature with flying", drake.canAttack(playerB.getId(), game));
                    Assert.assertTrue(info + " -- creature with islandwalk", spy.canAttack(playerB.getId(), game));
                });

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }

    @Test
    public void chaosPreventsLossAndOpponentWinUntilPlaneswalk() {
        addPlane(playerA, Planes.PLANE_CELESTINE_REEF);

        runCode("resolve chaos and test win-loss replacement", 1, PhaseStep.UPKEEP, playerA,
                (info, player, game) -> {
                    game.fireEvent(new GameEvent(
                            GameEvent.EventType.CHAOS_ENSUES,
                            game.getState().getFaceUpPlanes().get(0).getId(), null, playerA.getId()
                    ));
                    game.checkStateAndTriggered();
                    game.getStack().resolve(game);

                    Assert.assertTrue(info + " -- controller can't lose", game.replaceEvent(new GameEvent(
                            GameEvent.EventType.LOSES, null, null, playerA.getId()
                    )));
                    Assert.assertTrue(info + " -- opponent can't win", game.replaceEvent(new GameEvent(
                            GameEvent.EventType.WINS, null, null, playerB.getId()
                    )));
                    Assert.assertFalse(info + " -- opponent can still lose", game.replaceEvent(new GameEvent(
                            GameEvent.EventType.LOSES, null, null, playerB.getId()
                    )));

                    game.fireEvent(new GameEvent(
                            GameEvent.EventType.PLANESWALKED,
                            game.getState().getFaceUpPlanes().get(0).getId(), null, playerA.getId()
                    ));

                    Assert.assertFalse(info + " -- effect ended after planeswalking",
                            game.replaceEvent(new GameEvent(
                                    GameEvent.EventType.LOSES, null, null, playerA.getId()
                            )));
                });

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }

    @Test
    public void registryExposesMetadata() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_CELESTINE_REEF);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Celestine Reef", metadata.getEnglishName());
        Assert.assertEquals("Plane - Celestine Reef", metadata.getImageName());
        Assert.assertEquals("PCA", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }
}
