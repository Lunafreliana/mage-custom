package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.effects.common.continuous.GainAbilityControlledEffect;
import mage.abilities.effects.common.counter.AddCountersTargetEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.constants.Duration;
import mage.constants.Planes;
import mage.constants.RollDieType;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.events.DieRolledEvent;
import mage.game.events.GameEvent;
import mage.target.common.TargetCreaturePermanent;

/**
 * @author The XMage Developers
 */
public final class TenWizardsMountainPlane extends Plane {

    public TenWizardsMountainPlane() {
        this.setPlaneType(Planes.PLANE_TEN_WIZARDS_MOUNTAIN);

        // Whenever you roll the planar die, put a +1/+1 counter on up to one target creature.
        Ability ability = new TenWizardsMountainTriggeredAbility();
        ability.addTarget(new TargetCreaturePermanent(0, 1));
        this.getAbilities().add(ability);

        // Whenever chaos ensues, creatures you control gain flying until end of turn.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(new GainAbilityControlledEffect(
                FlyingAbility.getInstance(), Duration.EndOfTurn, StaticFilters.FILTER_CONTROLLED_CREATURES
        ), false));
    }

    private TenWizardsMountainPlane(final TenWizardsMountainPlane plane) {
        super(plane);
    }

    @Override
    public TenWizardsMountainPlane copy() {
        return new TenWizardsMountainPlane(this);
    }
}

class TenWizardsMountainTriggeredAbility extends TriggeredAbilityImpl {

    TenWizardsMountainTriggeredAbility() {
        super(Zone.COMMAND, new AddCountersTargetEffect(CounterType.P1P1.createInstance()), false);
        setTriggerPhrase("Whenever you roll the planar die, ");
    }

    private TenWizardsMountainTriggeredAbility(final TenWizardsMountainTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public TenWizardsMountainTriggeredAbility copy() {
        return new TenWizardsMountainTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DIE_ROLLED;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return game.getState().hasFaceUpPlane(Planes.PLANE_TEN_WIZARDS_MOUNTAIN)
                && isControlledBy(event.getTargetId())
                && ((DieRolledEvent) event).getRollDieType() == RollDieType.PLANAR;
    }
}
