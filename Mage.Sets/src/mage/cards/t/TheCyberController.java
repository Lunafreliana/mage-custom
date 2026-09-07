package mage.cards.t;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.GetXValue;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BecomesCybermanEffect;
import mage.abilities.effects.common.continuous.BoostControlledEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.players.Player;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class TheCyberController extends CardImpl {

    public TheCyberController(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{X}{U}{U}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.CYBERMAN);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // When The Cyber-Controller enters the battlefield, each opponent mills X cards. Put all creature cards milled this way onto the battlefield face down under your control. They're 2/2 Cyberman artifact creatures.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new TheCyberControllerEffect()));

        // Other artifact creatures you control get +1/+1.
        this.addAbility(new SimpleStaticAbility(new BoostControlledEffect(
                1, 1, Duration.WhileOnBattlefield,
                StaticFilters.FILTER_PERMANENTS_ARTIFACT_CREATURE, true
        )));
    }

    private TheCyberController(final TheCyberController card) {
        super(card);
    }

    @Override
    public TheCyberController copy() {
        return new TheCyberController(this);
    }
}

class TheCyberControllerEffect extends OneShotEffect {

    TheCyberControllerEffect() {
        super(Outcome.PutCreatureInPlay);
        staticText = "each opponent mills X cards. Put all creature cards milled this way " +
                "onto the battlefield face down under your control. They're 2/2 Cyberman artifact creatures";
    }

    private TheCyberControllerEffect(final TheCyberControllerEffect effect) {
        super(effect);
    }

    @Override
    public TheCyberControllerEffect copy() {
        return new TheCyberControllerEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        int xValue = GetXValue.instance.calculate(game, source, this);
        Cards creatureCards = new CardsImpl();
        for (UUID opponentId : game.getOpponents(source.getControllerId())) {
            Player opponent = game.getPlayer(opponentId);
            if (opponent != null) {
                creatureCards.addAllCards(opponent
                        .millCards(xValue, source, game)
                        .getCards(StaticFilters.FILTER_CARD_CREATURE, game));
            }
        }
        for (Card card : creatureCards.getCards(game)) {
            MageObjectReference mor = new MageObjectReference(
                    card.getId(), card.getZoneChangeCounter(game) + 1, game
            );
            game.addEffect(new BecomesCybermanEffect(mor), source);
        }
        if (!creatureCards.isEmpty()) {
            controller.moveCards(
                    creatureCards.getCards(game), Zone.BATTLEFIELD, source, game,
                    false, true, false, null
            );
        }
        return true;
    }
}
