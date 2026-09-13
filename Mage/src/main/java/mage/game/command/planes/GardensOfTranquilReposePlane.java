package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.DiesCreatureTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Card;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.filter.common.FilterCreaturePermanent;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.DalekToken;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author The XMage Developers
 */
public final class GardensOfTranquilReposePlane extends Plane {

    public GardensOfTranquilReposePlane() {
        this.setPlaneType(Planes.PLANE_GARDENS_OF_TRANQUIL_REPOSE);

        // Suspended Animation — Whenever a creature dies, exile it. Its controller scries 1.
        this.getAbilities().add(new DiesCreatureTriggeredAbility(
                Zone.COMMAND, new GardensOfTranquilReposeDiesEffect(), false,
                new FilterCreaturePermanent("a creature"), false
        ).withFlavorWord("Suspended Animation"));

        // Whenever chaos ensues, create X 3/3 black Dalek artifact creature tokens with menace,
        // where X is one plus the number of cards exiled with Gardens of Tranquil Repose.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(
                new GardensOfTranquilReposeChaosEffect(), false
        ));
    }

    private GardensOfTranquilReposePlane(final GardensOfTranquilReposePlane plane) {
        super(plane);
    }

    @Override
    public GardensOfTranquilReposePlane copy() {
        return new GardensOfTranquilReposePlane(this);
    }
}

class GardensOfTranquilReposeDiesEffect extends OneShotEffect {

    GardensOfTranquilReposeDiesEffect() {
        super(Outcome.Exile);
        staticText = "exile it. Its controller scries 1";
    }

    private GardensOfTranquilReposeDiesEffect(final GardensOfTranquilReposeDiesEffect effect) {
        super(effect);
    }

    @Override
    public GardensOfTranquilReposeDiesEffect copy() {
        return new GardensOfTranquilReposeDiesEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent creature = (Permanent) getValue("creatureDied");
        Player planarController = game.getPlayer(source.getControllerId());
        if (creature == null || planarController == null) {
            return false;
        }

        Card card = game.getCard(creature.getId());
        if (card != null) {
            planarController.moveCardsToExile(
                    card, source, game, true,
                    CardUtil.getExileZoneId(game, source), CardUtil.getSourceName(game, source)
            );
        }

        Player creatureController = game.getPlayer(creature.getControllerId());
        return creatureController != null && creatureController.scry(1, source, game);
    }
}

class GardensOfTranquilReposeChaosEffect extends OneShotEffect {

    GardensOfTranquilReposeChaosEffect() {
        super(Outcome.CreateToken);
        staticText = "create X 3/3 black Dalek artifact creature tokens with menace, where X is one plus "
                + "the number of cards exiled with {this}";
    }

    private GardensOfTranquilReposeChaosEffect(final GardensOfTranquilReposeChaosEffect effect) {
        super(effect);
    }

    @Override
    public GardensOfTranquilReposeChaosEffect copy() {
        return new GardensOfTranquilReposeChaosEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        int amount = 1;
        UUID exileId = CardUtil.getExileZoneId(game, source);
        if (game.getExile().getExileZone(exileId) != null) {
            amount += game.getExile().getExileZone(exileId).size();
        }
        return new DalekToken().putOntoBattlefield(amount, game, source);
    }
}
