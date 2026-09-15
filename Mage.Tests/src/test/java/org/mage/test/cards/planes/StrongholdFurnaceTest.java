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

public class StrongholdFurnaceTest extends CardTestPlayerBase {

    @Test
    public void doublesDamageToPlayersAndPermanents() {
        addPlane(playerA, Planes.PLANE_STRONGHOLD_FURNACE);
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 2);
        addCard(Zone.HAND, playerA, "Twin Bolt");
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Twin Bolt");
        addTargetAmount(playerA, playerB, 1);
        addTargetAmount(playerA, "Hill Giant", 1);
        setStrictChooseMode(true);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 18);
        assertDamageReceived(playerB, "Hill Giant", 2);
    }

    @Test
    public void doublesDamageFromItsOwnChaosAbility() {
        addPlane(playerA, Planes.PLANE_STRONGHOLD_FURNACE);
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        addTarget(playerA, playerB);
        setStrictChooseMode(true);

        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertLife(playerB, 18);
    }

    @Test
    public void isAvailableThroughPlanarCardRegistry() {
        String id = PlanarCardRegistry.getId(Planes.PLANE_STRONGHOLD_FURNACE);
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Stronghold Furnace", metadata.getEnglishName());
        Assert.assertEquals("Plane - Stronghold Furnace", metadata.getImageName());
        Assert.assertEquals("MOC", metadata.getSetCode());
        Assert.assertNotNull(PlanarCardRegistry.create(id));
    }
}
