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

public class PrestonsStageTest extends CardTestPlayerBase {

    @Test
    public void instantAndFlashSpellCreateHatsForTheirCasters() {
        addPlane(playerA, Planes.PLANE_PRESTONS_STAGE);
        addCard(Zone.HAND, playerA, "Shock");
        addCard(Zone.BATTLEFIELD, playerA, "Mountain");
        addCard(Zone.HAND, playerB, "Ambush Viper");
        addCard(Zone.BATTLEFIELD, playerB, "Forest", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Shock", playerB);
        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, "Ambush Viper");

        setStrictChooseMode(true);
        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Hat Token", 1);
        assertPermanentCount(playerB, "Hat Token", 1);
    }

    @Test
    public void turningCreatureFaceUpCreatesHatForItsController() {
        addPlane(playerA, Planes.PLANE_PRESTONS_STAGE);
        addCard(Zone.HAND, playerB, "Pine Walker");
        addCard(Zone.BATTLEFIELD, playerB, "Forest", 5);

        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerB, "Pine Walker using Morph");
        activateAbility(2, PhaseStep.POSTCOMBAT_MAIN, playerB,
                "{4}{G}: Turn this face-down permanent face up.");

        setStrictChooseMode(true);
        setStopAt(2, PhaseStep.END_TURN);
        execute();

        assertPermanentCount(playerB, "Hat Token", 1);
    }

    @Test
    public void hatCreatesRabbit() {
        addPlane(playerA, Planes.PLANE_PRESTONS_STAGE);
        addCard(Zone.HAND, playerA, "Shock");
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Shock", playerB);
        activateAbility(1, PhaseStep.POSTCOMBAT_MAIN, playerA,
                "{1}, {T}, Sacrifice this artifact: Create a 1/1 white Rabbit creature token");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertPermanentCount(playerA, "Hat Token", 0);
        assertPermanentCount(playerA, "Rabbit Token", 1);
    }

    @Test
    public void chaosGivesCreatureCardsFlashAndCastingOnePerformsMagicTrick() {
        addPlane(playerA, Planes.PLANE_PRESTONS_STAGE);
        addChaosSpell();
        addCard(Zone.HAND, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerA, "Grizzly Bears");

        setStrictChooseMode(true);
        setStopAt(2, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Grizzly Bears", 1);
        assertPermanentCount(playerA, "Hat Token", 1);
    }

    @Test
    public void registryExposesMetadata() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_PRESTONS_STAGE);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Preston's Stage", metadata.getEnglishName());
        Assert.assertEquals("Plane - Preston's Stage", metadata.getImageName());
        Assert.assertEquals("PUNK", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }

    private void addChaosSpell() {
        SpellAbility ability = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        ability.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, ability, null, CardType.SORCERY);
    }
}
