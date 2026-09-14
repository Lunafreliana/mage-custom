package mage.game.command.planes;

import mage.MageIdentifier;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.AsThoughEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.cards.Card;
import mage.constants.AsThoughEffectType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.ExileZone;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.events.GameEvent;
import mage.game.permanent.token.TreasureToken;
import mage.game.stack.Spell;
import mage.players.Player;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author The XMage Developers
 */
public final class TheMatrixOfTimePlane extends Plane {

    public TheMatrixOfTimePlane() {
        this.setPlaneType(Planes.PLANE_THE_MATRIX_OF_TIME);

        // When you planeswalk to The Matrix of Time, each player exiles the top card of their library.
        this.getAbilities().add(new PlaneswalkToSourceTriggeredAbility(new TheMatrixOfTimeExileEffect()));

        // During your turn, you may play lands and cast spells from among cards exiled with The Matrix of Time.
        this.getAbilities().add(new SimpleStaticAbility(
                Zone.COMMAND, new TheMatrixOfTimePlayEffect()
        ).setIdentifier(MageIdentifier.TheMatrixOfTimeWatcher));

        // Whenever you play a land or cast a spell from among cards exiled with The Matrix of Time,
        // that card's owner loses 3 life and exiles the top card of their library.
        this.getAbilities().add(new TheMatrixOfTimePlayedCardTriggeredAbility());

        // Whenever chaos ensues, create two Treasure tokens.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(
                new CreateTokenEffect(new TreasureToken(), 2), false
        ));
    }

    private TheMatrixOfTimePlane(final TheMatrixOfTimePlane plane) {
        super(plane);
    }

    @Override
    public TheMatrixOfTimePlane copy() {
        return new TheMatrixOfTimePlane(this);
    }
}

class TheMatrixOfTimeExileEffect extends OneShotEffect {

    TheMatrixOfTimeExileEffect() {
        super(Outcome.Exile);
        staticText = "each player exiles the top card of their library";
    }

    private TheMatrixOfTimeExileEffect(final TheMatrixOfTimeExileEffect effect) {
        super(effect);
    }

    @Override
    public TheMatrixOfTimeExileEffect copy() {
        return new TheMatrixOfTimeExileEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        for (UUID playerId : game.getPlayerList()) {
            Player player = game.getPlayer(playerId);
            if (player == null) {
                continue;
            }
            Card card = player.getLibrary().getFromTop(game);
            if (card != null) {
                player.moveCardsToExile(
                        card, source, game, true,
                        CardUtil.getExileZoneId(game, source), CardUtil.getSourceName(game, source)
                );
            }
        }
        return true;
    }
}

class TheMatrixOfTimePlayEffect extends AsThoughEffectImpl {

    TheMatrixOfTimePlayEffect() {
        super(AsThoughEffectType.PLAY_FROM_NOT_OWN_HAND_ZONE, Duration.WhileOnBattlefield, Outcome.Benefit);
        staticText = "During your turn, you may play lands and cast spells from among cards exiled with {this}";
    }

    private TheMatrixOfTimePlayEffect(final TheMatrixOfTimePlayEffect effect) {
        super(effect);
    }

    @Override
    public TheMatrixOfTimePlayEffect copy() {
        return new TheMatrixOfTimePlayEffect(this);
    }

    @Override
    public boolean applies(UUID sourceId, Ability source, UUID affectedControllerId, Game game) {
        if (!game.isActivePlayer(affectedControllerId)
                || game.getState().getZone(CardUtil.getMainCardId(game, sourceId)) != Zone.EXILED) {
            return false;
        }
        ExileZone exileZone = game.getExile().getExileZone(CardUtil.getExileZoneId(game, source));
        return exileZone != null && exileZone.contains(CardUtil.getMainCardId(game, sourceId));
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return true;
    }
}

class TheMatrixOfTimePlayedCardTriggeredAbility extends TriggeredAbilityImpl {

    TheMatrixOfTimePlayedCardTriggeredAbility() {
        super(Zone.COMMAND, new TheMatrixOfTimeOwnerEffect());
        setTriggerPhrase("Whenever you play a land or cast a spell from among cards exiled with {this}, ");
    }

    private TheMatrixOfTimePlayedCardTriggeredAbility(final TheMatrixOfTimePlayedCardTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public TheMatrixOfTimePlayedCardTriggeredAbility copy() {
        return new TheMatrixOfTimePlayedCardTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.LAND_PLAYED
                || event.getType() == GameEvent.EventType.SPELL_CAST;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!event.hasApprovingIdentifier(MageIdentifier.TheMatrixOfTimeWatcher)
                || !getSourceId().equals(event.getApprovingObject().getApprovingAbility().getSourceId())) {
            return false;
        }
        Card card;
        if (event.getType() == GameEvent.EventType.SPELL_CAST) {
            Spell spell = game.getSpell(event.getTargetId());
            card = spell == null ? null : spell.getCard();
        } else {
            card = game.getCard(event.getTargetId());
        }
        if (card == null) {
            return false;
        }
        getEffects().setTargetPointer(new FixedTarget(card.getOwnerId()));
        return true;
    }
}

class TheMatrixOfTimeOwnerEffect extends OneShotEffect {

    TheMatrixOfTimeOwnerEffect() {
        super(Outcome.Detriment);
        staticText = "that card's owner loses 3 life and exiles the top card of their library";
    }

    private TheMatrixOfTimeOwnerEffect(final TheMatrixOfTimeOwnerEffect effect) {
        super(effect);
    }

    @Override
    public TheMatrixOfTimeOwnerEffect copy() {
        return new TheMatrixOfTimeOwnerEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player owner = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (owner == null) {
            return false;
        }
        owner.loseLife(3, game, source, false);
        Card card = owner.getLibrary().getFromTop(game);
        if (card != null) {
            owner.moveCardsToExile(
                    card, source, game, true,
                    CardUtil.getExileZoneId(game, source), CardUtil.getSourceName(game, source)
            );
        }
        return true;
    }
}
