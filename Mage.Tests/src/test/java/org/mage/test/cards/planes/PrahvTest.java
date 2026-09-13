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

public class PrahvTest extends CardTestPlayerBase {

    @Test
    public void castingSpellPreventsAttacking() {
        addPlane(playerA, Planes.PLANE_PRAHV);
        addCard(Zone.HAND, playerA, "Memnite");
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Memnite");
        attack(1, playerA, "Grizzly Bears", playerB);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        try {
            execute();
            Assert.fail("Prahv must prevent the queued attack");
        } catch (AssertionError error) {
            Assert.assertTrue(error.getMessage(),
                    error.getMessage().contains("Player PlayerA must have 0 actions but found 1"));
        }

        assertLife(playerB, 20);
        assertPermanentCount(playerA, "Memnite", 1);
    }

    @Test
    public void attackingPreventsCasting() {
        addPlane(playerA, Planes.PLANE_PRAHV);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 2);
        addCard(Zone.HAND, playerA, "Silvercoat Lion");

        attack(1, playerA, "Grizzly Bears", playerB);
        checkPlayableAbility("can't cast after attacking", 1, PhaseStep.POSTCOMBAT_MAIN,
                playerA, "Cast Silvercoat Lion", false);

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertLife(playerB, 18);
        assertHandCount(playerA, "Silvercoat Lion", 1);
        assertPermanentCount(playerA, "Silvercoat Lion", 0);
    }

    @Test
    public void chaosGainsLifeForPlanarControllersHand() {
        removeAllCardsFromHand(playerA);
        addPlane(playerA, Planes.PLANE_PRAHV);
        addCard(Zone.HAND, playerA, "Mountain", 3);
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerA, 23);
        assertLife(playerB, 20);
    }

    @Test
    public void registryExposesPrahvMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_PRAHV)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Prahv", metadata.getEnglishName());
        Assert.assertEquals("Plane - Prahv", metadata.getImageName());
        Assert.assertEquals("PCA", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
