package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.PlaneswalkToSourceTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.keyword.BolsterEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.permanent.Permanent;
import mage.target.Target;
import mage.target.TargetPermanent;
import mage.target.common.TargetControlledCreaturePermanent;

import java.util.UUID;

/**
 * @author The XMage Developers
 */
public final class TheGreatAeriePlane extends Plane {

    public TheGreatAeriePlane() {
        this.setPlaneType(Planes.PLANE_THE_GREAT_AERIE);

        // When you planeswalk to The Great Aerie and at the beginning of your upkeep, bolster 3.
        this.getAbilities().add(new PlaneswalkToSourceTriggeredAbility(new BolsterEffect(3)));
        this.getAbilities().add(new BeginningOfUpkeepTriggeredAbility(
                Zone.COMMAND, TargetController.YOU, new BolsterEffect(3), false
        ));

        // Whenever chaos ensues, choose up to one target creature you control and up to one target
        // creature an opponent controls. Each of those creatures deals damage equal to its toughness to the other.
        Ability ability = new ChaosEnsuesTriggeredAbility(new TheGreatAerieDamageEffect(), false);
        ability.addTarget(new TargetControlledCreaturePermanent(0, 1));
        ability.addTarget(new TargetPermanent(
                0, 1, StaticFilters.FILTER_OPPONENTS_PERMANENT_A_CREATURE
        ));
        this.getAbilities().add(ability);
    }

    private TheGreatAeriePlane(final TheGreatAeriePlane plane) {
        super(plane);
    }

    @Override
    public TheGreatAeriePlane copy() {
        return new TheGreatAeriePlane(this);
    }
}

class TheGreatAerieDamageEffect extends OneShotEffect {

    TheGreatAerieDamageEffect() {
        super(Outcome.Damage);
        staticText = "choose up to one target creature you control and up to one target creature an opponent "
                + "controls. Each of those creatures deals damage equal to its toughness to the other";
    }

    private TheGreatAerieDamageEffect(final TheGreatAerieDamageEffect effect) {
        super(effect);
    }

    @Override
    public TheGreatAerieDamageEffect copy() {
        return new TheGreatAerieDamageEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        if (source.getTargets().size() < 2) {
            return false;
        }
        Target firstTarget = source.getTargets().get(0);
        Target secondTarget = source.getTargets().get(1);
        UUID firstId = firstTarget.getFirstTarget();
        UUID secondId = secondTarget.getFirstTarget();
        if (firstId == null || secondId == null
                || !firstTarget.isLegal(source, game) || !secondTarget.isLegal(source, game)) {
            return false;
        }
        Permanent first = game.getPermanent(firstId);
        Permanent second = game.getPermanent(secondId);
        if (first == null || second == null || !first.isCreature(game) || !second.isCreature(game)) {
            return false;
        }

        int firstToughness = first.getToughness().getValue();
        int secondToughness = second.getToughness().getValue();
        second.damage(firstToughness, firstId, source, game, false, true);
        first.damage(secondToughness, secondId, source, game, false, true);
        return true;
    }

    @Override
    public String getText(Mode mode) {
        return staticText;
    }
}
