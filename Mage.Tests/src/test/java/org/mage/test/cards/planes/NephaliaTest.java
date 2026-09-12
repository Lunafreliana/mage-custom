package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * Tests for Plane - Nephalia.
 */
public class NephaliaTest extends CardTestPlayerBase {

    @Test
    public void endStepMillsThenReturnsOneCardAtRandom() {
        removeAllCardsFromHand(playerA);
        removeAllCardsFromLibrary(playerA);
        addPlane(playerA, Planes.PLANE_NEPHALIA);
        addCard(Zone.LIBRARY, playerA, "Grizzly Bears", 10);

        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertHandCount(playerA, 1);
        assertGraveyardCount(playerA, "Grizzly Bears", 6);
        assertLibraryCount(playerA, 3);
    }

    @Test
    public void chaosReturnsTargetCardFromPlanarControllersGraveyard() {
        addPlane(playerA, Planes.PLANE_NEPHALIA);
        addCard(Zone.GRAVEYARD, playerA, "Grizzly Bears");
        addCard(Zone.GRAVEYARD, playerA, "Hill Giant");

        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Grizzly Bears");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, "Grizzly Bears", 1);
        assertGraveyardCount(playerA, "Grizzly Bears", 0);
        assertGraveyardCount(playerA, "Hill Giant", 1);
    }
}
