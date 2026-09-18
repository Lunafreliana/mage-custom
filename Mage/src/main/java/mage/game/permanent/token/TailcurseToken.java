package mage.game.permanent.token;

import mage.abilities.Ability;
import mage.abilities.BatchTriggeredAbility;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.keyword.EnchantAbility;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.DamagedBatchForOnePlayerEvent;
import mage.game.events.DamagedEvent;
import mage.game.events.DamagedPlayerEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.target.TargetPlayer;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author The XMage Developers
 */
public final class TailcurseToken extends TokenImpl {

    public TailcurseToken() {
        super("Tailcurse", "colorless Aura Curse enchantment token named Tailcurse");
        cardType.add(CardType.ENCHANTMENT);
        subtype.add(SubType.AURA);
        subtype.add(SubType.CURSE);

        // Enchant player
        TargetPlayer auraTarget = new TargetPlayer();
        Ability ability = new EnchantAbility(auraTarget);
        ability.addTarget(auraTarget);
        ability.addEffect(new AttachEffect(Outcome.Benefit));
        this.addAbility(ability);

        // Whenever enchanted player is dealt combat damage, you and each other player who
        // controlled a source that dealt combat damage to that player this way each create
        // that many 1/1 red Elemental creature tokens.
        this.addAbility(new TailcurseTriggeredAbility());
    }

    private TailcurseToken(final TailcurseToken token) {
        super(token);
    }

    @Override
    public TailcurseToken copy() {
        return new TailcurseToken(this);
    }
}

class TailcurseTriggeredAbility extends TriggeredAbilityImpl implements BatchTriggeredAbility<DamagedPlayerEvent> {

    TailcurseTriggeredAbility() {
        super(Zone.BATTLEFIELD, new TailcurseEffect());
        setTriggerPhrase("Whenever enchanted player is dealt combat damage, ");
    }

    private TailcurseTriggeredAbility(final TailcurseTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public TailcurseTriggeredAbility copy() {
        return new TailcurseTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DAMAGED_BATCH_FOR_ONE_PLAYER;
    }

    @Override
    public boolean checkEvent(DamagedPlayerEvent event, Game game) {
        return event.isCombatDamage();
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        Permanent aura = game.getPermanent(getSourceId());
        if (aura == null || !event.getTargetId().equals(aura.getAttachedTo())) {
            return false;
        }
        List<DamagedPlayerEvent> events = getFilteredEvents((DamagedBatchForOnePlayerEvent) event, game);
        if (events.isEmpty()) {
            return false;
        }
        Set<UUID> tokenCreators = events.stream()
                .map(GameEvent::getSourceId)
                .map(game::getPermanentOrLKIBattlefield)
                .filter(permanent -> permanent != null)
                .map(Permanent::getControllerId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        tokenCreators.add(getControllerId());
        getAllEffects().setValue("damage", events.stream().mapToInt(DamagedEvent::getAmount).sum());
        getAllEffects().setValue("tokenCreators", tokenCreators);
        return true;
    }
}

class TailcurseEffect extends OneShotEffect {

    TailcurseEffect() {
        super(Outcome.Benefit);
        staticText = "you and each other player who controlled a source that dealt combat damage to that player "
                + "this way each create that many 1/1 red Elemental creature tokens";
    }

    private TailcurseEffect(final TailcurseEffect effect) {
        super(effect);
    }

    @Override
    public TailcurseEffect copy() {
        return new TailcurseEffect(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean apply(Game game, Ability source) {
        Integer damage = (Integer) getValue("damage");
        Set<UUID> tokenCreators = (Set<UUID>) getValue("tokenCreators");
        if (damage == null || damage < 1 || tokenCreators == null) {
            return false;
        }
        for (UUID playerId : tokenCreators) {
            if (game.getPlayer(playerId) != null) {
                new RedElementalToken().putOntoBattlefield(
                        damage, game, source, playerId, false, false, null, null
                );
            }
        }
        return true;
    }
}
