package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.abilities.effects.common.TapTargetEffect;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.PlanarCardRegistry;
import mage.target.common.TargetCreaturePermanent;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class OodSphereTest extends CardTestPlayerBase {

    @Test
    public void noncreatureSpellsOfEveryPlayerHaveConvoke() {
        addPlane(playerA, Planes.PLANE_OOD_SPHERE);
        addCard(Zone.BATTLEFIELD, playerB, "Goblin Racketeer", 4);
        addCard(Zone.HAND, playerB, "Diabolic Tutor");
        addCard(Zone.HAND, playerB, "Hill Giant");

        checkPlayableAbility("opponent's noncreature spell has convoke", 2, PhaseStep.PRECOMBAT_MAIN,
                playerB, "Cast Diabolic Tutor", true);
        checkPlayableAbility("creature spell does not have convoke", 2, PhaseStep.PRECOMBAT_MAIN,
                playerB, "Cast Hill Giant", false);

        setStrictChooseMode(true);
        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();
    }

    @Test
    public void chaosPreventsNoncombatTappingButAllowsAttacking() {
        addPlane(playerA, Planes.PLANE_OOD_SPHERE);
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");

        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        SpellAbility tapCreature = new SpellAbility(new ManaCostsImpl<>("{0}"), "Tap creature");
        tapCreature.addEffect(new TapTargetEffect());
        tapCreature.addTarget(new TargetCreaturePermanent());
        addCustomCardWithSpell(playerA, tapCreature, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Hill Giant");
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Tap creature", "Hill Giant");
        attack(2, playerB, "Hill Giant", playerA);

        setStrictChooseMode(true);
        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerA, 17);
        assertTapped("Hill Giant", true);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_OOD_SPHERE)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Ood Sphere", metadata.getEnglishName());
        Assert.assertEquals("Plane - Ood Sphere", metadata.getImageName());
        Assert.assertEquals("WHO", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
