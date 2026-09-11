package mage.cards.t;

import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.BatchTriggeredAbility;
import mage.abilities.TriggeredAbility;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.AttacksTriggeredAbility;
import mage.abilities.dynamicvalue.common.CountersSourceCount;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.effects.common.replacement.DiesReplacementEffect;
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
import mage.game.events.GameEvent;
import mage.game.events.PhasedOutBatchEvent;
import mage.game.events.ZoneChangeBatchEvent;
import mage.game.events.ZoneChangeEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetAnyTarget;

import java.util.UUID;

/**
 * @author VibecodingQueens
 */
public final class TheWarDoctor extends CardImpl {

    public TheWarDoctor(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{R}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.TIME_LORD);
        this.subtype.add(SubType.DOCTOR);
        this.power = new MageInt(3);
        this.toughness = new MageInt(5);

        // Whenever one or more other permanents phase out and whenever one or more other cards
        // are put into exile from anywhere, put a time counter on The War Doctor.
        this.addAbility(new TheWarDoctorPhaseOutTriggeredAbility());
        this.addAbility(new TheWarDoctorExileTriggeredAbility());

        // Whenever The War Doctor attacks, it deals damage equal to the number of time counters
        // on it to any target. If a creature dealt damage this way would die this turn, exile it instead.
        TriggeredAbility ability = new AttacksTriggeredAbility(new TheWarDoctorDamageEffect(), false);
        ability.setLeavesTheBattlefieldTrigger(true);
        ability.addTarget(new TargetAnyTarget());
        this.addAbility(ability);
    }

    private TheWarDoctor(final TheWarDoctor card) {
        super(card);
    }

    @Override
    public TheWarDoctor copy() {
        return new TheWarDoctor(this);
    }
}

class TheWarDoctorPhaseOutTriggeredAbility extends TriggeredAbilityImpl implements BatchTriggeredAbility<GameEvent> {

    TheWarDoctorPhaseOutTriggeredAbility() {
        super(Zone.BATTLEFIELD, new AddCountersSourceEffect(CounterType.TIME.createInstance()), false);
        setTriggerPhrase("Whenever one or more other permanents phase out, ");
    }

    private TheWarDoctorPhaseOutTriggeredAbility(final TheWarDoctorPhaseOutTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.PHASED_OUT_BATCH;
    }

    @Override
    public boolean checkEvent(GameEvent event, Game game) {
        return !event.getTargetId().equals(getSourceId());
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        int count = getFilteredEvents((PhasedOutBatchEvent) event, game).size();
        if (count == 0) {
            return false;
        }
        getEffects().clear();
        addEffect(new AddCountersSourceEffect(CounterType.TIME.createInstance(count)));
        return true;
    }

    @Override
    public TheWarDoctorPhaseOutTriggeredAbility copy() {
        return new TheWarDoctorPhaseOutTriggeredAbility(this);
    }
}

class TheWarDoctorExileTriggeredAbility extends TriggeredAbilityImpl implements BatchTriggeredAbility<ZoneChangeEvent> {

    TheWarDoctorExileTriggeredAbility() {
        super(Zone.BATTLEFIELD, new AddCountersSourceEffect(CounterType.TIME.createInstance()), false);
        setTriggerPhrase("Whenever one or more other cards are put into exile from anywhere, ");
        setLeavesTheBattlefieldTrigger(true);
    }

    private TheWarDoctorExileTriggeredAbility(final TheWarDoctorExileTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ZONE_CHANGE_BATCH;
    }

    @Override
    public boolean checkEvent(ZoneChangeEvent event, Game game) {
        return event.getToZone() == Zone.EXILED && !event.getTargetId().equals(getSourceId());
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        int count = getFilteredEvents((ZoneChangeBatchEvent) event, game).size();
        if (count == 0) {
            return false;
        }
        getEffects().clear();
        addEffect(new AddCountersSourceEffect(CounterType.TIME.createInstance(count)));
        return true;
    }

    @Override
    public TriggeredAbility copy() {
        return new TheWarDoctorExileTriggeredAbility(this);
    }
}

class TheWarDoctorDamageEffect extends OneShotEffect {

    private static final CountersSourceCount AMOUNT = new CountersSourceCount(CounterType.TIME);

    TheWarDoctorDamageEffect() {
        super(Outcome.Damage);
        staticText = "it deals damage equal to the number of time counters on it to any target. "
                + "If a creature dealt damage this way would die this turn, exile it instead";
    }

    private TheWarDoctorDamageEffect(final TheWarDoctorDamageEffect effect) {
        super(effect);
    }

    @Override
    public TheWarDoctorDamageEffect copy() {
        return new TheWarDoctorDamageEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        int damage = AMOUNT.calculate(game, source, this);
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent != null) {
            int damageDealt = permanent.damage(damage, source.getSourceId(), source, game, false, true);
            if (damageDealt > 0 && permanent.isCreature(game)) {
                game.addEffect(new DiesReplacementEffect(
                        new MageObjectReference(permanent, game), Duration.EndOfTurn
                ), source);
            }
            return true;
        }
        Player player = game.getPlayer(getTargetPointer().getFirst(game, source));
        return player != null && player.damage(damage, source.getSourceId(), source, game) > 0;
    }
}
