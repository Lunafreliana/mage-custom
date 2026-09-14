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

public class MultiversalHighCouncilTest extends CardTestPlayerBase {

    @Test
    public void universesBeyondSpellCostsOneLess() {
        addPlane(playerA, Planes.PLANE_MULTIVERSAL_HIGH_COUNCIL);
        addCard(Zone.HAND, playerA, "WHO-Astrid Peth"); // {1}{W}
        addCard(Zone.BATTLEFIELD, playerA, "Plains");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Astrid Peth");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, "Astrid Peth", 1);
    }

    @Test
    public void creaturesCountDifferentUniversesTheirControllerControls() {
        addPlane(playerA, Planes.PLANE_MULTIVERSAL_HIGH_COUNCIL);
        addCard(Zone.BATTLEFIELD, playerA, "ICE-Balduvian Bears"); // Magic, 2/2
        addCard(Zone.BATTLEFIELD, playerA, "WHO-Astrid Peth"); // Doctor Who, 2/2
        addCard(Zone.BATTLEFIELD, playerA, "AFR-Dawnbringer Cleric"); // D&D, 1/3

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        assertPowerToughness(playerA, "Balduvian Bears", 5, 5);
        assertPowerToughness(playerA, "Astrid Peth", 5, 5);
        assertPowerToughness(playerA, "Dawnbringer Cleric", 4, 6);
    }

    @Test
    public void chaosReturnsAtMostOneCardFromEachUniverse() {
        addPlane(playerA, Planes.PLANE_MULTIVERSAL_HIGH_COUNCIL);
        addChaosSpell();
        addCard(Zone.GRAVEYARD, playerA, "ICE-Balduvian Bears");
        addCard(Zone.GRAVEYARD, playerA, "WHO-Astrid Peth");
        addCard(Zone.GRAVEYARD, playerA, "AFR-Dawnbringer Cleric");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Balduvian Bears^Astrid Peth^Dawnbringer Cleric");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertHandCount(playerA, "Balduvian Bears", 1);
        assertHandCount(playerA, "Astrid Peth", 1);
        assertHandCount(playerA, "Dawnbringer Cleric", 1);
    }

    @Test
    public void registryExposesMetadata() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_MULTIVERSAL_HIGH_COUNCIL);

        Assert.assertEquals("Multiversal High Council", PlanarCardRegistry.getMetadata(id).getEnglishName());
        Assert.assertEquals("Plane - Multiversal High Council", PlanarCardRegistry.getMetadata(id).getImageName());
        Assert.assertEquals("PUNK", PlanarCardRegistry.getMetadata(id).getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }

    private void addChaosSpell() {
        SpellAbility ability = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        ability.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, ability, null, CardType.SORCERY);
    }
}
