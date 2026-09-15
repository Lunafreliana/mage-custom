package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.abilities.effects.common.DrawCardTargetEffect;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.PlanarCardRegistry;
import mage.target.common.TargetPlayer;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class SeaOfSandTest extends CardTestPlayerBase {

    @Test
    public void eachPlayerGetsTheDrawnCardsLifeResult() {
        removeAllCardsFromHand(playerA);
        removeAllCardsFromHand(playerB);
        removeAllCardsFromLibrary(playerA);
        removeAllCardsFromLibrary(playerB);
        addPlane(playerA, Planes.PLANE_SEA_OF_SAND);
        addCard(Zone.LIBRARY, playerA, "Mountain");
        addCard(Zone.LIBRARY, playerB, "Grizzly Bears");
        SpellAbility drawLand = new SpellAbility(new ManaCostsImpl<>("{0}"), "Draw Land");
        drawLand.addEffect(new DrawCardTargetEffect(1));
        drawLand.addTarget(new TargetPlayer());
        addCustomCardWithSpell(playerA, drawLand, null, CardType.SORCERY);
        SpellAbility drawNonland = new SpellAbility(new ManaCostsImpl<>("{0}"), "Draw Nonland");
        drawNonland.addEffect(new DrawCardTargetEffect(1));
        drawNonland.addTarget(new TargetPlayer());
        addCustomCardWithSpell(playerA, drawNonland, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Draw Land", playerA);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Draw Nonland", playerB);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerA, 23);
        assertLife(playerB, 17);
        assertHandCount(playerA, "Mountain", 1);
        assertHandCount(playerB, "Grizzly Bears", 1);
    }

    @Test
    public void chaosPutsPermanentOnOwnersLibrary() {
        addPlane(playerA, Planes.PLANE_SEA_OF_SAND);
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Hill Giant");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerB, "Hill Giant", 0);
        assertLibraryCount(playerB, "Hill Giant", 1);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_SEA_OF_SAND)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Sea of Sand", metadata.getEnglishName());
        Assert.assertEquals("Plane - Sea of Sand", metadata.getImageName());
        Assert.assertEquals("PCA", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
