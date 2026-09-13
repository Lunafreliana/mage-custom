package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.cards.Card;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.CommanderCardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetCard;
import mage.util.CardUtil;
import mage.watchers.common.CommanderPlaysCountWatcher;

import java.util.Set;
import java.util.UUID;

/**
 * @author The XMage Developers
 */
public class TheCommandZonePlane extends Plane {

    public TheCommandZonePlane() {
        this.setPlaneType(Planes.PLANE_THE_COMMAND_ZONE);

        // When you planeswalk here, each player who controls their commander draws a card.
        // Other players may put their commander from the command zone onto the battlefield.
        this.getAbilities().add(new PlaneswalkToSourceTriggeredAbility(new TheCommandZoneArrivalEffect()));

        // If an ability of your commander would trigger during your turn, that ability triggers an additional time.
        this.getAbilities().add(new SimpleStaticAbility(Zone.COMMAND, new TheCommandZoneAdditionalTriggerEffect()));

        // Whenever chaos ensues, the number of times each commander has been cast from the command zone becomes 0.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(new TheCommandZoneResetEffect(), false));
    }

    private TheCommandZonePlane(final TheCommandZonePlane plane) {
        super(plane);
    }

    @Override
    public TheCommandZonePlane copy() {
        return new TheCommandZonePlane(this);
    }
}

class TheCommandZoneArrivalEffect extends OneShotEffect {

    TheCommandZoneArrivalEffect() {
        super(Outcome.Benefit);
        staticText = "each player who controls their commander draws a card. "
                + "Other players may put their commander from the command zone onto the battlefield";
    }

    private TheCommandZoneArrivalEffect(final TheCommandZoneArrivalEffect effect) {
        super(effect);
    }

    @Override
    public TheCommandZoneArrivalEffect copy() {
        return new TheCommandZoneArrivalEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        for (UUID playerId : game.getPlayerList()) {
            Player player = game.getPlayer(playerId);
            if (player == null) {
                continue;
            }
            Set<UUID> commanderIds = game.getCommandersIds(
                    player, CommanderCardType.COMMANDER_OR_OATHBREAKER, true
            );
            if (game.getBattlefield().getAllActivePermanents(playerId).stream()
                    .map(Permanent::getId)
                    .anyMatch(commanderIds::contains)) {
                player.drawCards(1, source, game);
            }
        }
        for (UUID playerId : game.getPlayerList()) {
            Player player = game.getPlayer(playerId);
            if (player == null || playerId.equals(source.getControllerId())) {
                continue;
            }
            Cards commanders = new CardsImpl(game.getCommanderCardsFromCommandZone(
                    player, CommanderCardType.COMMANDER_OR_OATHBREAKER
            ));
            TargetCard target = new TargetCard(0, 1, Zone.COMMAND, StaticFilters.FILTER_CARD);
            target.withNotTarget(true);
            player.choose(Outcome.PutCardInPlay, commanders, target, source, game);
            Card commander = game.getCard(target.getFirstTarget());
            if (commander != null) {
                player.moveCards(commander, Zone.BATTLEFIELD, source, game);
            }
        }
        return true;
    }
}

class TheCommandZoneAdditionalTriggerEffect extends ReplacementEffectImpl {

    TheCommandZoneAdditionalTriggerEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Benefit);
        staticText = "if an ability of your commander would trigger during your turn, "
                + "that ability triggers an additional time";
    }

    private TheCommandZoneAdditionalTriggerEffect(final TheCommandZoneAdditionalTriggerEffect effect) {
        super(effect);
    }

    @Override
    public TheCommandZoneAdditionalTriggerEffect copy() {
        return new TheCommandZoneAdditionalTriggerEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.NUMBER_OF_TRIGGERS;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        Player controller = game.getPlayer(source.getControllerId());
        UUID commanderId = CardUtil.getMainCardId(game, event.getSourceId());
        return controller != null
                && game.getState().hasFaceUpPlane(Planes.PLANE_THE_COMMAND_ZONE)
                && source.getControllerId().equals(game.getActivePlayerId())
                && game.getCommandersIds(controller, CommanderCardType.COMMANDER_OR_OATHBREAKER, false)
                .contains(commanderId);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        event.setAmount(CardUtil.overflowInc(event.getAmount(), 1));
        return false;
    }
}

class TheCommandZoneResetEffect extends OneShotEffect {

    TheCommandZoneResetEffect() {
        super(Outcome.Benefit);
        staticText = "the number of times each commander has been cast from the command zone becomes 0. "
                + "<i>(This resets commander tax.)</i>";
    }

    private TheCommandZoneResetEffect(final TheCommandZoneResetEffect effect) {
        super(effect);
    }

    @Override
    public TheCommandZoneResetEffect copy() {
        return new TheCommandZoneResetEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        CommanderPlaysCountWatcher watcher = game.getState().getWatcher(CommanderPlaysCountWatcher.class);
        if (watcher == null) {
            return false;
        }
        watcher.resetCommanderCastCounts();
        return true;
    }
}
