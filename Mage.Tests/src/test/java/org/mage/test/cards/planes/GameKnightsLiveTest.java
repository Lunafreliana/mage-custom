package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.common.DealsCombatDamageToAPlayerTriggeredAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.abilities.keyword.DoubleStrikeAbility;
import mage.abilities.keyword.TrampleAbility;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.command.PlanarCardRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestCommanderDuelBase;

public class GameKnightsLiveTest extends CardTestCommanderDuelBase {

    @Test
    public void castCommanderCanMakeItAKnight() {
        addPlane(playerA, Planes.PLANE_GAME_KNIGHTS_LIVE);
        addCard(Zone.COMMAND, playerA, "Balduvian Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Balduvian Bears");
        setChoice(playerA, true); // Say "Only one may stand."

        runCode("commander is a Knight", 1, PhaseStep.BEGIN_COMBAT, playerA,
                (info, player, game) -> Assert.assertTrue(game.getBattlefield()
                        .getAllActivePermanents(player.getId()).stream()
                        .filter(permanent -> permanent.getName().equals("Balduvian Bears"))
                        .findFirst().orElseThrow(AssertionError::new).hasSubtype(SubType.KNIGHT, game)));
        checkAbility("Knight has double strike", 1, PhaseStep.BEGIN_COMBAT,
                playerA, "Balduvian Bears", DoubleStrikeAbility.class, true);
        checkAbility("Knight has trample", 1, PhaseStep.BEGIN_COMBAT,
                playerA, "Balduvian Bears", TrampleAbility.class, true);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();
    }

    @Test
    public void decliningLeavesCommanderUnchanged() {
        addPlane(playerA, Planes.PLANE_GAME_KNIGHTS_LIVE);
        addCard(Zone.COMMAND, playerA, "Balduvian Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Balduvian Bears");
        setChoice(playerA, false);

        runCode("commander is not a Knight", 1, PhaseStep.BEGIN_COMBAT, playerA,
                (info, player, game) -> Assert.assertFalse(game.getBattlefield()
                        .getAllActivePermanents(player.getId()).stream()
                        .filter(permanent -> permanent.getName().equals("Balduvian Bears"))
                        .findFirst().orElseThrow(AssertionError::new).hasSubtype(SubType.KNIGHT, game)));

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();
    }

    @Test
    public void chaosGrantsCommanderCombatAbilitiesUntilEndOfTurn() {
        addPlane(playerA, Planes.PLANE_GAME_KNIGHTS_LIVE);
        addCard(Zone.COMMAND, playerA, "Balduvian Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Balduvian Bears");
        setChoice(playerA, false);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Cause Chaos");

        checkAbility("commander gains card-draw trigger", 1, PhaseStep.END_TURN,
                playerA, "Balduvian Bears", DealsCombatDamageToAPlayerTriggeredAbility.class, true);
        checkAbility("card-draw trigger expires", 2, PhaseStep.UPKEEP,
                playerA, "Balduvian Bears", DealsCombatDamageToAPlayerTriggeredAbility.class, false);

        setStopAt(2, PhaseStep.UPKEEP);
        execute();
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_GAME_KNIGHTS_LIVE)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Game Knights Live", metadata.getEnglishName());
        Assert.assertEquals("Plane - Game Knights Live", metadata.getImageName());
        Assert.assertEquals("PUNK", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
