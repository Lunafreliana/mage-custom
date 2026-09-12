package org.mage.test.cards.planes;

import mage.game.GameOptions;
import mage.game.command.PlanarDeckMode;
import mage.game.command.PlaneswalkContext;
import mage.game.command.SharedPlanarDeckSource;
import mage.game.match.MatchOptions;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class PlanechaseModeContextTest {

    @Test
    public void playerFacingOptionsDefaultToIndividualMode() {
        MatchOptions options = new MatchOptions("Planechase", "Freeform", true);

        Assert.assertEquals(PlanarDeckMode.INDIVIDUAL, options.getPlanarDeckMode());
        Assert.assertEquals(SharedPlanarDeckSource.TABLE_CONFIGURED, options.getSharedPlanarDeckSource());
    }

    @Test
    public void gameOptionsCopyPreservesSharedModeAndSource() {
        GameOptions options = new GameOptions();
        options.planarDeckMode = PlanarDeckMode.SHARED;
        options.sharedPlanarDeckSource = SharedPlanarDeckSource.MERGED_PLAYER_CONTRIBUTIONS;

        GameOptions copy = options.copy();

        Assert.assertEquals(PlanarDeckMode.SHARED, copy.planarDeckMode);
        Assert.assertEquals(SharedPlanarDeckSource.MERGED_PLAYER_CONTRIBUTIONS,
                copy.sharedPlanarDeckSource);
    }

    @Test
    public void planeswalkContextKeepsRulesIdentitiesExplicit() {
        UUID planeswalkingPlayerId = UUID.randomUUID();
        UUID sourceId = UUID.randomUUID();
        PlaneswalkContext context = new PlaneswalkContext(planeswalkingPlayerId,
                PlaneswalkContext.Cause.SPELL_OR_ABILITY, sourceId);

        Assert.assertEquals(planeswalkingPlayerId, context.getPlaneswalkingPlayerId());
        Assert.assertEquals(PlaneswalkContext.Cause.SPELL_OR_ABILITY, context.getCause());
        Assert.assertEquals(sourceId, context.getSourceId());
    }
}
