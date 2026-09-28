package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.command.PlaneswalkContext;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.ZombieToken;
import mage.players.Player;
import mage.target.TargetPlayer;
import mage.target.common.TargetSacrifice;
import mage.target.targetpointer.EachTargetPointer;

import java.util.UUID;

/**
 * @author OpenAI
 */
public final class LairOfTheAshenIdolPlane extends Plane {

    public LairOfTheAshenIdolPlane() {
        this.setPlaneType(Planes.PLANE_LAIR_OF_THE_ASHEN_IDOL);

        // At the beginning of your upkeep, sacrifice a creature. If you can't, planeswalk.
        this.getAbilities().add(new BeginningOfUpkeepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU, new LairOfTheAshenIdolUpkeepEffect(), false
        ));

        // Whenever chaos ensues, any number of target players each create a 2/2 black Zombie creature token.
        Ability ability = new ChaosEnsuesTriggeredAbility(new LairOfTheAshenIdolChaosEffect(), false);
        ability.addTarget(new TargetPlayer(0, Integer.MAX_VALUE, false));
        this.getAbilities().add(ability);
    }

    private LairOfTheAshenIdolPlane(final LairOfTheAshenIdolPlane plane) {
        super(plane);
    }

    @Override
    public LairOfTheAshenIdolPlane copy() {
        return new LairOfTheAshenIdolPlane(this);
    }
}

class LairOfTheAshenIdolUpkeepEffect extends OneShotEffect {

    LairOfTheAshenIdolUpkeepEffect() {
        super(Outcome.Sacrifice);
        staticText = "sacrifice a creature. If you can't, planeswalk";
    }

    private LairOfTheAshenIdolUpkeepEffect(final LairOfTheAshenIdolUpkeepEffect effect) {
        super(effect);
    }

    @Override
    public LairOfTheAshenIdolUpkeepEffect copy() {
        return new LairOfTheAshenIdolUpkeepEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }

        TargetSacrifice target = new TargetSacrifice(StaticFilters.FILTER_PERMANENT_CREATURE);
        if (target.canChoose(controller.getId(), source, game)) {
            controller.choose(Outcome.Sacrifice, target, source, game);
            Permanent creature = game.getPermanent(target.getFirstTarget());
            return creature != null && creature.sacrifice(source, game);
        }

        return game.planeswalk(new PlaneswalkContext(
                controller.getId(), PlaneswalkContext.Cause.SPELL_OR_ABILITY, source.getSourceId()
        ));
    }
}

class LairOfTheAshenIdolChaosEffect extends OneShotEffect {

    LairOfTheAshenIdolChaosEffect() {
        super(Outcome.PutCreatureInPlay);
        setTargetPointer(new EachTargetPointer());
        staticText = "any number of target players each create a 2/2 black Zombie creature token";
    }

    private LairOfTheAshenIdolChaosEffect(final LairOfTheAshenIdolChaosEffect effect) {
        super(effect);
    }

    @Override
    public LairOfTheAshenIdolChaosEffect copy() {
        return new LairOfTheAshenIdolChaosEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        boolean created = false;
        for (UUID playerId : getTargetPointer().getTargets(game, source)) {
            if (game.getPlayer(playerId) != null) {
                created |= new ZombieToken().putOntoBattlefield(1, game, source, playerId);
            }
        }
        return created || getTargetPointer().getTargets(game, source).isEmpty();
    }
}
