package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.EntersBattlefieldAllTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenCopyTargetEffect;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.SetTargetPointer;
import mage.constants.Zone;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.permanent.TokenPredicate;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCreaturePermanent;
import mage.util.CardUtil;

/**
 * @author The XMage Developers
 */
public final class IsleOfVesuvaPlane extends Plane {

    private static final FilterCreaturePermanent FILTER =
            new FilterCreaturePermanent("nontoken creature");

    static {
        FILTER.add(TokenPredicate.FALSE);
    }

    public IsleOfVesuvaPlane() {
        this.setPlaneType(Planes.PLANE_ISLE_OF_VESUVA);

        // Whenever a nontoken creature enters, its controller creates a token that's a copy of that creature.
        this.getAbilities().add(new EntersBattlefieldAllTriggeredAbility(
                Zone.COMMAND, new IsleOfVesuvaCopyEffect(), FILTER, false, SetTargetPointer.PERMANENT
        ));

        // Whenever chaos ensues, destroy target creature and all other creatures with the same name as that creature.
        Ability ability = new ChaosEnsuesTriggeredAbility(new IsleOfVesuvaChaosEffect(), false);
        ability.addTarget(new TargetCreaturePermanent());
        this.getAbilities().add(ability);
    }

    private IsleOfVesuvaPlane(final IsleOfVesuvaPlane plane) {
        super(plane);
    }

    @Override
    public IsleOfVesuvaPlane copy() {
        return new IsleOfVesuvaPlane(this);
    }
}

class IsleOfVesuvaCopyEffect extends OneShotEffect {

    IsleOfVesuvaCopyEffect() {
        super(Outcome.PutCardInPlay);
        staticText = "its controller creates a token that's a copy of that creature";
    }

    private IsleOfVesuvaCopyEffect(final IsleOfVesuvaCopyEffect effect) {
        super(effect);
    }

    @Override
    public IsleOfVesuvaCopyEffect copy() {
        return new IsleOfVesuvaCopyEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent creature = getTargetPointer().getFirstTargetPermanentOrLKI(game, source);
        if (creature == null) {
            return false;
        }
        CreateTokenCopyTargetEffect effect = new CreateTokenCopyTargetEffect(creature.getControllerId());
        effect.setTargetPointer(getTargetPointer().copy());
        return effect.apply(game, source);
    }
}

class IsleOfVesuvaChaosEffect extends OneShotEffect {

    IsleOfVesuvaChaosEffect() {
        super(Outcome.DestroyPermanent);
    }

    private IsleOfVesuvaChaosEffect(final IsleOfVesuvaChaosEffect effect) {
        super(effect);
    }

    @Override
    public IsleOfVesuvaChaosEffect copy() {
        return new IsleOfVesuvaChaosEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent target = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (target == null) {
            return false;
        }
        if (CardUtil.haveEmptyName(target)) {
            return target.destroy(source, game, false);
        }
        for (Permanent permanent : game.getBattlefield().getAllActivePermanents()) {
            if (permanent.isCreature(game) && CardUtil.haveSameNames(permanent, target)) {
                permanent.destroy(source, game, false);
            }
        }
        return true;
    }

    @Override
    public String getText(Mode mode) {
        return "destroy target creature and all other creatures with the same name as that creature";
    }
}
