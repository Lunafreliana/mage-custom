package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.effects.ContinuousRuleModifyingEffectImpl;
import mage.abilities.effects.common.ExileReturnBattlefieldNextEndStepTargetEffect;
import mage.abilities.effects.common.PlaneswalkEffect;
import mage.abilities.triggers.BeginningOfCombatTriggeredAbility;
import mage.cards.Card;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.target.common.TargetCreaturePermanent;

/**
 * @author The XMage Developers
 */
public final class BadWolfBayPlane extends Plane {

    public BadWolfBayPlane() {
        this.setPlaneType(Planes.PLANE_BAD_WOLF_BAY);

        // At the beginning of combat on your turn, exile up to one target creature.
        // Return it to the battlefield under its owner's control at the beginning of the next end step.
        Ability ability = new BeginningOfCombatTriggeredAbility(
                Zone.COMMAND, TargetController.YOU,
                new ExileReturnBattlefieldNextEndStepTargetEffect().withTextThatCard(false), false
        );
        ability.addTarget(new TargetCreaturePermanent(0, 1));
        this.getAbilities().add(ability);

        // When chaos ensues, cards can't enter the battlefield from exile this turn. Then planeswalk.
        ability = new ChaosEnsuesTriggeredAbility(new BadWolfBayEffect(), false);
        ability.addEffect(new PlaneswalkEffect(false).concatBy("Then"));
        this.getAbilities().add(ability);
    }

    private BadWolfBayPlane(final BadWolfBayPlane plane) {
        super(plane);
    }

    @Override
    public BadWolfBayPlane copy() {
        return new BadWolfBayPlane(this);
    }
}

class BadWolfBayEffect extends ContinuousRuleModifyingEffectImpl {

    BadWolfBayEffect() {
        super(Duration.EndOfTurn, Outcome.Detriment);
        staticText = "cards can't enter the battlefield from exile this turn";
    }

    private BadWolfBayEffect(final BadWolfBayEffect effect) {
        super(effect);
    }

    @Override
    public BadWolfBayEffect copy() {
        return new BadWolfBayEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ZONE_CHANGE;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        ZoneChangeEvent zoneChangeEvent = (ZoneChangeEvent) event;
        if (zoneChangeEvent.getFromZone() != Zone.EXILED
                || zoneChangeEvent.getToZone() != Zone.BATTLEFIELD) {
            return false;
        }
        Card card = game.getCard(event.getTargetId());
        return card != null;
    }
}
