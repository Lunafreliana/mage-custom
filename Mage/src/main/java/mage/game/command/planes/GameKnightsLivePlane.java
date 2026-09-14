package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.CompoundAbility;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.DealsCombatDamageToAPlayerTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.combat.CantBeBlockedAllEffect;
import mage.abilities.effects.common.continuous.GainAbilityControlledEffect;
import mage.abilities.keyword.DoubleStrikeAbility;
import mage.abilities.keyword.TrampleAbility;
import mage.constants.*;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.mageobject.CommanderPredicate;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.game.stack.Spell;
import mage.players.Player;
import mage.util.CardUtil;
import mage.watchers.common.PlaneswalkedWatcher;

import java.util.UUID;

/**
 * @author The XMage Developers
 */
public final class GameKnightsLivePlane extends Plane {

    private static final FilterControlledCreaturePermanent knightFilter
            = new FilterControlledCreaturePermanent(SubType.KNIGHT, "Knights you control");
    private static final FilterControlledCreaturePermanent commanderFilter
            = new FilterControlledCreaturePermanent("commanders you control");

    static {
        commanderFilter.add(CommanderPredicate.instance);
    }

    public GameKnightsLivePlane() {
        this.setPlaneType(Planes.PLANE_GAME_KNIGHTS_LIVE);

        // Whenever a player casts their commander or attacks with their commander, they may say
        // "Only one may stand." If they do, that commander becomes a Knight in addition to its
        // other types until a player planeswalks away from Game Knights Live.
        this.getAbilities().add(new GameKnightsLiveCommanderTriggeredAbility());

        // Knights you control have double strike and trample.
        this.getAbilities().add(new SimpleStaticAbility(Zone.COMMAND, new GainAbilityControlledEffect(
                new CompoundAbility(DoubleStrikeAbility.getInstance(), TrampleAbility.getInstance()),
                Duration.WhileOnBattlefield, knightFilter
        )));

        // Whenever chaos ensues, commanders you control can't be blocked this turn and gain
        // "Whenever this creature deals combat damage to a player, draw a card" until end of turn.
        Ability ability = new ChaosEnsuesTriggeredAbility(
                new CantBeBlockedAllEffect(commanderFilter, Duration.EndOfTurn), false
        );
        ability.addEffect(new GainAbilityControlledEffect(
                new DealsCombatDamageToAPlayerTriggeredAbility(
                        new DrawCardSourceControllerEffect(1), false
                ), Duration.EndOfTurn, commanderFilter
        ).setText("and gain \"Whenever this creature deals combat damage to a player, draw a card\" until end of turn"));
        this.getAbilities().add(ability);
    }

    private GameKnightsLivePlane(final GameKnightsLivePlane plane) {
        super(plane);
    }

    @Override
    public GameKnightsLivePlane copy() {
        return new GameKnightsLivePlane(this);
    }
}

class GameKnightsLiveCommanderTriggeredAbility extends TriggeredAbilityImpl {

    GameKnightsLiveCommanderTriggeredAbility() {
        super(Zone.COMMAND, new GameKnightsLiveMakeKnightEffect(), false);
        setTriggerPhrase("Whenever a player casts their commander or attacks with their commander, ");
    }

    private GameKnightsLiveCommanderTriggeredAbility(final GameKnightsLiveCommanderTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.SPELL_CAST
                || event.getType() == GameEvent.EventType.ATTACKER_DECLARED;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        Player player = game.getPlayer(event.getPlayerId());
        UUID commanderId;
        if (event.getType() == GameEvent.EventType.SPELL_CAST) {
            Spell spell = game.getStack().getSpell(event.getTargetId());
            if (player == null || spell == null || !game.isCommanderObject(player, spell)) {
                return false;
            }
            commanderId = CardUtil.getMainCardId(game, spell.getSourceId());
        } else {
            Permanent attacker = game.getPermanent(event.getSourceId());
            if (player == null || attacker == null || !game.isCommanderObject(player, attacker)) {
                return false;
            }
            commanderId = CardUtil.getMainCardId(game, attacker.getId());
        }
        getEffects().setValue("gameKnightsLivePlayer", player.getId());
        getEffects().setValue("gameKnightsLiveCommander", commanderId);
        return true;
    }

    @Override
    public GameKnightsLiveCommanderTriggeredAbility copy() {
        return new GameKnightsLiveCommanderTriggeredAbility(this);
    }
}

class GameKnightsLiveMakeKnightEffect extends OneShotEffect {

    GameKnightsLiveMakeKnightEffect() {
        super(Outcome.Benefit);
        staticText = "they may say \"Only one may stand.\" If they do, that commander becomes a Knight "
                + "in addition to its other types until a player planeswalks away from Game Knights Live";
    }

    private GameKnightsLiveMakeKnightEffect(final GameKnightsLiveMakeKnightEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer((UUID) getValue("gameKnightsLivePlayer"));
        UUID commanderId = (UUID) getValue("gameKnightsLiveCommander");
        if (player == null || commanderId == null || !player.chooseUse(
                Outcome.Benefit, "Say \"Only one may stand\"?", source, game
        )) {
            return false;
        }
        game.addEffect(new GameKnightsLiveKnightEffect(commanderId), source);
        return true;
    }

    @Override
    public GameKnightsLiveMakeKnightEffect copy() {
        return new GameKnightsLiveMakeKnightEffect(this);
    }
}

class GameKnightsLiveKnightEffect extends ContinuousEffectImpl {

    private final UUID commanderId;
    private int planeswalkCount = -1;

    GameKnightsLiveKnightEffect(UUID commanderId) {
        super(Duration.Custom, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.Benefit);
        this.commanderId = commanderId;
    }

    private GameKnightsLiveKnightEffect(final GameKnightsLiveKnightEffect effect) {
        super(effect);
        this.commanderId = effect.commanderId;
        this.planeswalkCount = effect.planeswalkCount;
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        PlaneswalkedWatcher watcher = game.getState().getWatcher(PlaneswalkedWatcher.class);
        planeswalkCount = watcher == null ? 0 : watcher.getCount();
    }

    @Override
    public boolean apply(Game game, Ability source) {
        PlaneswalkedWatcher watcher = game.getState().getWatcher(PlaneswalkedWatcher.class);
        if (!game.getState().hasFaceUpPlane(Planes.PLANE_GAME_KNIGHTS_LIVE)
                || watcher != null && watcher.getCount() > planeswalkCount) {
            discard();
            return false;
        }
        for (Permanent permanent : game.getBattlefield().getAllActivePermanents()) {
            if (commanderId.equals(CardUtil.getMainCardId(game, permanent.getId()))) {
                permanent.addSubType(game, SubType.KNIGHT);
            }
        }
        return true;
    }

    @Override
    public GameKnightsLiveKnightEffect copy() {
        return new GameKnightsLiveKnightEffect(this);
    }
}
