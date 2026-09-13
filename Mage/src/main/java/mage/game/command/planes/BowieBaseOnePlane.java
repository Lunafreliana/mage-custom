package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.effects.common.combat.GoadTargetEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.keyword.IslandwalkAbility;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.ObjectSourcePlayerPredicate;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.players.PlayerList;
import mage.target.TargetPermanent;
import mage.target.common.TargetCreaturePermanent;

/**
 * @author The XMage Developers
 */
public final class BowieBaseOnePlane extends Plane {

    private static final FilterCreaturePermanent filter
            = new FilterCreaturePermanent("creature controlled by the player to your left");

    static {
        filter.add(BowieBaseOneControllerPredicate.instance);
    }

    public BowieBaseOnePlane() {
        this.setPlaneType(Planes.PLANE_BOWIE_BASE_ONE);

        // At the beginning of your end step, goad target creature controlled by the player to your left.
        Ability ability = new BeginningOfEndStepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU, new GoadTargetEffect(), false, null
        );
        ability.addTarget(new TargetPermanent(filter));
        this.getAbilities().add(ability);

        // Whenever chaos ensues, target creature gains islandwalk until end of turn.
        ability = new ChaosEnsuesTriggeredAbility(
                new GainAbilityTargetEffect(new IslandwalkAbility(false)), false
        );
        ability.addTarget(new TargetCreaturePermanent());
        this.getAbilities().add(ability);
    }

    private BowieBaseOnePlane(final BowieBaseOnePlane plane) {
        super(plane);
    }

    @Override
    public BowieBaseOnePlane copy() {
        return new BowieBaseOnePlane(this);
    }
}

enum BowieBaseOneControllerPredicate implements ObjectSourcePlayerPredicate<Permanent> {
    instance;

    @Override
    public boolean apply(ObjectSourcePlayer<Permanent> input, Game game) {
        Player controller = game.getPlayer(input.getPlayerId());
        if (controller == null) {
            return false;
        }
        PlayerList players = game.getPlayerList().copy();
        if (!players.setCurrent(controller.getId())) {
            return false;
        }
        // PlayerList's underlying next seat is always to the left; its public navigation methods
        // account for a reversed turn order, so select the method that still follows that seat order.
        Player playerToLeft = game.isTurnOrderReversed()
                ? players.getPrevious(game)
                : players.getNext(game, false);
        return playerToLeft != null && input.getObject().isControlledBy(playerToLeft.getId());
    }
}
