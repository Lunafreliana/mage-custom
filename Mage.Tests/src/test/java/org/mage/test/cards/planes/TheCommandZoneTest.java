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
import mage.game.command.PlanarDeckMode;
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
                (info, player, game) -> {
                    Plane commandZone = Plane.createPlane(Planes.PLANE_THE_COMMAND_ZONE);
                    commandZone.setPlanarDeckOwnerId(playerA.getId());
                    game.getState().setPlayerPlanarDeck(
                            playerA.getId(), Collections.singletonList(commandZone), false
                    );
                    game.getState().getFaceUpPlanarCards().forEach(card ->
                            card.setPlanarDeckOwnerId(playerA.getId()));
                    game.getState().setPlanarDeckMode(PlanarDeckMode.INDIVIDUAL);
                    Assert.assertTrue(info, game.planeswalk(playerA.getId()));
                });

        setChoice(playerA, true);
        setChoice(playerB, false);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Daxos of Meletis", 1);
        assertCommandZoneCount(playerB, "Daxos of Meletis", 1);
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
    public void testChaosResetsCommanderTax() {
        addPlane(playerA, Planes.PLANE_THE_COMMAND_ZONE);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 1);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Daxos of Meletis");

        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Cause Chaos");

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        UUID commanderId = currentGame.getCommandersIds(
                playerA, CommanderCardType.COMMANDER_OR_OATHBREAKER, false
        ).iterator().next();
        CommanderPlaysCountWatcher watcher = currentGame.getState()
                .getWatcher(CommanderPlaysCountWatcher.class);
        Assert.assertEquals(0, watcher.getPlaysCount(commanderId));
        Assert.assertEquals(0, watcher.getPlayerCount(playerA.getId()));
    }
}
