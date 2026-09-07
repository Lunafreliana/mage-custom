package mage.cards.t;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BecomesCybermanEffect;
import mage.abilities.effects.common.continuous.BecomesFaceDownCreatureEffect;
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
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Susucr
 */
public final class TheCyberController extends CardImpl {

    public TheCyberController(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{X}{U}{U}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.CYBERMAN);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // When The Cyber-Controller enters, each opponent mills X cards. Put all creature cards milled this way onto the battlefield face down under your control. They're 2/2 Cyberman artifact creatures.
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
        this.staticText = "each opponent mills X cards. Put all creature cards milled this way "
                + "onto the battlefield face down under your control. They're 2/2 Cyberman artifact creatures";
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
        Cards milledCards = new CardsImpl();
        int xValue = CardUtil.getSourceCostsTag(game, source, "X", 0);
        for (UUID playerId : game.getOpponents(source.getControllerId())) {
            Player opponent = game.getPlayer(playerId);
            if (opponent != null) {
                milledCards.addAll(opponent.millCards(xValue, source, game));
            }
        }
        Set<Card> creatures = milledCards
                .getCards(StaticFilters.FILTER_CARD_CREATURE, game)
                .stream()
                .filter(card -> game.getState().getZone(card.getId()) == Zone.GRAVEYARD)
                .collect(Collectors.toSet());
        for (Card card : creatures) {
            MageObjectReference mor = new MageObjectReference(
                    card.getId(), card.getZoneChangeCounter(game) + 1, game
            );
            game.addEffect(new BecomesFaceDownCreatureEffect(
                    null, mor, Duration.Custom, BecomesFaceDownCreatureEffect.FaceDownType.MANUAL
            ), source);
            game.addEffect(new BecomesCybermanEffect().setTargetPointer(new FixedTarget(mor)), source);
        }
        return creatures.isEmpty()
                || controller.moveCards(creatures, Zone.BATTLEFIELD, source, game, false, true, false, null);
    }
}
