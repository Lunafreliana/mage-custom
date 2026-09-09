package mage.cards.d;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.SagaAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BecomesCybermanEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SagaChapter;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.ExileZone;
import mage.game.Game;
import mage.players.Player;
import mage.target.TargetPlayer;
import mage.util.CardUtil;

import java.util.UUID;

public final class DeathInHeaven extends CardImpl {

    public DeathInHeaven(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{B}");

        this.subtype.add(SubType.SAGA);

        // (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
        SagaAbility sagaAbility = new SagaAbility(this);

        // I, II -- Target player mills two cards, then exiles their graveyard.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_I, SagaChapter.CHAPTER_II,
                new DeathInHeavenMillEffect(), new TargetPlayer()
        );

        // III -- Put all creature cards exiled with this enchantment onto the battlefield face down
        // under your control. They're 2/2 Cyberman artifact creatures.
        sagaAbility.addChapterEffect(this, SagaChapter.CHAPTER_III, new DeathInHeavenReturnEffect());
        this.addAbility(sagaAbility);
    }

    private DeathInHeaven(final DeathInHeaven card) {
        super(card);
    }

    @Override
    public DeathInHeaven copy() {
        return new DeathInHeaven(this);
    }
}

class DeathInHeavenMillEffect extends OneShotEffect {

    DeathInHeavenMillEffect() {
        super(Outcome.Exile);
        staticText = "target player mills two cards, then exiles their graveyard";
    }

    private DeathInHeavenMillEffect(final DeathInHeavenMillEffect effect) {
        super(effect);
    }

    @Override
    public DeathInHeavenMillEffect copy() {
        return new DeathInHeavenMillEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (player == null) {
            return false;
        }
        player.millCards(2, source, game);
        return player.moveCardsToExile(
                player.getGraveyard().getCards(game), source, game, true,
                CardUtil.getExileZoneId(game, source), CardUtil.getSourceName(game, source)
        );
    }
}

class DeathInHeavenReturnEffect extends OneShotEffect {

    DeathInHeavenReturnEffect() {
        super(Outcome.PutCreatureInPlay);
        staticText = "put all creature cards exiled with this enchantment onto the battlefield face down " +
                "under your control. They're 2/2 Cyberman artifact creatures";
    }

    private DeathInHeavenReturnEffect(final DeathInHeavenReturnEffect effect) {
        super(effect);
    }

    @Override
    public DeathInHeavenReturnEffect copy() {
        return new DeathInHeavenReturnEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        ExileZone exileZone = game.getExile().getExileZone(CardUtil.getExileZoneId(game, source));
        if (controller == null || exileZone == null) {
            return false;
        }
        Cards creatureCards = new CardsImpl(exileZone.getCards(StaticFilters.FILTER_CARD_CREATURE, game));
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
