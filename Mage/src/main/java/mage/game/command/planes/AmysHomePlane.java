package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.counter.TimeTravelEffect;
import mage.abilities.keyword.SuspendAbility;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.Card;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.command.Plane;
import mage.players.Player;
import mage.target.TargetCard;
import mage.target.common.TargetCardInHand;

/**
 * @author The XMage Developers
 */
public final class AmysHomePlane extends Plane {

    public AmysHomePlane() {
        this.setPlaneType(Planes.PLANE_AMYS_HOME);

        // When you planeswalk to Amy's Home and at the beginning of your upkeep, you may exile a nonland card from your hand with a number of time counters on it equal to its mana value. If it doesn't have suspend, it gains suspend.
        this.getAbilities().add(new PlaneswalkToSourceTriggeredAbility(new AmysHomeSuspendEffect()));
        this.getAbilities().add(new BeginningOfUpkeepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU, new AmysHomeSuspendEffect(), false
        ));

        // Whenever chaos ensues, time travel.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(new TimeTravelEffect(), false));
    }

    private AmysHomePlane(final AmysHomePlane plane) {
        super(plane);
    }

    @Override
    public AmysHomePlane copy() {
        return new AmysHomePlane(this);
    }
}

class AmysHomeSuspendEffect extends OneShotEffect {

    AmysHomeSuspendEffect() {
        super(Outcome.Benefit);
        staticText = "you may exile a nonland card from your hand with a number of time counters on it "
                + "equal to its mana value. If it doesn't have suspend, it gains suspend";
    }

    private AmysHomeSuspendEffect(final AmysHomeSuspendEffect effect) {
        super(effect);
    }

    @Override
    public AmysHomeSuspendEffect copy() {
        return new AmysHomeSuspendEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return false;
        }
        TargetCard target = new TargetCardInHand(0, 1, StaticFilters.FILTER_CARD_NON_LAND);
        player.choose(Outcome.PlayForFree, player.getHand(), target, source, game);
        Card card = game.getCard(target.getFirstTarget());
        if (card == null || !player.moveCards(card, Zone.EXILED, source, game)) {
            return false;
        }
        return SuspendAbility.addTimeCountersAndSuspend(card, card.getManaValue(), source, game);
    }
}
