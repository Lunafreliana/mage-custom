package mage.cards.l;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.DiscardCardCost;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.DoIfCostPaid;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.HasteAbility;
import mage.abilities.keyword.MiracleAbility;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.TargetController;
import mage.constants.WatcherScope;
import mage.filter.common.FilterInstantOrSorceryCard;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.players.Player;
import mage.watchers.Watcher;
import mage.watchers.common.CardsDrawnThisTurnWatcher;
import mage.watchers.common.MiracleWatcher;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class LoreholdTheHistorian extends CardImpl {

    public LoreholdTheHistorian(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{R}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ELDER);
        this.subtype.add(SubType.DRAGON);
        this.power = new MageInt(5);
        this.toughness = new MageInt(5);

        // Flying, haste
        this.addAbility(FlyingAbility.getInstance());
        this.addAbility(HasteAbility.getInstance());

        // Each instant and sorcery card in your hand has miracle {2}.
        this.addAbility(
                new SimpleStaticAbility(new LoreholdTheHistorianEffect()),
                new LoreholdTheHistorianWatcher()
        );

        // At the beginning of each opponent's upkeep, you may discard a card. If you do, draw a card.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(
                TargetController.OPPONENT,
                new DoIfCostPaid(new DrawCardSourceControllerEffect(1), new DiscardCardCost()),
                false
        ));
    }

    private LoreholdTheHistorian(final LoreholdTheHistorian card) {
        super(card);
    }

    @Override
    public LoreholdTheHistorian copy() {
        return new LoreholdTheHistorian(this);
    }
}

class LoreholdTheHistorianEffect extends ContinuousEffectImpl {

    static final FilterInstantOrSorceryCard filter
            = new FilterInstantOrSorceryCard("instant and sorcery cards");

    LoreholdTheHistorianEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        this.staticText = "each instant and sorcery card in your hand has miracle {2}";
    }

    private LoreholdTheHistorianEffect(final LoreholdTheHistorianEffect effect) {
        super(effect);
    }

    @Override
    public LoreholdTheHistorianEffect copy() {
        return new LoreholdTheHistorianEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        for (Card card : controller.getHand().getCards(filter, game)) {
            game.getState().addOtherAbility(card, new MiracleAbility("{2}", false));
        }
        return true;
    }
}

class LoreholdTheHistorianWatcher extends Watcher {

    LoreholdTheHistorianWatcher() {
        super(WatcherScope.CARD);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (game.getPhase() == null || event.getType() != GameEvent.EventType.DREW_CARD) {
            return;
        }
        UUID playerId = event.getPlayerId();
        CardsDrawnThisTurnWatcher watcher = game.getState().getWatcher(CardsDrawnThisTurnWatcher.class);
        if (playerId == null || watcher == null || watcher.getCardsDrawnThisTurn(playerId) != 1) {
            return;
        }
        Card card = game.getCard(event.getTargetId());
        if (game.getPermanent(getSourceId()) != null
                && playerId.equals(getControllerId())
                && card != null
                && LoreholdTheHistorianEffect.filter.match(card, getControllerId(), null, game)) {
            if (card.getAbilities(game).stream().anyMatch(MiracleAbility.class::isInstance)) {
                return;
            }
            game.getState().addOtherAbility(card, new MiracleAbility("{2}", false));
            MiracleWatcher.checkMiracleAbility(event, game);
        }
    }
}
