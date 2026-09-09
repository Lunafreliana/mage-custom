package mage.cards.r;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.effects.common.continuous.MaximumHandSizeControllerEffect;
import mage.abilities.keyword.ReachAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.PhaseStep;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.WatcherScope;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.players.Player;
import mage.watchers.Watcher;

import java.util.UUID;

/**
 * @author muz
 */
public final class ReedRichardsSmartestMan extends CardImpl {

    public ReedRichardsSmartestMan(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{5}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.SCIENTIST);
        this.subtype.add(SubType.HERO);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // Reach
        this.addAbility(ReachAbility.getInstance());

        // You have no maximum hand size.
        this.addAbility(new SimpleStaticAbility(new MaximumHandSizeControllerEffect(
                Integer.MAX_VALUE, Duration.WhileOnBattlefield,
                MaximumHandSizeControllerEffect.HandSizeModification.SET
        )));

        // The first time you would draw a card each turn except the first card you draw during each of your draw steps, you draw four cards instead.
        this.addAbility(new SimpleStaticAbility(new ReedRichardsSmartestManReplacementEffect()),
                new ReedRichardsSmartestManWatcher());
    }

    private ReedRichardsSmartestMan(final ReedRichardsSmartestMan card) {
        super(card);
    }

    @Override
    public ReedRichardsSmartestMan copy() {
        return new ReedRichardsSmartestMan(this);
    }
}

class ReedRichardsSmartestManReplacementEffect extends ReplacementEffectImpl {

    ReedRichardsSmartestManReplacementEffect() {
        super(Duration.WhileOnBattlefield, Outcome.DrawCard);
        staticText = "The first time you would draw a card each turn except the first card you draw "
                + "during each of your draw steps, you draw four cards instead";
    }

    private ReedRichardsSmartestManReplacementEffect(final ReedRichardsSmartestManReplacementEffect effect) {
        super(effect);
    }

    @Override
    public ReedRichardsSmartestManReplacementEffect copy() {
        return new ReedRichardsSmartestManReplacementEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DRAW_CARD;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        if (!source.isControlledBy(event.getPlayerId())) {
            return false;
        }

        ReedRichardsSmartestManWatcher watcher = game.getState().getWatcher(
                ReedRichardsSmartestManWatcher.class, source.getSourceId()
        );
        if (watcher == null || watcher.isUsed()) {
            return false;
        }

        // The first draw of each of the player's draw steps is exempt even if an
        // earlier draw this turn has already used this replacement effect.
        if (game.isActivePlayer(event.getPlayerId())
                && game.getPhase().getStep().getType() == PhaseStep.DRAW
                && !watcher.hasDrawnDuringDrawStep()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        ReedRichardsSmartestManWatcher watcher = game.getState().getWatcher(
                ReedRichardsSmartestManWatcher.class, source.getSourceId()
        );
        if (watcher != null) {
            // Mark this before drawing so the replacement draws cannot make this
            // effect available again through nested replacement events.
            watcher.setUsed();
        }
        Player player = game.getPlayer(event.getPlayerId());
        if (player != null) {
            player.drawCards(4, source, game, event);
        }
        return true;
    }
}

class ReedRichardsSmartestManWatcher extends Watcher {

    private boolean used;
    private boolean drawnDuringDrawStep;

    ReedRichardsSmartestManWatcher() {
        super(WatcherScope.CARD);
    }

    private ReedRichardsSmartestManWatcher(final ReedRichardsSmartestManWatcher watcher) {
        super(watcher);
        this.used = watcher.used;
        this.drawnDuringDrawStep = watcher.drawnDuringDrawStep;
    }

    @Override
    public ReedRichardsSmartestManWatcher copy() {
        return new ReedRichardsSmartestManWatcher(this);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.DRAW_STEP_PRE) {
            drawnDuringDrawStep = false;
            return;
        }
        if (event.getType() == GameEvent.EventType.DREW_CARD
                && game.isActivePlayer(event.getPlayerId())
                && game.getPhase().getStep().getType() == PhaseStep.DRAW) {
            drawnDuringDrawStep = true;
        }
    }

    boolean hasDrawnDuringDrawStep() {
        return drawnDuringDrawStep;
    }

    boolean isUsed() {
        return used;
    }

    void setUsed() {
        used = true;
    }

    @Override
    public void reset() {
        super.reset();
        used = false;
        drawnDuringDrawStep = false;
    }
}
