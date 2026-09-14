package mage.abilities.effects.common.counter;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.choices.Choice;
import mage.choices.ChoiceImpl;
import mage.constants.Outcome;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adds one counter, chosen as this effect resolves, to the targeted permanent.
 */
public class AddCounterChoiceTargetEffect extends OneShotEffect {

    private final List<CounterType> counterTypes;

    public AddCounterChoiceTargetEffect(CounterType... counterTypes) {
        super(Outcome.Benefit);
        if (counterTypes.length < 2) {
            throw new IllegalArgumentException("At least two counter types are required");
        }
        this.counterTypes = Arrays.asList(counterTypes);
        List<String> names = this.counterTypes.stream()
                .map(CounterType::toString)
                .collect(Collectors.toList());
        staticText = "put your choice of a " + CardUtil.concatWithOr(names) + " counter on target permanent";
    }

    private AddCounterChoiceTargetEffect(final AddCounterChoiceTargetEffect effect) {
        super(effect);
        this.counterTypes = new ArrayList<>(effect.counterTypes);
    }

    @Override
    public AddCounterChoiceTargetEffect copy() {
        return new AddCounterChoiceTargetEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (controller == null || permanent == null) {
            return false;
        }
        Set<String> choices = counterTypes.stream()
                .map(type -> CardUtil.getTextWithFirstCharUpperCase(type.getName()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Choice choice = new ChoiceImpl(true);
        choice.setMessage("Choose counter type");
        choice.setChoices(choices);
        if (!controller.choose(outcome, choice, game)) {
            return false;
        }
        CounterType chosen = CounterType.findByName(choice.getChoice().toLowerCase(Locale.ENGLISH));
        return chosen != null && counterTypes.contains(chosen)
                && permanent.addCounters(chosen.createInstance(), source.getControllerId(), source, game);
    }
}
