package org.mage.test.cards.planes;

import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.PlanarCardRegistry;
import mage.game.events.GameEvent;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class NyxTest extends CardTestPlayerBase {

    @Test
    public void nontokenCreaturesBecomeEnchantmentsAndTriggerConstellation() {
        addPlane(playerA, Planes.PLANE_NYX);
        addCard(Zone.HAND, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Grizzly Bears");
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertType("Grizzly Bears", CardType.CREATURE, true);
        assertType("Grizzly Bears", CardType.ENCHANTMENT, true);
        assertLife(playerA, 21);
    }

    @Test
    public void tokenCreaturesDoNotBecomeEnchantmentsOrTriggerConstellation() {
        addPlane(playerA, Planes.PLANE_NYX);
        addCard(Zone.HAND, playerA, "Raise the Alarm");
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Raise the Alarm");
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertType("Soldier Token", CardType.CREATURE, true);
        assertType("Soldier Token", CardType.ENCHANTMENT, false);
        assertLife(playerA, 20);
    }

    @Test
    public void chaosAddsManaEqualToChosenColorDevotion() {
        addPlane(playerA, Planes.PLANE_NYX);
        addCard(Zone.BATTLEFIELD, playerA, "Leatherback Baloth");

        runCode("resolve Nyx chaos trigger", 1, PhaseStep.PRECOMBAT_MAIN,
                playerA, (info, player, game) -> {
                    game.fireEvent(new GameEvent(
                            GameEvent.EventType.CHAOS_ENSUES,
                            game.getState().getFaceUpPlanes().get(0).getId(),
                            null,
                            player.getId()
                    ));
                    game.checkStateAndTriggered();
                    game.getStack().resolve(game);
                    Assert.assertEquals(info, 3, player.getManaPool().getGreen());
                });
        setChoice(playerA, "Green");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_NYX)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Nyx", metadata.getEnglishName());
        Assert.assertEquals("Plane - Nyx", metadata.getImageName());
        Assert.assertEquals("MOC", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
