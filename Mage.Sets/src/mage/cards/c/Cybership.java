package mage.cards.c;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.DealsCombatDamageToAPlayerTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BecomesCybermanEffect;
import mage.abilities.keyword.CrewAbility;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.Game;
import mage.players.Player;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class Cybership extends CardImpl {

    public Cybership(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{6}");

        this.subtype.add(SubType.VEHICLE);
        this.power = new MageInt(8);
        this.toughness = new MageInt(8);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Whenever this Vehicle deals combat damage to a player, put the top two cards of that player's library onto the battlefield face down under your control. They're 2/2 Cyberman artifact creatures.
        this.addAbility(new DealsCombatDamageToAPlayerTriggeredAbility(
                new CybershipEffect(), false, true
        ));

        // Crew 4
        this.addAbility(new CrewAbility(4));
    }

    private Cybership(final Cybership card) {
        super(card);
    }

    @Override
    public Cybership copy() {
        return new Cybership(this);
    }
}

class CybershipEffect extends OneShotEffect {

    CybershipEffect() {
        super(Outcome.PutCreatureInPlay);
        this.staticText = "put the top two cards of that player's library onto the battlefield "
                + "face down under your control. They're 2/2 Cyberman artifact creatures";
    }

    private CybershipEffect(final CybershipEffect effect) {
        super(effect);
    }

    @Override
    public CybershipEffect copy() {
        return new CybershipEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Player damagedPlayer = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (controller == null || damagedPlayer == null) {
            return false;
        }
        Cards cards = new CardsImpl(damagedPlayer.getLibrary().getTopCards(game, 2));
        for (Card card : cards.getCards(game)) {
            MageObjectReference mor = new MageObjectReference(
                    card.getId(), card.getZoneChangeCounter(game) + 1, game
            );
            game.addEffect(new BecomesCybermanEffect(mor), source);
        }
        if (!cards.isEmpty()) {
            controller.moveCards(
                    cards.getCards(game), Zone.BATTLEFIELD, source, game,
                    false, true, false, null
            );
        }
        return true;
    }
}
