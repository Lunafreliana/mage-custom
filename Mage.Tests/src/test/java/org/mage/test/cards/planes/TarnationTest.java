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

public class TarnationTest extends CardTestPlayerBase {

    @Test
    public void criminalMayDrawACard() {
        addPlane(playerA, Planes.PLANE_TARNATION);
        addCard(Zone.HAND, playerB, "Shock");
        addCard(Zone.BATTLEFIELD, playerB, "Mountain");
        addCard(Zone.LIBRARY, playerB, "Island", 2);

        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, "Shock", playerA);
        setChoice(playerB, true);

        setStrictChooseMode(true);
        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerA, 18);
        assertHandCount(playerB, "Island", 2);
    }

    @Test
    public void criminalMayDeclineToDraw() {
        addPlane(playerA, Planes.PLANE_TARNATION);
        addCard(Zone.HAND, playerB, "Shock");
        addCard(Zone.BATTLEFIELD, playerB, "Mountain");
        addCard(Zone.LIBRARY, playerB, "Island", 2);

        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, "Shock", playerA);
        setChoice(playerB, false);

        setStrictChooseMode(true);
        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerB, "Island", 1);
    }

    @Test
    public void chaosDealsOneDamageToChosenTarget() {
        addPlane(playerA, Planes.PLANE_TARNATION);
        addChaosSpell();

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, playerB);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 19);
    }

    @Test
    public void registryExposesMetadata() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_TARNATION);

        Assert.assertEquals("Tarnation", PlanarCardRegistry.getMetadata(id).getEnglishName());
        Assert.assertEquals("Plane - Tarnation", PlanarCardRegistry.getMetadata(id).getImageName());
        Assert.assertEquals("PUNK", PlanarCardRegistry.getMetadata(id).getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }

    private void addChaosSpell() {
        SpellAbility ability = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        ability.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, ability, null, CardType.SORCERY);
    }
}
