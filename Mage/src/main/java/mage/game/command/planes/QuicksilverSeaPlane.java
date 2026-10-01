package mage.game.command.planes;

import mage.ApprovingObject;
import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.keyword.ScryEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.Card;
import mage.cards.CardsImpl;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.command.Plane;
import mage.players.Player;

/**
 * @author The XMage Developers
 */
public final class QuicksilverSeaPlane extends Plane {

    public QuicksilverSeaPlane() {
        this.setPlaneType(Planes.PLANE_QUICKSILVER_SEA);

        // When you planeswalk to Quicksilver Sea and at the beginning of your upkeep, scry 4.
        this.getAbilities().add(new PlaneswalkToSourceTriggeredAbility(new ScryEffect(4)));
        this.getAbilities().add(new BeginningOfUpkeepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU, new ScryEffect(4), false
        ));

        // Whenever chaos ensues, reveal the top card of your library. You may play it without paying its mana cost.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(new QuicksilverSeaChaosEffect(), false));
    }

    private QuicksilverSeaPlane(final QuicksilverSeaPlane plane) {
        super(plane);
    }

    @Override
    public QuicksilverSeaPlane copy() {
        return new QuicksilverSeaPlane(this);
    }
}

class QuicksilverSeaChaosEffect extends OneShotEffect {

    QuicksilverSeaChaosEffect() {
        super(Outcome.PlayForFree);
        staticText = "reveal the top card of your library. You may play it without paying its mana cost";
    }

    private QuicksilverSeaChaosEffect(final QuicksilverSeaChaosEffect effect) {
        super(effect);
    }

    @Override
    public QuicksilverSeaChaosEffect copy() {
        return new QuicksilverSeaChaosEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null || !controller.getLibrary().hasCards()) {
            return false;
        }
        Card card = controller.getLibrary().getFromTop(game);
        if (card == null) {
            return false;
        }
        controller.revealCards(source, new CardsImpl(card), game);
        if (controller.chooseUse(
                Outcome.PlayForFree,
                "Play " + card.getLogName() + " without paying its mana cost?",
                source,
                game
        )) {
            controller.playCard(card, game, true, new ApprovingObject(source, game));
        }
        return true;
    }
}
