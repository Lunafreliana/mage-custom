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

public class HorizonBoughsTest extends CardTestPlayerBase {

    @Test
    public void allPermanentsUntapDuringEachUntapStep() {
        addPlane(playerA, Planes.PLANE_HORIZON_BOUGHS);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 1, true);
        addCard(Zone.BATTLEFIELD, playerB, "Island", 1, true);

        setStopAt(1, PhaseStep.UPKEEP);
        execute();

        assertTapped("Forest", false);
        assertTapped("Island", false);
    }

    @Test
    public void chaosMayFindUpToThreeBasicLandsTapped() {
        addPlane(playerA, Planes.PLANE_HORIZON_BOUGHS);
        addCard(Zone.LIBRARY, playerA, "Forest");
        addCard(Zone.LIBRARY, playerA, "Island");
        addCard(Zone.LIBRARY, playerA, "Mountain");
        removeAllCardsFromLibrary(playerB);
        skipInitShuffling();

        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        setChoice(playerA, true);
        addTarget(playerA, "Forest^Island^Mountain");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Forest", 1);
        assertPermanentCount(playerA, "Island", 1);
        assertPermanentCount(playerA, "Mountain", 1);
        assertTapped("Forest", true);
        assertTapped("Island", true);
        assertTapped("Mountain", true);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_HORIZON_BOUGHS));

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Horizon Boughs", metadata.getEnglishName());
        Assert.assertEquals("Plane - Horizon Boughs", metadata.getImageName());
        Assert.assertEquals("PCA", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
