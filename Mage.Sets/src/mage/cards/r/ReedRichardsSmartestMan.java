package mage.cards.r;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.continuous.MaximumHandSizeControllerEffect;
import mage.abilities.effects.common.replacement.DrawExceptFirstDrawReplacementEffect;
import mage.abilities.keyword.ReachAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.WatcherScope;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.watchers.Watcher;
import mage.watchers.common.CardsDrawnDuringDrawStepWatcher;

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
        Ability ability = new SimpleStaticAbility(new ReedRichardsSmartestManReplacementEffect());
        ability.addWatcher(new CardsDrawnDuringDrawStepWatcher());
        this.addAbility(ability, new ReedRichardsSmartestManWatcher());
    }

    private ReedRichardsSmartestMan(final ReedRichardsSmartestMan card) {
        super(card);
    }

    @Override
    public ReedRichardsSmartestMan copy() {
        return new ReedRichardsSmartestMan(this);
    }
}

class ReedRichardsSmartestManReplacementEffect extends DrawExceptFirstDrawReplacementEffect {

    ReedRichardsSmartestManReplacementEffect() {
        super(4);
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
    public boolean applies(GameEvent event, Ability source, Game game) {
        ReedRichardsSmartestManWatcher watcher = game.getState().getWatcher(
                ReedRichardsSmartestManWatcher.class, source.getSourceId()
        );
        return watcher != null && !watcher.isUsed() && super.applies(event, source, game);
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
        return super.replaceEvent(event, source, game);
    }
}

class ReedRichardsSmartestManWatcher extends Watcher {

    private boolean used;

    ReedRichardsSmartestManWatcher() {
        super(WatcherScope.CARD);
    }

    private ReedRichardsSmartestManWatcher(final ReedRichardsSmartestManWatcher watcher) {
        super(watcher);
        this.used = watcher.used;
    }

    @Override
    public ReedRichardsSmartestManWatcher copy() {
        return new ReedRichardsSmartestManWatcher(this);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        // Usage is set by the replacement effect before it creates new draw events.
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
    }
}
