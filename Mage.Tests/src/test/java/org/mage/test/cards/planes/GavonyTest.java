package org.mage.test.cards.planes;

import mage.abilities.SpellAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ChaosEnsuesEffect;
import mage.abilities.keyword.IndestructibleAbility;
import mage.abilities.keyword.VigilanceAbility;
import mage.constants.CardType;
import mage.constants.PhaseStep;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.command.PlanarCardRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class GavonyTest extends CardTestPlayerBase {

    @Test
    public void allCreaturesHaveVigilance() {
        addPlane(playerA, Planes.PLANE_GAVONY);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertAbility(playerA, "Grizzly Bears", VigilanceAbility.getInstance(), true);
        assertAbility(playerB, "Hill Giant", VigilanceAbility.getInstance(), true);
    }

    @Test
    public void chaosGrantsIndestructibleOnlyToPlanarControllersCreaturesUntilEndOfTurn() {
        addPlane(playerA, Planes.PLANE_GAVONY);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerB, "Hill Giant");
        SpellAbility causeChaos = new SpellAbility(new ManaCostsImpl<>("{0}"), "Cause Chaos");
        causeChaos.addEffect(new ChaosEnsuesEffect());
        addCustomCardWithSpell(playerA, causeChaos, null, CardType.SORCERY);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cause Chaos");
        checkAbility("controller's creature gains indestructible", 1, PhaseStep.POSTCOMBAT_MAIN,
                playerA, "Grizzly Bears", IndestructibleAbility.class, true);
        checkAbility("opponent's creature does not gain indestructible", 1, PhaseStep.POSTCOMBAT_MAIN,
                playerB, "Hill Giant", IndestructibleAbility.class, false);
        checkAbility("indestructible expires", 2, PhaseStep.UPKEEP,
                playerA, "Grizzly Bears", IndestructibleAbility.class, false);

        setStopAt(2, PhaseStep.UPKEEP);
        execute();
    }

    @Test
    public void registryExposesGavonyMetadata() {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(
                PlanarCardRegistry.getId(Planes.PLANE_GAVONY)
        );

        Assert.assertNotNull(metadata);
        Assert.assertEquals("Gavony", metadata.getEnglishName());
        Assert.assertEquals("Plane - Gavony", metadata.getImageName());
        Assert.assertNotNull(PlanarCardRegistry.create(metadata.getId()));
    }
}
