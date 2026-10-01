package org.mage.test.cards.single.sos;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.game.permanent.Permanent;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class LandscapePainterTest extends CardTestPlayerBase {

    private static final String PAINTER = "Landscape Painter";
    private static final String VIBRANT_IDEA = "Vibrant Idea";

    @Test
    public void entersPrepared() {
        // Use the set-qualified printing so this test also guards card-repository registration.
        addCard(Zone.HAND, playerA, "SOS-" + PAINTER);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, PAINTER);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, PAINTER, 1);
        assertExileCount(playerA, VIBRANT_IDEA, 1);
        Permanent painter = getPermanent(PAINTER, playerA);
        Assert.assertNotNull(painter);
        Assert.assertTrue(painter.isPrepared());
    }

    @Test
    public void castsVibrantIdeaToDrawTwoCardsAndBecomeUnprepared() {
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.LIBRARY, playerA, "Darksteel Relic", 2);
        addCard(Zone.HAND, playerA, PAINTER);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 7);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, PAINTER);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPlayableAbility("prepared spell is castable", 1, PhaseStep.PRECOMBAT_MAIN,
                playerA, "Cast " + VIBRANT_IDEA, true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, VIBRANT_IDEA);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPlayableAbility("prepared spell cannot be cast again", 1, PhaseStep.PRECOMBAT_MAIN,
                playerA, "Cast " + VIBRANT_IDEA, false);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertHandCount(playerA, "Darksteel Relic", 2);
        assertExileCount(playerA, VIBRANT_IDEA, 0);
        Permanent painter = getPermanent(PAINTER, playerA);
        Assert.assertNotNull(painter);
        Assert.assertFalse(painter.isPrepared());
    }
}
