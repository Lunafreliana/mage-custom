package mage.cards.p;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.ActivateIfConditionActivatedAbility;
import mage.abilities.common.EntersBattlefieldThisOrAnotherTriggeredAbility;
import mage.abilities.condition.Condition;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ReturnSourceFromGraveyardToBattlefieldWithCounterEffect;
import mage.abilities.effects.keyword.SurveilEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.WatcherScope;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.watchers.Watcher;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author muz
 */
public final class ProctorOfPotential extends CardImpl {

    public ProctorOfPotential(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{W}{U}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.CLERIC);
        this.power = new MageInt(3);
        this.toughness = new MageInt(1);

        // Whenever this creature or another creature you control enters, surveil 1.
        this.addAbility(new EntersBattlefieldThisOrAnotherTriggeredAbility(
                new SurveilEffect(1), StaticFilters.FILTER_PERMANENT_CREATURE, false, true
        ));

        // {W}{U}: Return this card from your graveyard to the battlefield with a finality counter on it. Activate only if you've scried or surveilled this turn.
        this.addAbility(new ActivateIfConditionActivatedAbility(
                Zone.GRAVEYARD,
                new ReturnSourceFromGraveyardToBattlefieldWithCounterEffect(
                        CounterType.FINALITY.createInstance(), false
                ), new ManaCostsImpl<>("{W}{U}"), ProctorOfPotentialCondition.instance
        ), new ProctorOfPotentialWatcher());
    }

    private ProctorOfPotential(final ProctorOfPotential card) {
        super(card);
    }

    @Override
    public ProctorOfPotential copy() {
        return new ProctorOfPotential(this);
    }
}

enum ProctorOfPotentialCondition implements Condition {
    instance;

    @Override
    public boolean apply(Game game, Ability source) {
        ProctorOfPotentialWatcher watcher = game.getState().getWatcher(ProctorOfPotentialWatcher.class);
        return watcher != null && watcher.hasScriedOrSurveilled(source.getControllerId());
    }

    @Override
    public String toString() {
        return "if you've scried or surveilled this turn";
    }
}

class ProctorOfPotentialWatcher extends Watcher {

    private final Set<UUID> players = new HashSet<>();

    ProctorOfPotentialWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.SCRIED
                || event.getType() == GameEvent.EventType.SURVEILED) {
            players.add(event.getPlayerId());
        }
    }

    @Override
    public void reset() {
        super.reset();
        players.clear();
    }

    boolean hasScriedOrSurveilled(UUID playerId) {
        return players.contains(playerId);
    }
}
