package mage.cards.e;

import mage.MageIdentifier;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.Cost;
import mage.abilities.costs.Costs;
import mage.abilities.costs.CostsImpl;
import mage.abilities.costs.common.PayLifeCost;
import mage.abilities.effects.AsThoughEffectImpl;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.LifelinkAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AsThoughEffectType;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.Game;
import mage.players.Player;
import mage.watchers.common.CardsSurveilledWatcher;

import java.util.UUID;

/**
 * @author OpenAI
 */
public final class EyeOfDuskmantle extends CardImpl {

    public EyeOfDuskmantle(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{5}{B}{B}");

        this.subtype.add(SubType.EYE);
        this.power = new MageInt(3);
        this.toughness = new MageInt(8);

        // Flying, lifelink
        this.addAbility(FlyingAbility.getInstance());
        this.addAbility(LifelinkAbility.getInstance());

        // You may play lands and cast spells from among cards in your graveyard you've surveilled this turn.
        // If you cast a spell this way, you pay life equal to its mana value rather than paying its mana cost.
        this.addAbility(new SimpleStaticAbility(new EyeOfDuskmantleEffect())
                .setIdentifier(MageIdentifier.EyeOfDuskmantleAlternateCast), new CardsSurveilledWatcher());
    }

    private EyeOfDuskmantle(final EyeOfDuskmantle card) {
        super(card);
    }

    @Override
    public EyeOfDuskmantle copy() {
        return new EyeOfDuskmantle(this);
    }
}

class EyeOfDuskmantleEffect extends AsThoughEffectImpl {

    EyeOfDuskmantleEffect() {
        super(AsThoughEffectType.PLAY_FROM_NOT_OWN_HAND_ZONE, Duration.WhileOnBattlefield, Outcome.AIDontUseIt);
        staticText = "You may play lands and cast spells from among cards in your graveyard you've surveilled this turn. "
                + "If you cast a spell this way, you pay life equal to its mana value rather than paying its mana cost.";
    }

    private EyeOfDuskmantleEffect(final EyeOfDuskmantleEffect effect) {
        super(effect);
    }

    @Override
    public EyeOfDuskmantleEffect copy() {
        return new EyeOfDuskmantleEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return true;
    }

    @Override
    public boolean applies(UUID objectId, Ability source, UUID affectedControllerId, Game game) {
        Card card = game.getCard(objectId);
        Player player = game.getPlayer(affectedControllerId);
        CardsSurveilledWatcher watcher = game.getState().getWatcher(CardsSurveilledWatcher.class);
        if (card == null
                || player == null
                || !source.isControlledBy(affectedControllerId)
                || !card.isOwnedBy(affectedControllerId)
                || game.getState().getZone(card.getId()) != Zone.GRAVEYARD
                || watcher == null
                || !watcher.checkCard(card, game)) {
            return false;
        }
        if (!card.isLand(game)) {
            Costs<Cost> costs = new CostsImpl<>();
            costs.add(new PayLifeCost(card.getSpellAbility().getManaCosts().manaValue()));
            costs.addAll(card.getSpellAbility().getCosts());
            player.setCastSourceIdWithAlternateMana(
                    card.getId(), null, costs, MageIdentifier.EyeOfDuskmantleAlternateCast
            );
        }
        return true;
    }
}
