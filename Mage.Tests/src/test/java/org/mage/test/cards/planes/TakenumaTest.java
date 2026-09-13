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

public class TakenumaTest extends CardTestPlayerBase {

    @Test
    public void creatureControllerDrawsWhenCreatureLeavesBattlefield() {
        removeAllCardsFromHand(playerA);
        removeAllCardsFromHand(playerB);
        removeAllCardsFromLibrary(playerA);
        removeAllCardsFromLibrary(playerB);
        addPlane(playerA, Planes.PLANE_TAKENUMA);
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears");
        addCard(Zone.LIBRARY, playerB, "Silvercoat Lion");
        addCard(Zone.HAND, playerA, "Murder");
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 3);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Murder", "Hill Giant");
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertGraveyardCount(playerB, "Hill Giant", 1);
        assertHandCount(playerB, "Silvercoat Lion", 1);
        assertHandCount(playerA, "Grizzly Bears", 0);
    }

    @Test
    public void chaosReturnsControlledCreatureAndTriggersDraw() {
        removeAllCardsFromHand(playerA);
        removeAllCardsFromLibrary(playerA);
        addPlane(playerA, Planes.PLANE_TAKENUMA);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");
        addCard(Zone.LIBRARY, playerA, "Silvercoat Lion");
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Grizzly Bears");
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Grizzly Bears", 0);
        assertHandCount(playerA, "Grizzly Bears", 1);
        assertHandCount(playerA, "Silvercoat Lion", 1);
        assertPermanentCount(playerB, "Hill Giant", 1);
    }

    @Test
    public void registryExposesTakenumaMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_TAKENUMA)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Takenuma", metadata.getEnglishName());
        Assert.assertEquals("Plane - Takenuma", metadata.getImageName());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
