package mage.game.command.planes;

import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.RestrictionEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.constants.*;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.mageobject.PowerPredicate;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.EldraziAnnihilatorToken;

/**
 * @author VibecodingQueens
 */
public class HedronFieldsOfAgadeemPlane extends Plane {

    public HedronFieldsOfAgadeemPlane() {
        this.setPlaneType(Planes.PLANE_HEDRON_FIELDS_OF_AGADEEM);

        // Creatures with power 7 or greater can't attack or block 
        Ability ability = new SimpleStaticAbility(Zone.COMMAND, new HedronFieldsOfAgadeemRestrictionEffect());
        this.getAbilities().add(ability);

        // Whenever chaos ensues, create a 7/7 colorless Eldrazi creature with annhilator 1
        Effect chaosEffect = new CreateTokenEffect(new EldraziAnnihilatorToken());

        ChaosEnsuesTriggeredAbility chaosAbility = new ChaosEnsuesTriggeredAbility(chaosEffect, false);
        this.getAbilities().add(chaosAbility);
    }

    private HedronFieldsOfAgadeemPlane(final HedronFieldsOfAgadeemPlane plane) {
        super(plane);
    }

    @Override
    public HedronFieldsOfAgadeemPlane copy() {
        return new HedronFieldsOfAgadeemPlane(this);
    }
}

class HedronFieldsOfAgadeemRestrictionEffect extends RestrictionEffect {

    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent("creatures with power 7 or greater");

    static {
        filter.add(new PowerPredicate(ComparisonType.MORE_THAN, 6));
    }

    public HedronFieldsOfAgadeemRestrictionEffect() {
        super(Duration.Custom);
        staticText = "Creatures with power 7 or greater can't attack or block";
    }

    protected HedronFieldsOfAgadeemRestrictionEffect(final HedronFieldsOfAgadeemRestrictionEffect effect) {
        super(effect);
    }

    @Override
    public HedronFieldsOfAgadeemRestrictionEffect copy() {
        return new HedronFieldsOfAgadeemRestrictionEffect(this);
    }

    @Override
    public boolean canAttack(Game game, boolean canUseChooseDialogs) {
        return false;
    }

    @Override
    public boolean canBlock(Permanent attacker, Permanent blocker, Ability source, Game game, boolean canUseChooseDialogs) {
        return false;
    }

    @Override
    public boolean applies(Permanent permanent, Ability source, Game game) {
        if (!game.getState().hasFaceUpPlane(Planes.PLANE_HEDRON_FIELDS_OF_AGADEEM)) {
            return false;
        }
        return filter.match(permanent, source.getControllerId(), source, game);
    }
}
