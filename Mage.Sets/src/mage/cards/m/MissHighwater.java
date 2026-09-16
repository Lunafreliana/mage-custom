package mage.cards.m;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.DelayedTriggeredAbility;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenCopyTargetEffect;
import mage.abilities.keyword.MenaceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.events.DamagedEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.targetpointer.FixedTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author muz
 */
public final class MissHighwater extends CardImpl {

    public MissHighwater(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.DEMON, SubType.ADVISOR);
        this.power = new MageInt(5);
        this.toughness = new MageInt(3);

        // Menace
        this.addAbility(MenaceAbility.getInstance());

        // Whenever Miss Highwater deals combat damage to a player who doesn't have a contract counter,
        // they may discard their hand. If they do, they draw seven cards and get a contract counter.
        // For as long as they have a contract counter, when they lose the game, for each artifact and
        // creature they controlled, create a token that's a copy of it.
        this.addAbility(new MissHighwaterCombatDamageTriggeredAbility());
    }

    private MissHighwater(final MissHighwater card) {
        super(card);
    }

    @Override
    public MissHighwater copy() {
        return new MissHighwater(this);
    }
}

class MissHighwaterCombatDamageTriggeredAbility extends TriggeredAbilityImpl {

    MissHighwaterCombatDamageTriggeredAbility() {
        super(Zone.BATTLEFIELD, new MissHighwaterContractEffect());
        setTriggerPhrase("Whenever {this} deals combat damage to a player who doesn't have a contract counter, ");
    }

    private MissHighwaterCombatDamageTriggeredAbility(final MissHighwaterCombatDamageTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public MissHighwaterCombatDamageTriggeredAbility copy() {
        return new MissHighwaterCombatDamageTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DAMAGED_PLAYER;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        Player player = game.getPlayer(event.getPlayerId());
        if (!getSourceId().equals(event.getSourceId())
                || !((DamagedEvent) event).isCombatDamage()
                || player == null
                || player.getCountersCount(CounterType.CONTRACT) > 0) {
            return false;
        }
        getEffects().setTargetPointer(new FixedTarget(player.getId()));
        return true;
    }
}

class MissHighwaterContractEffect extends OneShotEffect {

    MissHighwaterContractEffect() {
        super(Outcome.DrawCard);
        staticText = "they may discard their hand. If they do, they draw seven cards and get a contract counter. "
                + "For as long as they have a contract counter, when they lose the game, for each artifact and "
                + "creature they controlled, create a token that's a copy of it";
    }

    private MissHighwaterContractEffect(final MissHighwaterContractEffect effect) {
        super(effect);
    }

    @Override
    public MissHighwaterContractEffect copy() {
        return new MissHighwaterContractEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(getTargetPointer().getFirst(game, source));
        if (player == null || !player.chooseUse(
                Outcome.DrawCard, "Discard your hand, draw seven cards, and get a contract counter?", source, game
        )) {
            return false;
        }
        player.discard(player.getHand(), false, source, game);
        player.drawCards(7, source, game);
        player.addCounters(CounterType.CONTRACT.createInstance(), source.getControllerId(), source, game);
        game.addDelayedTriggeredAbility(new MissHighwaterPlayerLosesTriggeredAbility(player.getId()), source);
        return true;
    }
}

class MissHighwaterPlayerLosesTriggeredAbility extends DelayedTriggeredAbility {

    private final UUID playerId;

    MissHighwaterPlayerLosesTriggeredAbility(UUID playerId) {
        super(new MissHighwaterCreateCopiesEffect(), Duration.EndOfGame, true, false);
        this.playerId = playerId;
        setTriggerPhrase("When that player loses the game, ");
    }

    private MissHighwaterPlayerLosesTriggeredAbility(final MissHighwaterPlayerLosesTriggeredAbility ability) {
        super(ability);
        this.playerId = ability.playerId;
    }

    @Override
    public MissHighwaterPlayerLosesTriggeredAbility copy() {
        return new MissHighwaterPlayerLosesTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.LOST;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!playerId.equals(event.getPlayerId())) {
            return false;
        }
        MissHighwaterCreateCopiesEffect effect = (MissHighwaterCreateCopiesEffect) getEffects().get(0);
        game.getBattlefield().getAllActivePermanents(playerId).stream()
                .filter(permanent -> permanent.isArtifact(game) || permanent.isCreature(game))
                .forEach(effect::addPermanent);
        return true;
    }

    @Override
    public boolean isInactive(Game game) {
        Player player = game.getPlayer(playerId);
        return player == null || player.getCountersCount(CounterType.CONTRACT) == 0;
    }
}

class MissHighwaterCreateCopiesEffect extends OneShotEffect {

    private final List<Permanent> permanents = new ArrayList<>();

    MissHighwaterCreateCopiesEffect() {
        super(Outcome.PutCreatureInPlay);
        staticText = "for each artifact and creature they controlled, create a token that's a copy of it";
    }

    private MissHighwaterCreateCopiesEffect(final MissHighwaterCreateCopiesEffect effect) {
        super(effect);
        effect.permanents.forEach(permanent -> permanents.add(permanent.copy()));
    }

    void addPermanent(Permanent permanent) {
        permanents.add(permanent.copy());
    }

    @Override
    public MissHighwaterCreateCopiesEffect copy() {
        return new MissHighwaterCreateCopiesEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        permanents.forEach(permanent -> new CreateTokenCopyTargetEffect()
                .setSavedPermanent(permanent)
                .apply(game, source));
        return true;
    }
}
