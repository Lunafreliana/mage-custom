package mage.game.command.planes;

import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SpellCastAllTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DrawCardTargetEffect;
import mage.cards.Card;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.SetTargetPointer;
import mage.constants.Zone;
import mage.filter.FilterCard;
import mage.filter.StaticFilters;
import mage.filter.predicate.mageobject.ColorPredicate;
import mage.game.Game;
import mage.game.command.Plane;
import mage.players.Player;
import mage.target.TargetCard;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class MinamoPlane extends Plane {

    public MinamoPlane() {
        this.setPlaneType(Planes.PLANE_MINAMO);

        // Whenever a player casts a spell, that player may draw a card.
        this.getAbilities().add(new SpellCastAllTriggeredAbility(
                Zone.COMMAND, new DrawCardTargetEffect(1, true), StaticFilters.FILTER_SPELL_A,
                false, SetTargetPointer.PLAYER
        ));

        // Whenever chaos ensues, each player may return a blue card from their graveyard to their hand.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(new MinamoChaosEffect(), false));
    }

    private MinamoPlane(final MinamoPlane plane) {
        super(plane);
    }

    @Override
    public MinamoPlane copy() {
        return new MinamoPlane(this);
    }
}

class MinamoChaosEffect extends OneShotEffect {

    private static final FilterCard filter = new FilterCard("a blue card from your graveyard");

    static {
        filter.add(new ColorPredicate(ObjectColor.BLUE));
    }

    MinamoChaosEffect() {
        super(Outcome.ReturnToHand);
        staticText = "each player may return a blue card from their graveyard to their hand";
    }

    private MinamoChaosEffect(final MinamoChaosEffect effect) {
        super(effect);
    }

    @Override
    public MinamoChaosEffect copy() {
        return new MinamoChaosEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Cards cardsToHand = new CardsImpl();
        for (UUID playerId : game.getState().getPlayersInRange(controller.getId(), game)) {
            Player player = game.getPlayer(playerId);
            if (player == null) {
                continue;
            }
            TargetCard target = new TargetCardInYourGraveyard(0, 1, filter, true);
            if (player.choose(outcome, target, source, game)) {
                Card card = game.getCard(target.getFirstTarget());
                if (card != null) {
                    cardsToHand.add(card);
                }
            }
        }
        return controller.moveCards(cardsToHand, Zone.HAND, source, game);
    }
}
