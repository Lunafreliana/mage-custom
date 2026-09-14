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

public class BlackLotusLoungeTest extends CardTestPlayerBase {

    @Test
    public void planeswalkAndUpkeepCreateBlackLotusTokens() {
        addPlane(playerA, Planes.PLANE_BLACK_LOTUS_LOUNGE);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Black Lotus", 2);
        assertPermanentCount(playerB, "Black Lotus", 0);
    }

    @Test
    public void chaosCountsEachPlayersArtifactTokens() {
        addPlane(playerA, Planes.PLANE_BLACK_LOTUS_LOUNGE);
        addCard(Zone.BATTLEFIELD, playerA, "Sol Ring");
        addCard(Zone.LIBRARY, playerA, "Mountain", 9);
        addCard(Zone.LIBRARY, playerB, "Island", 9);
        skipInitShuffling();
        SpellAbility ability = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        ability.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, ability, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertHandCount(playerA, "Mountain", 9);
        assertHandCount(playerB, "Island", 7);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_BLACK_LOTUS_LOUNGE)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Black Lotus Lounge", metadata.getEnglishName());
        Assert.assertEquals("Plane - Black Lotus Lounge", metadata.getImageName());
        Assert.assertEquals("PUNK", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
