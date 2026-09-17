package mage.cards.n;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.GainLifeControllerTriggeredAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.Cost;
import mage.abilities.costs.common.PayLifeCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.dynamicvalue.common.SavedGainedLifeValue;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.GainLifeEffect;
import mage.abilities.effects.common.LoseLifeOpponentsEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.game.Game;
import mage.players.Player;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class NivMizzetGhostCounsel extends CardImpl {

    public NivMizzetGhostCounsel(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{W}{W}{B}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.SPIRIT);
        this.subtype.add(SubType.DRAGON);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Whenever you gain life, you may pay that much life. If you do, draw that many cards.
        this.addAbility(new GainLifeControllerTriggeredAbility(new NivMizzetGhostCounselEffect()));

        // {T}: Each opponent loses 1 life and you gain 1 life.
        Ability ability = new SimpleActivatedAbility(new LoseLifeOpponentsEffect(1), new TapSourceCost());
        ability.addEffect(new GainLifeEffect(1).concatBy("and"));
        this.addAbility(ability);
    }

    private NivMizzetGhostCounsel(final NivMizzetGhostCounsel card) {
        super(card);
    }

    @Override
    public NivMizzetGhostCounsel copy() {
        return new NivMizzetGhostCounsel(this);
    }
}

class NivMizzetGhostCounselEffect extends OneShotEffect {

    NivMizzetGhostCounselEffect() {
        super(Outcome.DrawCard);
        staticText = "you may pay that much life. If you do, draw that many cards";
    }

    private NivMizzetGhostCounselEffect(final NivMizzetGhostCounselEffect effect) {
        super(effect);
    }

    @Override
    public NivMizzetGhostCounselEffect copy() {
        return new NivMizzetGhostCounselEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        int amount = SavedGainedLifeValue.MANY.calculate(game, source, this);
        Cost cost = new PayLifeCost(amount);
        if (!cost.canPay(source, source, controller.getId(), game)
                || !controller.chooseUse(
                        Outcome.DrawCard, "Pay " + amount + " life to draw " + amount + " cards?", source, game
                )
                || !cost.pay(source, game, source, controller.getId(), true)) {
            return true;
        }
        controller.drawCards(amount, source, game);
        return true;
    }
}
