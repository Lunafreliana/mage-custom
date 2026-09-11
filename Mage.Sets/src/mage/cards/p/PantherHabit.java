package mage.cards.p;

import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.PreventionEffectData;
import mage.abilities.effects.PreventionEffectImpl;
import mage.abilities.keyword.EquipAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;

import java.util.UUID;

/**
 * @author VibecodingQueens
 */
public final class PantherHabit extends CardImpl {

    public PantherHabit(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{4}");

        this.subtype.add(SubType.EQUIPMENT);

        // If equipped creature would be dealt damage, prevent that damage and put that many +1/+1 counters on it.
        this.addAbility(new SimpleStaticAbility(new PantherHabitEffect()));

        // Equip {2}
        this.addAbility(new EquipAbility(2, false));
    }

    private PantherHabit(final PantherHabit card) {
        super(card);
    }

    @Override
    public PantherHabit copy() {
        return new PantherHabit(this);
    }
}

class PantherHabitEffect extends PreventionEffectImpl {

    PantherHabitEffect() {
        super(Duration.WhileOnBattlefield);
        staticText = "if equipped creature would be dealt damage, prevent that damage "
                + "and put that many +1/+1 counters on it";
    }

    private PantherHabitEffect(final PantherHabitEffect effect) {
        super(effect);
    }

    @Override
    public PantherHabitEffect copy() {
        return new PantherHabitEffect(this);
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        Permanent equipment = game.getPermanent(source.getSourceId());
        return equipment != null
                && equipment.getAttachedTo() != null
                && equipment.getAttachedTo().equals(event.getTargetId());
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        PreventionEffectData preventionData = preventDamageAction(event, source, game);
        Permanent equipment = game.getPermanent(source.getSourceId());
        Permanent creature = equipment == null ? null : game.getPermanent(equipment.getAttachedTo());
        if (creature != null && preventionData.getPreventedDamage() > 0) {
            creature.addCounters(
                    CounterType.P1P1.createInstance(preventionData.getPreventedDamage()),
                    source.getControllerId(), source, game
            );
        }
        return false;
    }
}
