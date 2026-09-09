package mage.cards.r;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.effects.common.continuous.MaximumHandSizeControllerEffect;
import mage.abilities.keyword.ReachAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.PhaseStep;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.players.Player;
import mage.watchers.common.CardsDrawnDuringDrawStepWatcher;
import mage.watchers.common.CardsDrawnThisTurnWatcher;

import java.util.UUID;

/**
 * @author muz
 */
public final class ReedRichardsSmartestMan extends CardImpl {

    public ReedRichardsSmartestMan(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{5}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.SCIENTIST);
        this.subtype.add(SubType.HERO);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // Reach
        this.addAbility(ReachAbility.getInstance());

        // You have no maximum hand size.
        this.addAbility(new SimpleStaticAbility(new MaximumHandSizeControllerEffect(
                Integer.MAX_VALUE, Duration.WhileOnBattlefield,
                MaximumHandSizeControllerEffect.HandSizeModification.SET
        )));

        // The first time you would draw a card each turn except the first card you draw during each of your draw steps, you draw four cards instead.
        this.addAbility(
                new SimpleStaticAbility(new ReedRichardsSmartestManReplacementEffect()),
                new CardsDrawnDuringDrawStepWatcher()
        );
    }

    private ReedRichardsSmartestMan(final ReedRichardsSmartestMan card) {
        super(card);
    }

    @Override
    public ReedRichardsSmartestMan copy() {
        return new ReedRichardsSmartestMan(this);
    }
}

class ReedRichardsSmartestManReplacementEffect extends ReplacementEffectImpl {

    ReedRichardsSmartestManReplacementEffect() {
        super(Duration.WhileOnBattlefield, Outcome.DrawCard);
        staticText = "The first time you would draw a card each turn except the first card you draw "
                + "during each of your draw steps, you draw four cards instead";
    }

    private ReedRichardsSmartestManReplacementEffect(final ReedRichardsSmartestManReplacementEffect effect) {
        super(effect);
    }

    @Override
    public ReedRichardsSmartestManReplacementEffect copy() {
        return new ReedRichardsSmartestManReplacementEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DRAW_CARD;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        if (!source.isControlledBy(event.getPlayerId())) {
            return false;
        }

        CardsDrawnDuringDrawStepWatcher drawStepWatcher
                = game.getState().getWatcher(CardsDrawnDuringDrawStepWatcher.class);
        int cardsDrawnDuringDrawStep = drawStepWatcher == null
                ? 0
                : drawStepWatcher.getAmountCardsDrawn(event.getPlayerId());

        // The first draw of each of the player's draw steps is exempt even if an
        // earlier draw this turn has already used this replacement effect.
        if (game.isActivePlayer(event.getPlayerId())
                && game.getPhase().getStep().getType() == PhaseStep.DRAW
                && cardsDrawnDuringDrawStep == 0) {
            return false;
        }

        CardsDrawnThisTurnWatcher turnWatcher
                = game.getState().getWatcher(CardsDrawnThisTurnWatcher.class);
        int cardsDrawnThisTurn = turnWatcher == null
                ? 0
                : turnWatcher.getCardsDrawnThisTurn(event.getPlayerId());

        // Of the cards already drawn this turn, at most one was the exempt draw.
        return cardsDrawnThisTurn - Math.min(cardsDrawnDuringDrawStep, 1) == 0;
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Player player = game.getPlayer(event.getPlayerId());
        if (player != null) {
            player.drawCards(4, source, game, event);
        }
        return true;
    }
}
