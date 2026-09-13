package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.condition.Condition;
import mage.abilities.condition.common.PermanentsOnTheBattlefieldCondition;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.keyword.ManifestDreadEffect;
import mage.abilities.triggers.BeginningOfSecondMainTriggeredAbility;
import mage.cards.Card;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.permanent.TappedPredicate;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetCreaturePermanent;
import mage.util.CardUtil;

/**
 * @author The XMage Developers
 */
public final class PursuedBySomethingPlane extends Plane {

    private static final FilterPermanent filter
            = new FilterControlledCreaturePermanent("you control a tapped creature");

    static {
        filter.add(TappedPredicate.TAPPED);
    }

    private static final Condition condition = new PermanentsOnTheBattlefieldCondition(filter);

    public PursuedBySomethingPlane() {
        this.setPlaneType(Planes.PLANE_PURSUED_BY_SOMETHING);

        // At the beginning of your second main phase, if you control a tapped creature, manifest dread.
        this.getAbilities().add(new BeginningOfSecondMainTriggeredAbility(
                Zone.COMMAND, TargetController.YOU, new ManifestDreadEffect(), false
        ).withInterveningIf(condition));

        // Whenever chaos ensues, exile target creature. If it's an instant or sorcery, cast it without
        // paying its mana cost. Otherwise put it onto the battlefield under its owner's control.
        Ability ability = new ChaosEnsuesTriggeredAbility(new PursuedBySomethingEffect(), false);
        ability.addTarget(new TargetCreaturePermanent());
        this.getAbilities().add(ability);
    }

    private PursuedBySomethingPlane(final PursuedBySomethingPlane plane) {
        super(plane);
    }

    @Override
    public PursuedBySomethingPlane copy() {
        return new PursuedBySomethingPlane(this);
    }
}

class PursuedBySomethingEffect extends OneShotEffect {

    PursuedBySomethingEffect() {
        super(Outcome.Benefit);
        staticText = "exile target creature. If it's an instant or sorcery, cast it without paying its mana cost. "
                + "Otherwise put it onto the battlefield under its owner's control";
    }

    private PursuedBySomethingEffect(final PursuedBySomethingEffect effect) {
        super(effect);
    }

    @Override
    public PursuedBySomethingEffect copy() {
        return new PursuedBySomethingEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        Player controller = game.getPlayer(source.getControllerId());
        if (permanent == null || controller == null
                || !controller.moveCards(permanent, Zone.EXILED, source, game)) {
            return false;
        }
        Card card = game.getCard(permanent.getId());
        if (card == null) {
            return false;
        }
        if (card.isInstantOrSorcery(game)) {
            return CardUtil.castSpellWithAttributesForFree(controller, source, game, card);
        }
        Player owner = game.getPlayer(card.getOwnerId());
        return owner != null && owner.moveCards(card, Zone.BATTLEFIELD, source, game);
    }
}
