package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.TapForManaAllTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DamageTargetEffect;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.SetTargetPointer;
import mage.constants.Zone;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterLandPermanent;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetSacrifice;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author The XMage Developers
 */
public final class TemberCityPlane extends Plane {

    public TemberCityPlane() {
        this.setPlaneType(Planes.PLANE_TEMBER_CITY);

        // Whenever a player taps a land for mana, Tember City deals 1 damage to that player.
        this.getAbilities().add(new TapForManaAllTriggeredAbility(
                Zone.COMMAND,
                new DamageTargetEffect(1).withTargetDescription("that player"),
                new FilterLandPermanent("a player taps a land"),
                SetTargetPointer.PLAYER
        ));

        // Whenever chaos ensues, each other player sacrifices a nonland permanent of their choice.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(new TemberCityChaosEffect(), false));
    }

    private TemberCityPlane(final TemberCityPlane plane) {
        super(plane);
    }

    @Override
    public TemberCityPlane copy() {
        return new TemberCityPlane(this);
    }
}

class TemberCityChaosEffect extends OneShotEffect {

    private static final FilterPermanent FILTER = new FilterPermanent("a nonland permanent");

    static {
        FILTER.add(Predicates.not(CardType.LAND.getPredicate()));
    }

    TemberCityChaosEffect() {
        super(Outcome.Sacrifice);
        staticText = "each other player sacrifices a nonland permanent of their choice";
    }

    private TemberCityChaosEffect(final TemberCityChaosEffect effect) {
        super(effect);
    }

    @Override
    public TemberCityChaosEffect copy() {
        return new TemberCityChaosEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Set<UUID> permanentsToSacrifice = new HashSet<>();
        for (UUID playerId : game.getState().getPlayersInRange(source.getControllerId(), game)) {
            Player player = game.getPlayer(playerId);
            if (player == null || playerId.equals(source.getControllerId())) {
                continue;
            }
            TargetSacrifice target = new TargetSacrifice(1, FILTER);
            if (target.canChoose(playerId, source, game)) {
                if (target.choose(outcome, playerId, source, game)) {
                    permanentsToSacrifice.add(target.getFirstTarget());
                }
            }
        }
        for (UUID permanentId : permanentsToSacrifice) {
            Permanent permanent = game.getPermanent(permanentId);
            if (permanent != null) {
                permanent.sacrifice(source, game);
            }
        }
        return true;
    }
}
