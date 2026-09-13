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
import org.mage.test.serverside.base.CardTestCommander4Players;

public class KharashaFoothillsTest extends CardTestCommander4Players {

    @Test
    public void attackingPlayerCreatesCopiesForOtherOpponents() {
        addPlane(playerA, Planes.PLANE_KHARASHA_FOOTHILLS);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");

        attack(1, playerA, "Grizzly Bears", playerB);
        setChoice(playerA, true); // Create a copy attacking player D.
        setChoice(playerA, true); // Create a copy attacking player C.

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertTokenCount(playerA, "Grizzly Bears", 2);
        assertLife(playerB, 18);
        assertLife(playerC, 18);
        assertLife(playerD, 18);
    }

    @Test
    public void copiesAreExiledAtBeginningOfNextEndStep() {
        addPlane(playerA, Planes.PLANE_KHARASHA_FOOTHILLS);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");

        attack(1, playerA, "Grizzly Bears", playerB);
        setChoice(playerA, true);
        setChoice(playerA, true);

        setStrictChooseMode(true);
        setStopAt(2, PhaseStep.UPKEEP);
        execute();

        assertTokenCount(playerA, "Grizzly Bears", 0);
    }

    @Test
    public void attackingPlaneswalkerDoesNotTrigger() {
        addPlane(playerA, Planes.PLANE_KHARASHA_FOOTHILLS);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Jace Beleren");

        attack(1, playerA, "Grizzly Bears", "Jace Beleren");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertTokenCount(playerA, "Grizzly Bears", 0);
    }

    @Test
    public void chaosSacrificesChosenCreaturesAndDealsThatMuchDamage() {
        addPlane(playerA, Planes.PLANE_KHARASHA_FOOTHILLS);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Silvercoat Lion");
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");
        addChaosSpell();

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Hill Giant");
        setChoice(playerA, true);
        setChoice(playerA, "Grizzly Bears^Silvercoat Lion");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertGraveyardCount(playerA, "Grizzly Bears", 1);
        assertGraveyardCount(playerA, "Silvercoat Lion", 1);
        assertDamageReceived(playerB, "Hill Giant", 2);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_KHARASHA_FOOTHILLS)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Kharasha Foothills", metadata.getEnglishName());
        Assert.assertEquals("Plane - Kharasha Foothills", metadata.getImageName());
        Assert.assertEquals("MOC", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }

    private void addChaosSpell() {
        SpellAbility ability = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        ability.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, ability, null, CardType.SORCERY);
    }
}
