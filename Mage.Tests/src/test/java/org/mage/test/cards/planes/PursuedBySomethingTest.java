package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.constants.CardType;
import mage.constants.EmptyNames;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.PlanarCardRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class PursuedBySomethingTest extends CardTestPlayerBase {

    @Test
    public void tappedCreatureCausesManifestDread() {
        skipInitShuffling();
        addPlane(playerA, Planes.PLANE_PURSUED_BY_SOMETHING);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.LIBRARY, playerA, "Lightning Bolt");
        addCard(Zone.LIBRARY, playerA, "Forest");

        attack(1, playerA, "Grizzly Bears");
        setChoice(playerA, "Lightning Bolt");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertPermanentCount(playerA, EmptyNames.FACE_DOWN_CREATURE.getTestCommand(), 1);
        assertGraveyardCount(playerA, "Forest", 1);
    }

    @Test
    public void noTappedCreatureDoesNotManifestDread() {
        addPlane(playerA, Planes.PLANE_PURSUED_BY_SOMETHING);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertPermanentCount(playerA, EmptyNames.FACE_DOWN_CREATURE.getTestCommand(), 0);
    }

    @Test
    public void chaosReturnsCreatureUnderOwnersControl() {
        addPlane(playerA, Planes.PLANE_PURSUED_BY_SOMETHING);
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
        addChaosSpell();

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, "Grizzly Bears");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Grizzly Bears", 0);
        assertPermanentCount(playerB, "Grizzly Bears", 1);
        assertExileCount("Grizzly Bears", 0);
    }

    @Test
    public void chaosCastsManifestedInstantForFree() {
        skipInitShuffling();
        addPlane(playerA, Planes.PLANE_PURSUED_BY_SOMETHING);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.LIBRARY, playerA, "Lightning Bolt@manifestedBolt");
        addCard(Zone.LIBRARY, playerA, "Forest");
        addChaosSpell();

        attack(1, playerA, "Grizzly Bears");
        setChoice(playerA, "Lightning Bolt");
        // Wait until the next turn so the second-main-phase trigger has resolved
        // and the manifested card exists before choosing the chaos ability's target.
        castSpell(3, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, EmptyNames.FACE_DOWN_CREATURE.getTestCommand());
        addTarget(playerA, playerB);

        setStrictChooseMode(true);
        setStopAt(3, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 17);
        assertGraveyardCount(playerA, "Lightning Bolt", 1);
        assertPermanentCount(playerA, EmptyNames.FACE_DOWN_CREATURE.getTestCommand(), 0);
    }

    @Test
    public void registryExposesMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_PURSUED_BY_SOMETHING)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Pursued by Something", metadata.getEnglishName());
        Assert.assertEquals("Plane - Pursued by Something", metadata.getImageName());
        Assert.assertEquals("PUNK", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }

    private void addChaosSpell() {
        SpellAbility ability = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        ability.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, ability, null, CardType.SORCERY);
    }
}
