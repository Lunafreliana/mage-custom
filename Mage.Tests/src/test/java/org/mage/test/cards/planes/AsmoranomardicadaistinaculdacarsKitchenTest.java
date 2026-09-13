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

public class AsmoranomardicadaistinaculdacarsKitchenTest extends CardTestPlayerBase {

    @Test
    public void planeswalkingAndCreatureEnteringCreateFood() {
        addPlane(playerA, Planes.PLANE_ASMORANOMARDICADAISTINACULDACARS_KITCHEN);
        addCard(Zone.HAND, playerA, "Memnite");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Memnite");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Food Token", 2);
    }

    @Test
    public void gainingLifeMakesTargetOpponentLoseThatMuch() {
        addPlane(playerA, Planes.PLANE_ASMORANOMARDICADAISTINACULDACARS_KITCHEN);
        addCard(Zone.HAND, playerA, "Angel's Mercy");
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 4);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Angel's Mercy");
        addTarget(playerA, playerB);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerA, 27);
        assertLife(playerB, 13);
    }

    @Test
    public void chaosDrawsAndLosesLifeForPermanentsActuallySacrificed() {
        skipInitShuffling();
        removeAllCardsFromLibrary(playerA);
        removeAllCardsFromLibrary(playerB);
        addPlane(playerA, Planes.PLANE_ASMORANOMARDICADAISTINACULDACARS_KITCHEN);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Sol Ring");
        addCard(Zone.LIBRARY, playerA, "Island", 5);
        addChaosSpell();

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Grizzly Bears^Sol Ring");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertGraveyardCount(playerA, "Grizzly Bears", 1);
        assertGraveyardCount(playerA, "Sol Ring", 1);
        // The starting player skips their first draw step, so these are exactly the two cards
        // drawn by the chaos ability.
        assertHandCount(playerA, "Island", 2);
        assertLife(playerA, 18);
    }

    @Test
    public void registryExposesMetadata() {
        String id = PlanarCardRegistry.getId(
                Planes.PLANE_ASMORANOMARDICADAISTINACULDACARS_KITCHEN
        );
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Asmoranomardicadaistinaculdacar's Kitchen", metadata.getEnglishName());
        Assert.assertEquals("Plane - Asmoranomardicadaistinaculdacar's Kitchen", metadata.getImageName());
        Assert.assertEquals("PUNK", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }

    private void addChaosSpell() {
        SpellAbility ability = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        ability.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, ability, null, CardType.SORCERY);
    }
}
