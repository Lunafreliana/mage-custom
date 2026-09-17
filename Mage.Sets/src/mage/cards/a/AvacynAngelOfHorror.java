package mage.cards.a;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.DelayedTriggeredAbility;
import mage.abilities.common.DiesThisOrAnotherTriggeredAbility;
import mage.abilities.common.delayed.AtTheBeginOfNextEndStepDelayedTriggeredAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ReturnToBattlefieldUnderYourControlTargetEffect;
import mage.abilities.keyword.DeathtouchAbility;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.permanent.TokenPredicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.target.targetpointer.FixedTarget;

import java.util.UUID;

/**
 * @author muz
 */
public final class AvacynAngelOfHorror extends CardImpl {

    private static final FilterControlledCreaturePermanent filter
            = new FilterControlledCreaturePermanent("nontoken creature you control");

    static {
        filter.add(TokenPredicate.FALSE);
    }

    public AvacynAngelOfHorror(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{5}{B}{B}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ANGEL);
        this.power = new MageInt(8);
        this.toughness = new MageInt(8);

        // Flying, deathtouch
        this.addAbility(FlyingAbility.getInstance());
        this.addAbility(DeathtouchAbility.getInstance());

        // Whenever Avacyn or another nontoken creature you control dies, return that card to the battlefield under your control at the beginning of the next end step.
        this.addAbility(new AvacynAngelOfHorrorTriggeredAbility(filter));
    }

    private AvacynAngelOfHorror(final AvacynAngelOfHorror card) {
        super(card);
    }

    @Override
    public AvacynAngelOfHorror copy() {
        return new AvacynAngelOfHorror(this);
    }
}

class AvacynAngelOfHorrorTriggeredAbility extends DiesThisOrAnotherTriggeredAbility {

    AvacynAngelOfHorrorTriggeredAbility(FilterPermanent filter) {
        super(new AvacynAngelOfHorrorEffect(), false, filter);
    }

    private AvacynAngelOfHorrorTriggeredAbility(final AvacynAngelOfHorrorTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public AvacynAngelOfHorrorTriggeredAbility copy() {
        return new AvacynAngelOfHorrorTriggeredAbility(this);
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!super.checkTrigger(event, game)) {
            return false;
        }
        ZoneChangeEvent zEvent = (ZoneChangeEvent) event;
        this.getAllEffects().setTargetPointer(new FixedTarget(
                zEvent.getTarget().getId(), zEvent.getTarget().getZoneChangeCounter(game) + 1
        ));
        return true;
    }
}

class AvacynAngelOfHorrorEffect extends OneShotEffect {

    AvacynAngelOfHorrorEffect() {
        super(Outcome.PutCreatureInPlay);
        staticText = "return that card to the battlefield under your control at the beginning of the next end step";
    }

    private AvacynAngelOfHorrorEffect(final AvacynAngelOfHorrorEffect effect) {
        super(effect);
    }

    @Override
    public AvacynAngelOfHorrorEffect copy() {
        return new AvacynAngelOfHorrorEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Card card = game.getCard(getTargetPointer().getFirst(game, source));
        if (card == null) {
            return false;
        }
        Effect effect = new ReturnToBattlefieldUnderYourControlTargetEffect();
        effect.setTargetPointer(getTargetPointer().copy());
        DelayedTriggeredAbility delayedAbility = new AtTheBeginOfNextEndStepDelayedTriggeredAbility(effect);
        game.addDelayedTriggeredAbility(delayedAbility, source);
        return true;
    }
}
