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

public class NyxTest extends CardTestPlayerBase {

    @Test
    public void nontokenCreaturesBecomeEnchantmentsAndTriggerConstellation() {
        addPlane(playerA, Planes.PLANE_NYX);
        addCard(Zone.HAND, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Grizzly Bears");
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertType("Grizzly Bears", CardType.CREATURE, true);
        assertType("Grizzly Bears", CardType.ENCHANTMENT, true);
        assertLife(playerA, 21);
    }

    @Test
    public void tokenCreaturesDoNotBecomeEnchantmentsOrTriggerConstellation() {
        addPlane(playerA, Planes.PLANE_NYX);
        addCard(Zone.HAND, playerA, "Raise the Alarm");
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Raise the Alarm");
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertType("Soldier Token", CardType.CREATURE, true);
        assertType("Soldier Token", CardType.ENCHANTMENT, false);
        assertLife(playerA, 20);
    }

    @Test
    public void chaosAddsManaEqualToChosenColorDevotion() {
        addPlane(playerA, Planes.PLANE_NYX);
        addCard(Zone.BATTLEFIELD, playerA, "Leatherback Baloth");
        addCustomCardWithSpell(playerA, createCauseChaosAbility(), null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        setChoice(playerA, "Green");
        checkManaPool("mana from green devotion", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "G", 3);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_NYX)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Nyx", metadata.getEnglishName());
        Assert.assertEquals("Plane - Nyx", metadata.getImageName());
        Assert.assertEquals("MOC", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }

    private static SpellAbility createCauseChaosAbility() {
        SpellAbility ability = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        ability.addEffect(new ChaosEnsuesEffect());
        return ability;
    }
}
