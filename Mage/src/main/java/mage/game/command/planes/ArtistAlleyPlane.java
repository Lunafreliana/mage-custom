package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ExileTopXMayPlayUntilEffect;
import mage.abilities.effects.common.MayCastTargetCardEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.Card;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.CastManaAdjustment;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.command.Plane;
import mage.players.Player;
import mage.target.TargetCard;
import mage.target.common.TargetCardInExile;
import mage.target.common.TargetOpponent;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

import java.util.Set;
import java.util.UUID;

/**
 * @author The XMage Developers
 */
public final class ArtistAlleyPlane extends Plane {

    public ArtistAlleyPlane() {
        this.setPlaneType(Planes.PLANE_ARTIST_ALLEY);

        // Whenever you planeswalk here and at the beginning of your upkeep, exile the top card of
        // your library. You may play that card this turn.
        ExileTopXMayPlayUntilEffect exileAndPlay = new ExileTopXMayPlayUntilEffect(1, Duration.EndOfTurn);
        this.getAbilities().add(new PlaneswalkToSourceTriggeredAbility(exileAndPlay));
        this.getAbilities().add(new BeginningOfUpkeepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU, exileAndPlay.copy(), false
        ));

        // Whenever chaos ensues, exile the top six cards of your library. An opponent or player
        // outside the game chooses a nonland card with their favorite art. Then you may cast that
        // spell without paying its mana cost.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(new ArtistAlleyChaosEffect(), false));
    }

    private ArtistAlleyPlane(final ArtistAlleyPlane plane) {
        super(plane);
    }

    @Override
    public ArtistAlleyPlane copy() {
        return new ArtistAlleyPlane(this);
    }
}

class ArtistAlleyChaosEffect extends OneShotEffect {

    ArtistAlleyChaosEffect() {
        super(Outcome.PlayForFree);
        staticText = "exile the top six cards of your library. An opponent or player outside the game "
                + "chooses a nonland card with their favorite art. Then you may cast that spell "
                + "without paying its mana cost";
    }

    private ArtistAlleyChaosEffect(final ArtistAlleyChaosEffect effect) {
        super(effect);
    }

    @Override
    public ArtistAlleyChaosEffect copy() {
        return new ArtistAlleyChaosEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Cards cards = new CardsImpl(controller.getLibrary().getTopCards(game, 6));
        if (cards.isEmpty()) {
            return true;
        }
        UUID exileId = CardUtil.getExileZoneId(game, source);
        controller.moveCardsToExile(cards.getCards(game), source, game, true, exileId, CardUtil.getSourceName(game, source));
        cards.retainZone(Zone.EXILED, game);
        cards.removeIf(cardId -> {
            Card card = game.getCard(cardId);
            return card == null || card.isLand(game);
        });
        if (cards.isEmpty()) {
            return true;
        }

        Player chooser = getOpponent(controller, source, game);
        if (chooser == null) {
            return true;
        }
        TargetCard target = new TargetCardInExile(StaticFilters.FILTER_CARD_NON_LAND);
        target.withNotTarget(true);
        if (!chooser.choose(Outcome.Neutral, cards, target, source, game)) {
            return true;
        }
        Card chosenCard = game.getCard(target.getFirstTarget());
        if (chosenCard == null) {
            return true;
        }
        MayCastTargetCardEffect effect = new MayCastTargetCardEffect(CastManaAdjustment.WITHOUT_PAYING_MANA_COST);
        effect.setTargetPointer(new FixedTarget(chosenCard, game));
        effect.apply(game, source);
        return true;
    }

    private static Player getOpponent(Player controller, Ability source, Game game) {
        Set<UUID> opponents = game.getOpponents(controller.getId());
        if (opponents.size() == 1) {
            return game.getPlayer(opponents.iterator().next());
        }
        TargetOpponent target = new TargetOpponent(true);
        if (!controller.choose(Outcome.Neutral, target, source, game)) {
            return null;
        }
        return game.getPlayer(target.getFirstTarget());
    }
}
