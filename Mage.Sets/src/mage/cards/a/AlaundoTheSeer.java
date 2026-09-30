package mage.cards.a;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.keyword.HasteAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.players.Player;
import mage.target.TargetCard;
import mage.target.common.TargetCardInHand;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author The XMage Developers
 */
public final class AlaundoTheSeer extends CardImpl {

    public AlaundoTheSeer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN, SubType.SHAMAN);
        this.power = new MageInt(3);
        this.toughness = new MageInt(5);

        // {T}: Draw a card, then exile a card from your hand and put a number of time counters on it equal to its mana value. It gains "When the last time counter is removed from this card, if it's exiled, you may cast it without paying its mana cost. If you cast a creature spell this way, it gains haste until end of turn." Then remove a time counter from each other card you own in exile.
        Ability ability = new SimpleActivatedAbility(new DrawCardSourceControllerEffect(1), new TapSourceCost());
        ability.addEffect(new AlaundoTheSeerEffect());
        this.addAbility(ability);
    }

    private AlaundoTheSeer(final AlaundoTheSeer card) {
        super(card);
    }

    @Override
    public AlaundoTheSeer copy() {
        return new AlaundoTheSeer(this);
    }
}

class AlaundoTheSeerEffect extends OneShotEffect {

    AlaundoTheSeerEffect() {
        super(Outcome.Benefit);
        staticText = "then exile a card from your hand and put a number of time counters on it equal to its mana value. "
                + "It gains \"When the last time counter is removed from this card, if it's exiled, you may cast it "
                + "without paying its mana cost. If you cast a creature spell this way, it gains haste until end of turn.\" "
                + "Then remove a time counter from each other card you own in exile";
    }

    private AlaundoTheSeerEffect(final AlaundoTheSeerEffect effect) {
        super(effect);
    }

    @Override
    public AlaundoTheSeerEffect copy() {
        return new AlaundoTheSeerEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return false;
        }
        TargetCard target = new TargetCardInHand(StaticFilters.FILTER_CARD);
        if (!player.choose(Outcome.Exile, player.getHand(), target, source, game)) {
            return false;
        }
        Card card = game.getCard(target.getFirstTarget());
        if (card == null || !player.moveCards(card, Zone.EXILED, source, game)) {
            return false;
        }
        int manaValue = card.getManaValue();
        if (manaValue > 0) {
            card.addCounters(CounterType.TIME.createInstance(manaValue), player.getId(), source, game);
        }
        game.addEffect(new AlaundoTheSeerGainAbilityEffect(new MageObjectReference(card, game)), source);

        game.getExile().getCardsOwned(game, player.getId()).stream()
                .filter(exiledCard -> !exiledCard.getId().equals(card.getId()))
                .forEach(exiledCard -> exiledCard.removeCounters(
                        CounterType.TIME.getName(), 1, source, game
                ));
        return true;
    }
}

class AlaundoTheSeerGainAbilityEffect extends ContinuousEffectImpl {

    private final MageObjectReference mor;

    AlaundoTheSeerGainAbilityEffect(MageObjectReference mor) {
        super(Duration.Custom, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        this.mor = mor;
    }

    private AlaundoTheSeerGainAbilityEffect(final AlaundoTheSeerGainAbilityEffect effect) {
        super(effect);
        this.mor = effect.mor;
    }

    @Override
    public AlaundoTheSeerGainAbilityEffect copy() {
        return new AlaundoTheSeerGainAbilityEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Card card = mor.getCard(game);
        if (card == null || game.getState().getZone(card.getId()) != Zone.EXILED) {
            discard();
            return true;
        }
        Ability ability = new AlaundoTheSeerTriggeredAbility();
        ability.setSourceId(card.getId());
        ability.setControllerId(card.getOwnerId());
        game.getState().addOtherAbility(card, ability);
        return true;
    }
}

class AlaundoTheSeerTriggeredAbility extends TriggeredAbilityImpl {

    AlaundoTheSeerTriggeredAbility() {
        super(Zone.EXILED, new AlaundoTheSeerCastEffect());
    }

    private AlaundoTheSeerTriggeredAbility(final AlaundoTheSeerTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.COUNTER_REMOVED;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        Card card = game.getCard(getSourceId());
        return event.getTargetId().equals(getSourceId())
                && CounterType.TIME.getName().equals(event.getData())
                && card != null
                && game.getState().getZone(getSourceId()) == Zone.EXILED
                && card.getCounters(game).getCount(CounterType.TIME) == 0;
    }

    @Override
    public String getRule() {
        return "When the last time counter is removed from this card, if it's exiled, ";
    }

    @Override
    public AlaundoTheSeerTriggeredAbility copy() {
        return new AlaundoTheSeerTriggeredAbility(this);
    }
}

class AlaundoTheSeerCastEffect extends OneShotEffect {

    AlaundoTheSeerCastEffect() {
        super(Outcome.PlayForFree);
        staticText = "you may cast it without paying its mana cost. "
                + "If you cast a creature spell this way, it gains haste until end of turn";
    }

    private AlaundoTheSeerCastEffect(final AlaundoTheSeerCastEffect effect) {
        super(effect);
    }

    @Override
    public AlaundoTheSeerCastEffect copy() {
        return new AlaundoTheSeerCastEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        Card card = game.getCard(source.getSourceId());
        if (player == null || card == null) {
            return false;
        }
        card = card.getMainCard();
        if (!CardUtil.castSpellWithAttributesForFree(
                player, source, game, new CardsImpl(card), StaticFilters.FILTER_CARD
        )) {
            return true;
        }
        if (card.isCreature(game)) {
            ContinuousEffect effect = new GainAbilityTargetEffect(
                    HasteAbility.getInstance(), Duration.EndOfTurn, null, true
            );
            effect.setTargetPointer(new FixedTarget(card, game));
            game.addEffect(effect, source);
        }
        return true;
    }
}
