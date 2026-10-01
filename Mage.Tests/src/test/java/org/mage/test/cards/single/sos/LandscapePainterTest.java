package org.mage.test.cards.single.sos;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author TheElk801
 */
public class LandscapePainterTest extends CardTestPlayerBase {

    private static final String PAINTER = "Landscape Painter";
    private static final String VIBRANT_IDEA = "Vibrant Idea";

    @Test
    public void testEntersPreparedAndCastsVibrantIdea() {
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.LIBRARY, playerA, "Forest", 2);
        addCard(Zone.HAND, playerA, PAINTER);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 7);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, PAINTER);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPlayableAbility("prepared spell is castable", 1, PhaseStep.PRECOMBAT_MAIN,
                playerA, "Cast " + VIBRANT_IDEA, true);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, VIBRANT_IDEA);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, PAINTER, 1);
        assertPowerToughness(playerA, PAINTER, 2, 1);
        assertHandCount(playerA, "Forest", 2);
        assertExileCount(playerA, VIBRANT_IDEA, 0);
    }
}
