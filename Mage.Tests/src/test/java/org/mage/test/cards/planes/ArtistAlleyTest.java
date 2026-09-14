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

public class ArtistAlleyTest extends CardTestPlayerBase {

    @Test
    public void planeswalkAndUpkeepExileCardsThatCanBePlayed() {
        addPlane(playerA, Planes.PLANE_ARTIST_ALLEY);
        addCard(Zone.LIBRARY, playerA, "Mountain", 2);

        setChoice(playerA, "When you planeswalk");
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertExileCount(playerA, "Mountain", 2);
    }

    @Test
    public void chaosLetsOpponentChooseSpellAndControllerCastItForFree() {
        addPlane(playerA, Planes.PLANE_ARTIST_ALLEY);
        addCard(Zone.LIBRARY, playerA, "Lightning Bolt", 8);
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        setChoice(playerA, "When you planeswalk");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        setChoice(playerB, "Lightning Bolt");
        setChoice(playerA, true);
        addTarget(playerA, playerB);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 17);
        assertExileCount(playerA, "Lightning Bolt", 7);
        assertGraveyardCount(playerA, "Lightning Bolt", 1);
    }

    @Test
    public void chaosDoesNothingFurtherWhenOnlyLandsAreExiled() {
        addPlane(playerA, Planes.PLANE_ARTIST_ALLEY);
        addCard(Zone.LIBRARY, playerA, "Mountain", 8);
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        setChoice(playerA, "When you planeswalk");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertExileCount(playerA, "Mountain", 8);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_ARTIST_ALLEY)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Artist Alley", metadata.getEnglishName());
        Assert.assertEquals("Plane - Artist Alley", metadata.getImageName());
        Assert.assertEquals("PUNK", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
