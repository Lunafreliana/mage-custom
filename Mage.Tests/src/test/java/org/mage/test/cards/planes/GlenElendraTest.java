package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.abilities.effects.common.continuous.GainControlTargetEffect;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.PlanarCardRegistry;
import mage.target.common.TargetCreaturePermanent;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * Tests for Plane - Glen Elendra.
 */
public class GlenElendraTest extends CardTestPlayerBase {

    @Test
    public void exchangesWithCreatureControlledByDamagedPlayer() {
        addPlane(playerA, Planes.PLANE_GLEN_ELENDRA);
        addCard(Zone.BATTLEFIELD, playerA, "Runeclaw Bear");
        addCard(Zone.BATTLEFIELD, playerB, "Glory Seeker");

        attack(1, playerA, "Runeclaw Bear", playerB);
        addTarget(playerA, "Runeclaw Bear");
        addTarget(playerA, "Glory Seeker");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Glory Seeker", 1);
        assertPermanentCount(playerB, "Runeclaw Bear", 1);
    }

    @Test
    public void chaosReturnsCreatureToItsOwner() {
        addPlane(playerA, Planes.PLANE_GLEN_ELENDRA);
        addCard(Zone.BATTLEFIELD, playerA, "Runeclaw Bear");

        SpellAbility steal = new SpellAbility(new ManaCostsImpl<>("{0}"), "Steal");
        steal.addEffect(new GainControlTargetEffect(Duration.EndOfGame));
        steal.addTarget(new TargetCreaturePermanent());
        addCustomCardWithSpell(playerB, steal, null, CardType.SORCERY);

        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, "Steal", "Runeclaw Bear");
        castSpell(3, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");

        setStopAt(3, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Runeclaw Bear", 1);
        assertPermanentCount(playerB, "Runeclaw Bear", 0);
    }

    @Test
    public void isAvailableThroughPlanarCardRegistry() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_GLEN_ELENDRA);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Glen Elendra", metadata.getEnglishName());
        Assert.assertEquals("PCA", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }
}
