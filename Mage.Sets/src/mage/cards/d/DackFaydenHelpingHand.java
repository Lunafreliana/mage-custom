package mage.cards.d;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.combat.GoadTargetEffect;
import mage.abilities.effects.common.continuous.GainControlTargetEffect;
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
import mage.filter.FilterOpponent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.other.PlayerIdPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPlayer;
import mage.target.targetpointer.FixedTarget;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class DackFaydenHelpingHand extends CardImpl {

    public DackFaydenHelpingHand(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{W}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ADVISOR);
        this.power = new MageInt(4);
        this.toughness = new MageInt(6);

        // When Dack Fayden enters, reveal cards from the top of your library until you reveal X creature cards,
        // where X is the number of opponents you have. Put those creature cards onto the battlefield, then shuffle.
        // They're goaded for the rest of the game. For each of those permanents, choose a different opponent.
        // Each opponent gains control of the permanent for which they were chosen.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new DackFaydenHelpingHandEffect()));
    }

    private DackFaydenHelpingHand(final DackFaydenHelpingHand card) {
        super(card);
    }

    @Override
    public DackFaydenHelpingHand copy() {
        return new DackFaydenHelpingHand(this);
    }
}

class DackFaydenHelpingHandEffect extends OneShotEffect {

    DackFaydenHelpingHandEffect() {
        super(Outcome.PutCreatureInPlay);
        this.staticText = "reveal cards from the top of your library until you reveal X creature cards, "
                + "where X is the number of opponents you have. Put those creature cards onto the battlefield, "
                + "then shuffle. They're goaded for the rest of the game. For each of those permanents, "
                + "choose a different opponent. Each opponent gains control of the permanent for which they were chosen";
    }

    private DackFaydenHelpingHandEffect(final DackFaydenHelpingHandEffect effect) {
        super(effect);
    }

    @Override
    public DackFaydenHelpingHandEffect copy() {
        return new DackFaydenHelpingHandEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }

        int opponentCount = game.getOpponents(controller.getId()).size();
        if (opponentCount == 0) {
            return true;
        }

        Cards revealed = new CardsImpl();
        Set<Card> creatureCards = new LinkedHashSet<>();
        for (Card card : controller.getLibrary().getCards(game)) {
            revealed.add(card);
            if (card.isCreature(game)) {
                creatureCards.add(card);
                if (creatureCards.size() == opponentCount) {
                    break;
                }
            }
        }
        controller.revealCards(source, revealed, game);
        List<UUID> creatureIds = new ArrayList<>();
        creatureCards.forEach(card -> creatureIds.add(card.getId()));
        controller.moveCards(creatureCards, Zone.BATTLEFIELD, source, game, false, false, true, null);
        controller.shuffleLibrary(source, game);

        List<Permanent> permanents = new ArrayList<>();
        for (UUID creatureId : creatureIds) {
            Permanent permanent = game.getPermanent(creatureId);
            if (permanent != null) {
                permanents.add(permanent);
                game.addEffect(new GoadTargetEffect(Duration.EndOfGame)
                        .setTargetPointer(new FixedTarget(permanent, game)), source);
            }
        }

        Set<UUID> chosenOpponents = new LinkedHashSet<>();
        for (Permanent permanent : permanents) {
            Set<UUID> availableOpponents = new LinkedHashSet<>(game.getOpponents(controller.getId()));
            availableOpponents.removeAll(chosenOpponents);
            UUID opponentId;
            if (availableOpponents.size() == 1) {
                opponentId = availableOpponents.iterator().next();
            } else {
                FilterOpponent filter = new FilterOpponent("a different opponent to gain control of "
                        + permanent.getLogName());
                chosenOpponents.forEach(playerId -> filter.add(Predicates.not(new PlayerIdPredicate(playerId))));
                TargetPlayer target = new TargetPlayer(1, 1, true, filter);
                if (!controller.chooseTarget(outcome, target, source, game)) {
                    continue;
                }
                opponentId = target.getFirstTarget();
                if (opponentId == null) {
                    continue;
                }
            }
            chosenOpponents.add(opponentId);
            ContinuousEffect effect = new GainControlTargetEffect(Duration.EndOfGame, opponentId);
            effect.setTargetPointer(new FixedTarget(permanent, game));
            game.addEffect(effect, source);
        }
        return true;
    }
}
