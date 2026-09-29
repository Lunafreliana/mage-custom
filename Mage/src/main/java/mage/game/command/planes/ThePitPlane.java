package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.SacrificeAllEffect;
import mage.choices.Choice;
import mage.choices.ChoiceImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.permanent.token.Angel33Token;
import mage.game.permanent.token.BelzenlokDemonToken;
import mage.game.permanent.token.Token;
import mage.players.Player;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author The XMage Developers
 */
public final class ThePitPlane extends Plane {

    private static final FilterPermanent FILTER_NONARTIFACT_CREATURE
            = new FilterCreaturePermanent("nonartifact creature");

    static {
        FILTER_NONARTIFACT_CREATURE.add(Predicates.not(CardType.ARTIFACT.getPredicate()));
    }

    public ThePitPlane() {
        this.setPlaneType(Planes.PLANE_THE_PIT);

        // When you planeswalk to The Pit, each player creates their choice of a 3/3 white Angel
        // creature token with flying or a 6/6 black Demon creature token with flying, trample, and
        // "At the beginning of your upkeep, sacrifice another creature. If you can't, this token
        // deals 6 damage to you."
        this.getAbilities().add(new PlaneswalkToSourceTriggeredAbility(new ThePitCreateTokenEffect()));

        // Whenever chaos ensues, each player sacrifices a nonartifact creature of their choice.
        this.getAbilities().add(new ChaosEnsuesTriggeredAbility(
                new SacrificeAllEffect(FILTER_NONARTIFACT_CREATURE), false
        ));
    }

    private ThePitPlane(final ThePitPlane plane) {
        super(plane);
    }

    @Override
    public ThePitPlane copy() {
        return new ThePitPlane(this);
    }
}

class ThePitCreateTokenEffect extends OneShotEffect {

    private static final String ANGEL = "3/3 white Angel creature token with flying";
    private static final String DEMON = "6/6 black Demon creature token with flying and trample";
    // Keep the printed Angel-then-Demon order stable in clients and deterministic tests.
    private static final Set<String> CHOICES = new LinkedHashSet<>(Arrays.asList(ANGEL, DEMON));

    ThePitCreateTokenEffect() {
        super(Outcome.PutCreatureInPlay);
        staticText = "each player creates their choice of a 3/3 white Angel creature token with flying "
                + "or a 6/6 black Demon creature token with flying, trample, and \"At the beginning of "
                + "your upkeep, sacrifice another creature. If you can't, this token deals 6 damage to you.\"";
    }

    private ThePitCreateTokenEffect(final ThePitCreateTokenEffect effect) {
        super(effect);
    }

    @Override
    public ThePitCreateTokenEffect copy() {
        return new ThePitCreateTokenEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        boolean result = false;
        for (UUID playerId : game.getState().getPlayersInRange(source.getControllerId(), game)) {
            Player player = game.getPlayer(playerId);
            if (player == null) {
                continue;
            }
            Choice choice = new ChoiceImpl(true);
            choice.setMessage("Choose a creature token to create");
            choice.setChoices(CHOICES);
            if (!player.choose(outcome, choice, game)) {
                continue;
            }
            Token token = ANGEL.equals(choice.getChoice()) ? new Angel33Token() : new BelzenlokDemonToken();
            result |= token.putOntoBattlefield(1, game, source, playerId);
        }
        return result;
    }
}
