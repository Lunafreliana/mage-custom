package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.common.TapForManaAllTriggeredManaAbility;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.effects.common.continuous.GainAbilityControllerEffect;
import mage.abilities.effects.mana.AddManaOfAnyTypeProducedEffect;
import mage.abilities.effects.mana.ManaEffect;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.SetTargetPointer;
import mage.constants.Zone;
import mage.filter.common.FilterControlledLandPermanent;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.events.CreateTokenEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
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
        ManaEffect manaEffect = new AddManaOfAnyTypeProducedEffect();
        manaEffect.setText("add one mana of any type that land produced");
        Ability manaAbility = new TapForManaAllTriggeredManaAbility(
                manaEffect,
                new FilterControlledLandPermanent("you tap a land"),
                SetTargetPointer.PERMANENT
        );
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(
                new GainAbilityControllerEffect(manaAbility, Duration.EndOfTurn).setText(
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
