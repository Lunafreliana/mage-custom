package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlaneswalkAwayFromSourceTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.counter.ProliferateEffect;
import mage.abilities.triggers.BeginningOfSecondMainTriggeredAbility;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.SubType;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.permanent.token.PirateToken;
import mage.game.permanent.token.TreasureToken;
import mage.players.Player;
import mage.watchers.common.PlayersDealtCombatDamageThisTurnWatcher;

/**
 * @author The XMage Developers
 */
public final class RaidersAllegiancePlane extends Plane {

    static final FilterControlledCreaturePermanent PIRATE_FILTER
            = new FilterControlledCreaturePermanent(SubType.PIRATE, "Pirate you control");

    public RaidersAllegiancePlane() {
        this.setPlaneType(Planes.PLANE_RAIDERS_ALLEGIANCE);

        // Raid -- At the beginning of your second main phase, for each player dealt combat damage
        // this turn, you get a point counter and create a 2/2 black Pirate creature token with menace.
        Ability ability = new BeginningOfSecondMainTriggeredAbility(
                Zone.COMMAND, TargetController.YOU, new RaidersAllegianceRaidEffect(), false
        );
        this.getAbilities().add(ability);

        // Whenever you planeswalk away from here, each player with the most point counters or tied
        // for the most point counters creates three Treasure tokens and draws three cards. Then remove
        // all point counters from each player.
        this.getAbilities().add(new PlaneswalkAwayFromSourceTriggeredAbility(
                new RaidersAllegianceDepartureEffect()
        ));

        // Whenever chaos ensues, proliferate for each Pirate you control.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(
                new RaidersAllegianceChaosEffect(), false
        ));
    }

    private RaidersAllegiancePlane(final RaidersAllegiancePlane plane) {
        super(plane);
    }

    @Override
    public RaidersAllegiancePlane copy() {
        return new RaidersAllegiancePlane(this);
    }
}

class RaidersAllegianceRaidEffect extends OneShotEffect {

    RaidersAllegianceRaidEffect() {
        super(Outcome.Benefit);
        staticText = "for each player dealt combat damage this turn, you get a point counter "
                + "and create a 2/2 black Pirate creature token with menace";
    }

    private RaidersAllegianceRaidEffect(final RaidersAllegianceRaidEffect effect) {
        super(effect);
    }

    @Override
    public RaidersAllegianceRaidEffect copy() {
        return new RaidersAllegianceRaidEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        PlayersDealtCombatDamageThisTurnWatcher watcher
                = game.getState().getWatcher(PlayersDealtCombatDamageThisTurnWatcher.class);
        if (controller == null || watcher == null) {
            return false;
        }
        int count = watcher.getCount();
        if (count > 0) {
            controller.addCounters(CounterType.POINT.createInstance(count), controller.getId(), source, game);
            new PirateToken().putOntoBattlefield(count, game, source);
        }
        return true;
    }
}

class RaidersAllegianceDepartureEffect extends OneShotEffect {

    RaidersAllegianceDepartureEffect() {
        super(Outcome.Benefit);
        staticText = "each player with the most point counters or tied for the most point counters "
                + "creates three Treasure tokens and draws three cards. Then remove all point counters from each player";
    }

    private RaidersAllegianceDepartureEffect(final RaidersAllegianceDepartureEffect effect) {
        super(effect);
    }

    @Override
    public RaidersAllegianceDepartureEffect copy() {
        return new RaidersAllegianceDepartureEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        int most = game.getPlayers().values().stream()
                .filter(Player::isInGame)
                .mapToInt(player -> player.getCountersCount(CounterType.POINT))
                .max()
                .orElse(0);
        for (Player player : game.getPlayers().values()) {
            if (player.isInGame() && player.getCountersCount(CounterType.POINT) == most) {
                new TreasureToken().putOntoBattlefield(3, game, source, player.getId());
                player.drawCards(3, source, game);
            }
        }
        for (Player player : game.getPlayers().values()) {
            player.loseAllCounters(CounterType.POINT.getName(), source, game);
        }
        return true;
    }
}

class RaidersAllegianceChaosEffect extends OneShotEffect {

    RaidersAllegianceChaosEffect() {
        super(Outcome.Benefit);
        staticText = "proliferate for each Pirate you control";
    }

    private RaidersAllegianceChaosEffect(final RaidersAllegianceChaosEffect effect) {
        super(effect);
    }

    @Override
    public RaidersAllegianceChaosEffect copy() {
        return new RaidersAllegianceChaosEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        int count = game.getBattlefield().count(
                RaidersAllegiancePlane.PIRATE_FILTER, source.getControllerId(), source, game
        );
        for (int i = 0; i < count; i++) {
            new ProliferateEffect(false).apply(game, source);
        }
        return true;
    }
}
