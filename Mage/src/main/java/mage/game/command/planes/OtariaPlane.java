package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.turn.AddExtraTurnControllerEffect;
import mage.abilities.keyword.FlashbackAbility;
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

/**
 * Plane - Otaria
 */
public final class OtariaPlane extends Plane {

    public OtariaPlane() {
        setPlaneType(Planes.PLANE_OTARIA);

        // Instant and sorcery cards in graveyards have flashback. The flashback
        // cost is equal to the card's mana cost.
        getAbilities().add(new SimpleStaticAbility(Zone.COMMAND, new OtariaFlashbackEffect()));

        // Whenever chaos ensues, take an extra turn after this one.
        getAbilities().add(new ChaosEnsuesTriggeredAbility(new AddExtraTurnControllerEffect(), false));
    }

    private OtariaPlane(final OtariaPlane plane) {
        super(plane);
    }

    @Override
    public OtariaPlane copy() {
        return new OtariaPlane(this);
    }
}

class OtariaFlashbackEffect extends ContinuousEffectImpl {

    OtariaFlashbackEffect() {
        super(Duration.Custom, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        staticText = "Instant and sorcery cards in graveyards have flashback. "
                + "The flashback cost is equal to the card's mana cost";
    }

    private OtariaFlashbackEffect(final OtariaFlashbackEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        if (!game.getState().hasFaceUpPlane(Planes.PLANE_OTARIA)) {
            return false;
        }
        for (Player player : game.getPlayers().values()) {
            for (Card card : player.getGraveyard().getCards(
                    StaticFilters.FILTER_CARD_INSTANT_OR_SORCERY, game)) {
                Ability ability = new FlashbackAbility(card, card.getManaCost());
                ability.setSourceId(card.getId());
                ability.setControllerId(card.getOwnerId());
                game.getState().addOtherAbility(card, ability);
            }
        }
        return true;
    }

    @Override
    public OtariaFlashbackEffect copy() {
        return new OtariaFlashbackEffect(this);
    }
}
