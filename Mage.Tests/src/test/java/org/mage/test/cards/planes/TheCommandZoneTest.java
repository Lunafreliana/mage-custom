package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.PlanarCardRegistry;
import mage.watchers.common.CommanderPlaysCountWatcher;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestCommanderDuelBase;

import java.util.UUID;

import static mage.constants.CommanderCardType.ANY;

public class TheCommandZoneTest extends CardTestCommanderDuelBase {

    @Test
    public void eachPlayerWithoutCommanderMayPutItOntoBattlefield() {
        addPlane(playerA, Planes.PLANE_THE_COMMAND_ZONE);
        addCard(Zone.COMMAND, playerA, "Balduvian Bears");
        addCard(Zone.COMMAND, playerB, "Memnite");

        addTarget(playerA, "Balduvian Bears");
        addTarget(playerB, "Memnite");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertCommandZoneCount(playerA, "Balduvian Bears", 0);
        assertPermanentCount(playerA, "Balduvian Bears", 1);
        assertCommandZoneCount(playerB, "Memnite", 0);
        assertPermanentCount(playerB, "Memnite", 1);
    }

    @Test
    public void commanderAbilityTriggersTwiceDuringPlanarControllersTurn() {
        addPlane(playerA, Planes.PLANE_THE_COMMAND_ZONE);
        addCard(Zone.COMMAND, playerA, "Soul Warden");
        addCard(Zone.HAND, playerA, "Memnite");
        addCard(Zone.BATTLEFIELD, playerA, "Plains");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Soul Warden");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Memnite");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerA, 42);
    }

    @Test
    public void chaosResetsCommanderCastCount() {
        addPlane(playerA, Planes.PLANE_THE_COMMAND_ZONE);
        addCard(Zone.COMMAND, playerA, "Balduvian Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);

        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Balduvian Bears");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN, playerA);
        runCode("commander was cast once", 1, PhaseStep.BEGIN_COMBAT, playerA, (info, player, game) -> {
            UUID commanderId = game.getCommandersIds(player, ANY, false)
                    .stream()
                    .filter(id -> game.getCard(id) != null
                            && game.getCard(id).getName().equals("Balduvian Bears"))
                    .findFirst().orElse(null);
            CommanderPlaysCountWatcher watcher = game.getState().getWatcher(CommanderPlaysCountWatcher.class);
            Assert.assertNotNull(watcher);
            Assert.assertNotNull(commanderId);
            Assert.assertEquals(1, watcher.getPlaysCount(commanderId));
            Assert.assertEquals(1, watcher.getPlayerCount(player.getId()));
        });
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Cause Chaos");
        waitStackResolved(1, PhaseStep.POSTCOMBAT_MAIN);
        runCode("commander tax reset", 1, PhaseStep.END_TURN, playerA, (info, player, game) -> {
            UUID commanderId = game.getCommandersIds(player, ANY, false)
                    .stream()
                    .filter(id -> game.getCard(id) != null
                            && game.getCard(id).getName().equals("Balduvian Bears"))
                    .findFirst().orElse(null);
            CommanderPlaysCountWatcher watcher = game.getState().getWatcher(CommanderPlaysCountWatcher.class);
            Assert.assertNotNull(watcher);
            Assert.assertNotNull(commanderId);
            Assert.assertEquals(0, watcher.getPlaysCount(commanderId));
            Assert.assertEquals(0, watcher.getPlayerCount(player.getId()));
        });

        setStopAt(1, PhaseStep.END_TURN);
        execute();
    }

    @Test
    public void registryExposesTheCommandZoneMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_THE_COMMAND_ZONE)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("The Command Zone", metadata.getEnglishName());
        Assert.assertEquals("Plane - The Command Zone", metadata.getImageName());
        Assert.assertEquals("PUNK", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
