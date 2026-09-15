package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.abilities.keyword.ShadowAbility;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.command.PlanarCardRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Arrays;

public class TheBeanTest extends CardTestPlayerBase {

    @Test
    public void arrivalHasEachPlayerCopyAControlledCreatureAsAReflection() {
        useBeanAsPlaneswalkDestination();
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");

        setChoice(playerA, "Grizzly Bears");
        setChoice(playerB, "Hill Giant");
        runCode("planeswalk to The Bean", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                (info, player, game) -> Assert.assertTrue(info, game.planeswalk(player.getId())));
        runCode("verify player A's Reflection", 1, PhaseStep.POSTCOMBAT_MAIN, playerA,
                (info, player, game) -> Assert.assertTrue(info, game.getBattlefield()
                        .getAllActivePermanents(player.getId()).stream()
                        .anyMatch(permanent -> permanent.isToken()
                                && permanent.hasSubtype(SubType.REFLECTION, game)
                                && permanent.getAbilities().containsKey(ShadowAbility.getInstance().getId()))));
        runCode("verify player B's Reflection", 1, PhaseStep.POSTCOMBAT_MAIN, playerB,
                (info, player, game) -> Assert.assertTrue(info, game.getBattlefield()
                        .getAllActivePermanents(player.getId()).stream()
                        .anyMatch(permanent -> permanent.isToken()
                                && permanent.hasSubtype(SubType.REFLECTION, game)
                                && permanent.getAbilities().containsKey(ShadowAbility.getInstance().getId()))));
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Grizzly Bears", 2);
        assertPermanentCount(playerB, "Hill Giant", 2);
    }

    @Test
    public void chaosCopiesOnlyAPlanarControllersCreature() {
        addPlane(playerA, Planes.PLANE_THE_BEAN);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        setChoice(playerA, "Grizzly Bears");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        runCode("verify chaos Reflection", 1, PhaseStep.POSTCOMBAT_MAIN, playerA,
                (info, player, game) -> Assert.assertTrue(info, game.getBattlefield()
                        .getAllActivePermanents(player.getId()).stream()
                        .anyMatch(permanent -> permanent.isToken()
                                && permanent.hasSubtype(SubType.REFLECTION, game)
                                && permanent.getAbilities().containsKey(ShadowAbility.getInstance().getId()))));
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        // The planeswalk-to trigger first gives both players one copy. Chaos then gives
        // only the planar controller an additional copy.
        assertPermanentCount(playerA, "Grizzly Bears", 3);
        assertPermanentCount(playerB, "Hill Giant", 2);
    }

    @Test
    public void registryExposesMetadata() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_THE_BEAN);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals("The Bean", metadata.getEnglishName());
        Assert.assertEquals("Plane - The Bean", metadata.getImageName());
        Assert.assertEquals("PUNK", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }

    private void useBeanAsPlaneswalkDestination() {
        gameOptions.planeChase = true;
        gameOptions.sharedPlanarDeck = Arrays.asList(Planes.PLANE_AKOUM, Planes.PLANE_THE_BEAN);
        skipInitShuffling();
    }
}
