package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.constants.CardType;
import mage.constants.CommanderCardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.Plane;
import mage.watchers.common.CommanderPlaysCountWatcher;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestCommanderDuelBase;

import java.util.Collections;
import java.util.UUID;

public class TheCommandZoneTest extends CardTestCommanderDuelBase {

    public TheCommandZoneTest() {
        setDecknamePlayerA("CommanderDuel_UW.dck");
        setDecknamePlayerB("CommanderDuel_UW.dck");
    }

    @Test
    public void testPlaneswalkAbilityCanPutCommanderOntoBattlefield() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Collections.singletonList(Planes.PLANE_FIELDS_OF_SUMMER);

        runCode("planeswalk to The Command Zone", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> Assert.assertTrue(info, game.addPlane(
                        Plane.createPlane(Planes.PLANE_THE_COMMAND_ZONE), playerA.getId()
                )));

        setChoice(playerB, true);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertCommandZoneCount(playerA, "Daxos of Meletis", 1);
        assertPermanentCount(playerB, "Daxos of Meletis", 1);
    }

    @Test
    public void testPlaneswalkAbilityDrawsForPlayerControllingCommander() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Collections.singletonList(Planes.PLANE_FIELDS_OF_SUMMER);
        removeAllCardsFromHand(playerA);

        addCard(Zone.BATTLEFIELD, playerA, "Plains", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 1);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Daxos of Meletis");
        setChoice(playerA, false); // Don't gain life from Fields of Summer

        runCode("planeswalk to The Command Zone", 1, PhaseStep.POSTCOMBAT_MAIN, playerA,
                (info, player, game) -> Assert.assertTrue(info, game.addPlane(
                        Plane.createPlane(Planes.PLANE_THE_COMMAND_ZONE), playerA.getId()
                )));
        setChoice(playerB, false);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertPermanentCount(playerA, "Daxos of Meletis", 1);
        assertHandCount(playerA, 1);
    }

    @Test
    public void testCommanderAbilityTriggersAdditionalTimeDuringYourTurn() {
        addPlane(playerA, Planes.PLANE_THE_COMMAND_ZONE);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 1);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Daxos of Meletis");
        attack(3, playerA, "Daxos of Meletis");

        setStopAt(3, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertExileCount(playerB, 2);
    }

    @Test
    public void testNonCommanderAbilityDoesNotTriggerAdditionalTime() {
        removeAllCardsFromHand(playerA);
        addPlane(playerA, Planes.PLANE_THE_COMMAND_ZONE);
        addCard(Zone.BATTLEFIELD, playerA, "Thieving Magpie");

        attack(1, playerA, "Thieving Magpie");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, 1);
    }

    @Test
    public void testChaosResetsCommanderTax() {
        addPlane(playerA, Planes.PLANE_THE_COMMAND_ZONE);
        setChoice(playerB, false); // Don't put the other player's commander onto the battlefield
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 1);
        addCard(Zone.BATTLEFIELD, playerB, "Plains", 2);
        addCard(Zone.BATTLEFIELD, playerB, "Island", 1);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Daxos of Meletis");
        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, "Daxos of Meletis");

        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);
        castSpell(3, PhaseStep.POSTCOMBAT_MAIN, playerA, "Cause Chaos");

        setStopAt(3, PhaseStep.END_TURN);
        execute();

        UUID commanderAId = currentGame.getCommandersIds(
                playerA, CommanderCardType.COMMANDER_OR_OATHBREAKER, false
        ).iterator().next();
        UUID commanderBId = currentGame.getCommandersIds(
                playerB, CommanderCardType.COMMANDER_OR_OATHBREAKER, false
        ).iterator().next();
        CommanderPlaysCountWatcher watcher = currentGame.getState()
                .getWatcher(CommanderPlaysCountWatcher.class);
        Assert.assertEquals(0, watcher.getPlaysCount(commanderAId));
        Assert.assertEquals(0, watcher.getPlaysCount(commanderBId));
        Assert.assertEquals(0, watcher.getPlayerCount(playerA.getId()));
        Assert.assertEquals(0, watcher.getPlayerCount(playerB.getId()));
    }
}
