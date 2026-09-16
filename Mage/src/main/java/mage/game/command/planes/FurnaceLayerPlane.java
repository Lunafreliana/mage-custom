package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DestroyTargetEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.Card;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.command.Plane;
import mage.players.Player;
import mage.target.Target;
import mage.target.TargetPlayer;
import mage.target.common.TargetNonlandPermanent;

/**
 * @author The XMage Developers
 */
public final class FurnaceLayerPlane extends Plane {

    public FurnaceLayerPlane() {
        this.setPlaneType(Planes.PLANE_FURNACE_LAYER);

        // When you planeswalk to Furnace Layer and at the beginning of your upkeep, select target
        // player at random. That player discards a card. If that player discards a land card this
        // way, they lose 3 life.
        this.getAbilities().add(createPlaneswalkAbility());
        this.getAbilities().add(createUpkeepAbility());

        // Whenever chaos ensues, you may destroy target nonland permanent.
        Ability ability = new ChaosEnsuesTriggeredAbility(new DestroyTargetEffect(), true);
        ability.addTarget(new TargetNonlandPermanent());
        this.getAbilities().add(ability);
    }

    private static Ability createPlaneswalkAbility() {
        Ability ability = new PlaneswalkToSourceTriggeredAbility(new FurnaceLayerDiscardEffect());
        ability.addTarget(createRandomPlayerTarget());
        return ability;
    }

    private static Ability createUpkeepAbility() {
        Ability ability = new BeginningOfUpkeepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU, new FurnaceLayerDiscardEffect(), false
        );
        ability.addTarget(createRandomPlayerTarget());
        return ability;
    }

    private static Target createRandomPlayerTarget() {
        Target target = new TargetPlayer();
        target.setRandom(true);
        return target;
    }

    private FurnaceLayerPlane(final FurnaceLayerPlane plane) {
        super(plane);
    }

    @Override
    public FurnaceLayerPlane copy() {
        return new FurnaceLayerPlane(this);
    }
}

class FurnaceLayerDiscardEffect extends OneShotEffect {

    FurnaceLayerDiscardEffect() {
        super(Outcome.Discard);
        staticText = "that player discards a card. If that player discards a land card this way, "
                + "they lose 3 life";
    }

    private FurnaceLayerDiscardEffect(final FurnaceLayerDiscardEffect effect) {
        super(effect);
    }

    @Override
    public FurnaceLayerDiscardEffect copy() {
        return new FurnaceLayerDiscardEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (player == null) {
            return false;
        }
        Card discardedCard = player.discardOne(false, false, source, game);
        if (discardedCard != null && discardedCard.isLand(game)) {
            player.loseLife(3, game, source, false);
        }
        return true;
    }
}
