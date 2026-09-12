package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ReturnToBattlefieldUnderOwnerControlTargetEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.Card;
import mage.cards.CardsImpl;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.command.Plane;
import mage.players.Player;
import mage.target.common.TargetCardInYourGraveyard;

/**
 * @author The XMage Developers
 */
public class TheMaelstromPlane extends Plane {

    public TheMaelstromPlane() {
        this.setPlaneType(Planes.PLANE_THE_MAELSTROM);

        // When you planeswalk to The Maelstrom and at the beginning of your upkeep, you may reveal the top card of your library.
        // If a permanent card is revealed this way, you may put it onto the battlefield.
        // If you revealed a card but didn't put it onto the battlefield, put it on the bottom of your library.
        this.getAbilities().add(new PlaneswalkToSourceTriggeredAbility(new TheMaelstromRevealEffect()));
        this.getAbilities().add(new BeginningOfUpkeepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU, new TheMaelstromRevealEffect(), false
        ));

        // Whenever chaos ensues, return target permanent card from your graveyard to the battlefield.
        Ability chaosAbility = new ChaosEnsuesTriggeredAbility(
                new ReturnToBattlefieldUnderOwnerControlTargetEffect(false, false), false
        );
        chaosAbility.addTarget(new TargetCardInYourGraveyard(StaticFilters.FILTER_CARD_PERMANENT));
        this.getAbilities().add(chaosAbility);
    }

    private TheMaelstromPlane(final TheMaelstromPlane plane) {
        super(plane);
    }

    @Override
    public TheMaelstromPlane copy() {
        return new TheMaelstromPlane(this);
    }
}

class TheMaelstromRevealEffect extends OneShotEffect {

    TheMaelstromRevealEffect() {
        super(Outcome.PutCardInPlay);
        staticText = "you may reveal the top card of your library. If a permanent card is revealed this way, "
                + "you may put it onto the battlefield. If you revealed a card but didn't put it onto the "
                + "battlefield, put it on the bottom of your library";
    }

    private TheMaelstromRevealEffect(final TheMaelstromRevealEffect effect) {
        super(effect);
    }

    @Override
    public TheMaelstromRevealEffect copy() {
        return new TheMaelstromRevealEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null || !controller.getLibrary().hasCards()) {
            return false;
        }
        Card card = controller.getLibrary().getFromTop(game);
        if (card == null || !controller.chooseUse(
                Outcome.Neutral, "Reveal the top card of your library?", source, game
        )) {
            return false;
        }
        controller.revealCards(source, new CardsImpl(card), game);
        if (card.isPermanent(game)
                && controller.chooseUse(Outcome.PutCardInPlay,
                "Put " + card.getIdName() + " onto the battlefield?", source, game)
                && controller.moveCards(card, Zone.BATTLEFIELD, source, game)) {
            return true;
        }
        return controller.putCardsOnBottomOfLibrary(card, game, source);
    }
}
