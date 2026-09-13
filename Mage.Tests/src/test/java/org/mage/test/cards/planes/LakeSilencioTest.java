package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.PlanarCardRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class LakeSilencioTest extends CardTestPlayerBase {

    @Test
    public void allPlayersSpellsHaveSplitSecond() {
        addPlane(playerA, Planes.PLANE_LAKE_SILENCIO);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 2);
        addCard(Zone.BATTLEFIELD, playerB, "Mountain");
        addCard(Zone.HAND, playerA, "Counterspell");
        addCard(Zone.HAND, playerB, "Shock");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerB, "Shock", playerA);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Counterspell", "Shock");

        setStopAt(1, PhaseStep.END_TURN);

        try {
            execute();
            Assert.fail("must not allow a spell to be cast while another spell is on the stack");
        } catch (AssertionError e) {
            Assert.assertTrue(e.getMessage().contains("Can't find ability to activate command: Cast Counterspell"));
        }
    }

    @Test
    public void chaosDamagesOnlyOpponentsCreatureAndExilesItIfItDies() {
        addPlane(playerA, Planes.PLANE_LAKE_SILENCIO);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Hill Giant");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Grizzly Bears", 1);
        assertExileCount(playerB, "Hill Giant", 1);
        assertGraveyardCount(playerB, "Hill Giant", 0);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_LAKE_SILENCIO)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Lake Silencio", metadata.getEnglishName());
        Assert.assertEquals("Plane - Lake Silencio", metadata.getImageName());
        Assert.assertEquals("WHO", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
