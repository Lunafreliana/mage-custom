package mage.cards.m;

import mage.ApprovingObject;
import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.DealsCombatDamageToAPlayerTriggeredAbility;
import mage.abilities.common.EntersBattlefieldAllTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ComparisonType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.filter.FilterCard;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.common.FilterInstantOrSorceryCard;
import mage.filter.predicate.mageobject.ManaValuePredicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetCardInYourGraveyard;
import mage.target.targetpointer.FixedTarget;

import java.util.UUID;

/**
 * @author muz
 */
public final class MassimoTheMagician extends CardImpl {

    private static final FilterCard filter
            = new FilterInstantOrSorceryCard("instant or sorcery card with mana value 1 from your graveyard");

    static {
        filter.add(new ManaValuePredicate(ComparisonType.EQUAL_TO, 1));
    }

    public MassimoTheMagician(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{U}{R}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.CAT, SubType.WIZARD);
        this.power = new MageInt(3);
        this.toughness = new MageInt(2);

        // Whenever Massimo or another creature you control enters, exile up to one target instant or sorcery card with mana value 1 from your graveyard. If a card is exiled this way, that creature gains "Whenever this creature deals combat damage to a player, copy the exiled card. You may cast the copy without paying its mana cost."
        Ability ability = new MassimoTheMagicianTriggeredAbility(new MassimoTheMagicianExileEffect());
        ability.addTarget(new TargetCardInYourGraveyard(0, 1, filter));
        this.addAbility(ability);
    }

    private MassimoTheMagician(final MassimoTheMagician card) {
        super(card);
    }

    @Override
    public MassimoTheMagician copy() {
        return new MassimoTheMagician(this);
    }
}

class MassimoTheMagicianTriggeredAbility extends EntersBattlefieldAllTriggeredAbility {

    MassimoTheMagicianTriggeredAbility(OneShotEffect effect) {
        super(effect, new FilterControlledCreaturePermanent("Massimo or another creature you control"));
        this.setTriggerPhrase("Whenever Massimo or another creature you control enters, ");
    }

    private MassimoTheMagicianTriggeredAbility(final MassimoTheMagicianTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!super.checkTrigger(event, game)) {
            return false;
        }
        Permanent permanent = game.getPermanent(event.getTargetId());
        this.getAllEffects().setValue("massimoEnteringCreature", new MageObjectReference(permanent, game));
        return true;
    }

    @Override
    public MassimoTheMagicianTriggeredAbility copy() {
        return new MassimoTheMagicianTriggeredAbility(this);
    }
}

class MassimoTheMagicianExileEffect extends OneShotEffect {

    MassimoTheMagicianExileEffect() {
        super(Outcome.Benefit);
        staticText = "exile up to one target instant or sorcery card with mana value 1 from your graveyard. "
                + "If a card is exiled this way, that creature gains \"Whenever this creature deals combat damage "
                + "to a player, copy the exiled card. You may cast the copy without paying its mana cost.\"";
    }

    private MassimoTheMagicianExileEffect(final MassimoTheMagicianExileEffect effect) {
        super(effect);
    }

    @Override
    public MassimoTheMagicianExileEffect copy() {
        return new MassimoTheMagicianExileEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Card card = game.getCard(source.getFirstTarget());
        Player controller = game.getPlayer(source.getControllerId());
        MageObjectReference creatureReference = (MageObjectReference) getValue("massimoEnteringCreature");
        if (card == null || controller == null || creatureReference == null
                || game.getState().getZone(card.getId()) != Zone.GRAVEYARD
                || !controller.getGraveyard().contains(card.getId())) {
            return false;
        }
        if (!controller.moveCardsToExile(card, source, game, true, source.getSourceId(), "Massimo, the Magician")) {
            return false;
        }
        Permanent creature = creatureReference.getPermanent(game);
        if (creature != null) {
            Ability gainedAbility = new DealsCombatDamageToAPlayerTriggeredAbility(
                    new MassimoTheMagicianCopyEffect(card.getId()));
            game.addEffect(new GainAbilityTargetEffect(gainedAbility, Duration.Custom)
                    .setTargetPointer(new FixedTarget(creatureReference)), source);
        }
        return true;
    }
}

class MassimoTheMagicianCopyEffect extends OneShotEffect {

    private final UUID cardId;

    MassimoTheMagicianCopyEffect(UUID cardId) {
        super(Outcome.PlayForFree);
        this.cardId = cardId;
        staticText = "copy the exiled card. You may cast the copy without paying its mana cost";
    }

    private MassimoTheMagicianCopyEffect(final MassimoTheMagicianCopyEffect effect) {
        super(effect);
        this.cardId = effect.cardId;
    }

    @Override
    public MassimoTheMagicianCopyEffect copy() {
        return new MassimoTheMagicianCopyEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Card card = game.getCard(cardId);
        Player controller = game.getPlayer(source.getControllerId());
        if (card == null || controller == null || game.getState().getZone(cardId) != Zone.EXILED) {
            return false;
        }
        Card copy = game.copyCard(card, source, source.getControllerId());
        if (copy == null || !controller.chooseUse(outcome, "Cast the copied card without paying its mana cost?", source, game)) {
            return false;
        }
        game.getState().setValue("PlayFromNotOwnHandZone" + copy.getId(), Boolean.TRUE);
        boolean cast = controller.cast(controller.chooseAbilityForCast(copy, game, true),
                game, true, new ApprovingObject(source, game));
        game.getState().setValue("PlayFromNotOwnHandZone" + copy.getId(), null);
        return cast;
    }
}
