package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlanarDieRolledTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.continuous.MaximumHandSizeControllerEffect;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.command.Plane;

/**
 * @author TheElk801
 */
public final class StairsToInfinityPlane extends Plane {

    public StairsToInfinityPlane() {
        this.setPlaneType(Planes.PLANE_STAIRS_TO_INFINITY);

        // Players have no maximum hand size.
        this.getAbilities().add(new SimpleStaticAbility(Zone.COMMAND, new MaximumHandSizeControllerEffect(
                Integer.MAX_VALUE, Duration.WhileOnBattlefield,
                MaximumHandSizeControllerEffect.HandSizeModification.SET, TargetController.ANY
        )));

        // Whenever you roll the planar die, draw a card.
        this.getAbilities().add(new PlanarDieRolledTriggeredAbility(new DrawCardSourceControllerEffect(1)));

        // Whenever chaos ensues, reveal the top card of your planar deck.
        // You may put it on the bottom of your planar deck.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(new StairsToInfinityEffect(), false));
    }

    private StairsToInfinityPlane(final StairsToInfinityPlane plane) {
        super(plane);
    }

    @Override
    public StairsToInfinityPlane copy() {
        return new StairsToInfinityPlane(this);
    }
}

class StairsToInfinityEffect extends OneShotEffect {

    StairsToInfinityEffect() {
        super(Outcome.Neutral);
        staticText = "reveal the top card of your planar deck. You may put it on the bottom of your planar deck";
    }

    private StairsToInfinityEffect(final StairsToInfinityEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return game.revealTopPlanarCard(source.getControllerId(), true, source);
    }

    @Override
    public StairsToInfinityEffect copy() {
        return new StairsToInfinityEffect(this);
    }
}
