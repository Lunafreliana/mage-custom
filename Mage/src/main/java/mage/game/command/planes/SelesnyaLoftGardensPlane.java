package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.effects.common.CreateDelayedTriggeredAbilityEffect;
import mage.abilities.effects.mana.AddManaOfAnyTypeProducedEffect;
import mage.abilities.mana.DelayedTriggeredManaAbility;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.events.CreateTokenEvent;
import mage.game.events.GameEvent;
import mage.game.events.TappedForManaEvent;
import mage.game.permanent.Permanent;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

/**
 * @author The XMage Developers
 */
public class SelesnyaLoftGardensPlane extends Plane {

    public SelesnyaLoftGardensPlane() {
        this.setPlaneType(Planes.PLANE_SELESNYA_LOFT_GARDENS);

        // If an effect would create one or more tokens, it creates twice that many of those tokens instead.
        this.getAbilities().add(new SimpleStaticAbility(
                Zone.COMMAND, new SelesnyaLoftGardensTokenEffect()));

        // If an effect would put one or more counters on a permanent, it puts twice that many of those counters on that permanent instead.
        this.getAbilities().add(new SimpleStaticAbility(
                Zone.COMMAND, new SelesnyaLoftGardensCounterEffect()));

        // Whenever chaos ensues, until end of turn, whenever you tap a land for mana, add one mana of any type that land produced.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(
                new CreateDelayedTriggeredAbilityEffect(new SelesnyaLoftGardensManaAbility()).setText(
                        "until end of turn, whenever you tap a land for mana, "
                                + "add one mana of any type that land produced"
                ), false));
    }

    private SelesnyaLoftGardensPlane(final SelesnyaLoftGardensPlane plane) {
        super(plane);
    }

    @Override
    public SelesnyaLoftGardensPlane copy() {
        return new SelesnyaLoftGardensPlane(this);
    }
}

class SelesnyaLoftGardensManaAbility extends DelayedTriggeredManaAbility {

    SelesnyaLoftGardensManaAbility() {
        super(new AddManaOfAnyTypeProducedEffect(), Duration.EndOfTurn, false);
        this.usesStack = false;
    }

    private SelesnyaLoftGardensManaAbility(final SelesnyaLoftGardensManaAbility ability) {
        super(ability);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.TAPPED_FOR_MANA;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        TappedForManaEvent manaEvent = (TappedForManaEvent) event;
        Permanent permanent = manaEvent.getPermanent();
        if (permanent == null
                || !permanent.isLand(game)
                || !permanent.isControlledBy(getControllerId())) {
            return false;
        }
        getEffects().setValue("mana", manaEvent.getMana());
        getEffects().setValue("tappedPermanent", permanent);
        getEffects().setTargetPointer(new FixedTarget(
                permanent.getId(), permanent.getZoneChangeCounter(game)
        ));
        return true;
    }

    @Override
    public SelesnyaLoftGardensManaAbility copy() {
        return new SelesnyaLoftGardensManaAbility(this);
    }
}

class SelesnyaLoftGardensTokenEffect extends ReplacementEffectImpl {

    SelesnyaLoftGardensTokenEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Copy);
        staticText = "If an effect would create one or more tokens, "
                + "it creates twice that many of those tokens instead";
    }

    private SelesnyaLoftGardensTokenEffect(final SelesnyaLoftGardensTokenEffect effect) {
        super(effect);
    }

    @Override
    public SelesnyaLoftGardensTokenEffect copy() {
        return new SelesnyaLoftGardensTokenEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.CREATE_TOKEN;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        return game.getState().hasFaceUpPlane(Planes.PLANE_SELESNYA_LOFT_GARDENS);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        if (event instanceof CreateTokenEvent) {
            ((CreateTokenEvent) event).multiplyTokens(2);
        }
        return false;
    }
}

class SelesnyaLoftGardensCounterEffect extends ReplacementEffectImpl {

    SelesnyaLoftGardensCounterEffect() {
        super(Duration.WhileOnBattlefield, Outcome.BoostCreature, false);
        staticText = "If an effect would put one or more counters on a permanent, "
                + "it puts twice that many of those counters on that permanent instead";
    }

    private SelesnyaLoftGardensCounterEffect(final SelesnyaLoftGardensCounterEffect effect) {
        super(effect);
    }

    @Override
    public SelesnyaLoftGardensCounterEffect copy() {
        return new SelesnyaLoftGardensCounterEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ADD_COUNTERS;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        if (!game.getState().hasFaceUpPlane(Planes.PLANE_SELESNYA_LOFT_GARDENS)
                || !event.getFlag()
                || event.getAmount() <= 0) {
            return false;
        }
        Permanent permanent = game.getPermanent(event.getTargetId());
        if (permanent == null) {
            permanent = game.getPermanentEntering(event.getTargetId());
            if (permanent != null && permanent.isLand(game)) {
                return false;
            }
        }
        return permanent != null;
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        event.setAmountForCounters(CardUtil.overflowMultiply(event.getAmount(), 2), true);
        return false;
    }

    @Override
    public String getText(Mode mode) {
        return staticText;
    }
}
