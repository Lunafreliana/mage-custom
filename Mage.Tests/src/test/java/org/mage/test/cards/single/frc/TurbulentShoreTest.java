package org.mage.test.cards.single.frc;

import mage.constants.PhaseStep;
import mage.constants.SubType;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class TurbulentShoreTest extends CardTestPlayerBase {

    private static final String turbulentShore = "Turbulent Shore";

    @Test
    public void entersTappedWhenOpponentControlsSevenLands() {
        addCard(Zone.HAND, playerA, turbulentShore);
        addCard(Zone.BATTLEFIELD, playerB, "Plains", 7);

        playLand(1, PhaseStep.PRECOMBAT_MAIN, playerA, turbulentShore);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertTapped(turbulentShore, true);
        assertSubtype(turbulentShore, SubType.PLAINS);
        assertSubtype(turbulentShore, SubType.ISLAND);
    }

    @Test
    public void entersUntappedWhenOpponentControlsEightLands() {
        addCard(Zone.HAND, playerA, turbulentShore);
        addCard(Zone.BATTLEFIELD, playerB, "Plains", 8);

        playLand(1, PhaseStep.PRECOMBAT_MAIN, playerA, turbulentShore);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertTapped(turbulentShore, false);
    }
}
