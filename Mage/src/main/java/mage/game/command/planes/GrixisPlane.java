package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.ReturnFromGraveyardToBattlefieldTargetEffect;
import mage.abilities.keyword.UnearthAbility;
import mage.cards.Card;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.SubLayer;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.command.Plane;
import mage.players.Player;
import mage.target.common.TargetCardInGraveyard;

/**
 * @author The XMage Developers
 */
public final class GrixisPlane extends Plane {

    public GrixisPlane() {
        setPlaneType(Planes.PLANE_GRIXIS);

        // Blue, black, and/or red creature cards in your graveyard have unearth.
        // The unearth cost is equal to the card's mana cost.
        getAbilities().add(new SimpleStaticAbility(Zone.COMMAND, new GrixisUnearthEffect()));

        // Whenever chaos ensues, put target creature card from a graveyard
        // onto the battlefield under your control.
        Ability ability = new ChaosEnsuesTriggeredAbility(
                new ReturnFromGraveyardToBattlefieldTargetEffect(), false
        );
        ability.addTarget(new TargetCardInGraveyard(StaticFilters.FILTER_CARD_CREATURE_A_GRAVEYARD));
        getAbilities().add(ability);
    }

    private GrixisPlane(final GrixisPlane plane) {
        super(plane);
    }

    @Override
    public GrixisPlane copy() {
        return new GrixisPlane(this);
    }
}

class GrixisUnearthEffect extends ContinuousEffectImpl {

    GrixisUnearthEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6,
                SubLayer.NA, Outcome.AddAbility);
        staticText = "Blue, black, and/or red creature cards in your graveyard have unearth. "
                + "The unearth cost is equal to the card's mana cost";
    }

    private GrixisUnearthEffect(final GrixisUnearthEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        for (Card card : controller.getGraveyard().getCards(game)) {
            if (!card.isCreature(game)
                    || !(card.getColor(game).isBlue()
                    || card.getColor(game).isBlack()
                    || card.getColor(game).isRed())) {
                continue;
            }
            game.getState().addOtherAbility(card, new UnearthAbility(card.getManaCost().copy()));
        }
        return true;
    }

    @Override
    public GrixisUnearthEffect copy() {
        return new GrixisUnearthEffect(this);
    }
}
