package org.mage.test.cards.single.who;

import mage.abilities.Ability;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.game.ExileZone;
import mage.game.permanent.Permanent;
import mage.util.CardUtil;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import static org.mage.test.serverside.base.impl.CardTestPlayerAPIImpl.StackClause.WHILE_ON_STACK;

public class RiverSongsDiaryTest extends CardTestPlayerBase {

    @Test
    public void exilesResolvedSpellsCastFromHandButNotCounteredSpells() {
        addCard(Zone.BATTLEFIELD, playerA, "River Song's Diary");
        addCard(Zone.BATTLEFIELD, playerA, "Island", 3);
        addCard(Zone.HAND, playerA, "Opt");
        addCard(Zone.HAND, playerA, "Counterspell");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Opt");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Counterspell", "Opt", "Opt", WHILE_ON_STACK);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertExileCount("Counterspell", 1);
        assertGraveyardCount(playerA, "Counterspell", 0);
        assertGraveyardCount(playerA, "Opt", 1);
    }

    @Test
    public void randomlyChoosesAndOffersToCastAtFourImprintedCards() {
        addCard(Zone.BATTLEFIELD, playerA, "River Song's Diary");
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 4);
        addCard(Zone.HAND, playerA, "Lightning Bolt", 4);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Lightning Bolt", playerB);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Lightning Bolt", playerB);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Lightning Bolt", playerB);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Lightning Bolt", playerB);
        runCode("verify Diary imprint state", 1, PhaseStep.POSTCOMBAT_MAIN, playerA, (info, player, game) -> {
            Permanent diary = game.getBattlefield().getAllActivePermanents(player.getId()).stream()
                    .filter(permanent -> permanent.getName().equals("River Song's Diary"))
                    .findFirst()
                    .orElse(null);
            Assert.assertNotNull("River Song's Diary must be on the battlefield", diary);

            Ability upkeepAbility = diary.getAbilities(game).stream()
                    .filter(BeginningOfUpkeepTriggeredAbility.class::isInstance)
                    .findFirst()
                    .orElse(null);
            Assert.assertNotNull("River Song's Diary must have its upkeep ability", upkeepAbility);

            ExileZone imprintZone = game.getExile().getExileZone(CardUtil.getExileZoneId(
                    game,
                    upkeepAbility.getSourceId(),
                    CardUtil.getActualSourceObjectZoneChangeCounter(game, upkeepAbility)
            ));
            Assert.assertNotNull("River Song's Diary must have an associated imprint exile zone", imprintZone);
            Assert.assertEquals("exactly four cards must be imprinted by this Diary", 4, imprintZone.size());
        });
        setChoice(playerA, true);
        addTarget(playerA, playerB);

        setStopAt(3, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertLife(playerB, 5);
        assertExileCount("Lightning Bolt", 3);
        assertGraveyardCount(playerA, "Lightning Bolt", 1);
    }
}
