package mage.cards.o;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.keyword.EmpowerJaceEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author muz
 */
public final class OverwriteTheMultiverse extends CardImpl {

    public OverwriteTheMultiverse(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{4}{B}{B}");

        // Exile all creatures. Empower Jace X, where X is the number of creatures exiled this way.
        this.getSpellAbility().addEffect(new OverwriteTheMultiverseEffect());
    }

    private OverwriteTheMultiverse(final OverwriteTheMultiverse card) {
        super(card);
    }

    @Override
    public OverwriteTheMultiverse copy() {
        return new OverwriteTheMultiverse(this);
    }
}

class OverwriteTheMultiverseEffect extends OneShotEffect {

    OverwriteTheMultiverseEffect() {
        super(Outcome.Exile);
        staticText = "exile all creatures. Empower Jace X, where X is the number of creatures exiled this way. "
                + "<i>(Put that many loyalty counters on a Jace token you control. If you don't control one, "
                + "first create a blue Jace planeswalker token with \"[−1]: Surveil 1\" and \"[−3]: Draw a card.\")</i>";
    }

    private OverwriteTheMultiverseEffect(final OverwriteTheMultiverseEffect effect) {
        super(effect);
    }

    @Override
    public OverwriteTheMultiverseEffect copy() {
        return new OverwriteTheMultiverseEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        List<Permanent> creatures = game.getBattlefield().getActivePermanents(
                StaticFilters.FILTER_PERMANENT_CREATURES, source.getControllerId(), source, game
        );
        List<UUID> creatureIds = creatures
                .stream().map(Permanent::getId).collect(Collectors.toList());
        Cards cards = new CardsImpl();
        creatures.forEach(cards::add);
        controller.moveCards(cards, Zone.EXILED, source, game);
        int exiled = (int) creatureIds.stream()
                .filter(id -> game.getState().getZone(id) == Zone.EXILED)
                .count();
        return EmpowerJaceEffect.doEmpower(exiled, game, source) != null;
    }
}
