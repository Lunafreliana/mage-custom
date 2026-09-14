package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.ReturnFromGraveyardToHandTargetEffect;
import mage.abilities.effects.common.cost.SpellsCostReductionControllerEffect;
import mage.cards.Card;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.filter.StaticFilters;
import mage.filter.predicate.card.MultiversalHighCouncilPredicate;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.EnumSet;
import java.util.UUID;

/**
 * @author The XMage Developers
 */
public final class MultiversalHighCouncilPlane extends Plane {

    private static final FilterCard universesBeyondFilter = new FilterCard("Universes Beyond spells");

    static {
        universesBeyondFilter.add(MultiversalHighCouncilPredicate.instance);
    }

    public MultiversalHighCouncilPlane() {
        this.setPlaneType(Planes.PLANE_MULTIVERSAL_HIGH_COUNCIL);

        // Universes Beyond spells you cast cost {1} less to cast.
        this.getAbilities().add(new SimpleStaticAbility(Zone.COMMAND,
                new SpellsCostReductionControllerEffect(universesBeyondFilter, 1)));

        // Each creature gets +1/+1 for each different Universe among permanents its controller controls.
        this.getAbilities().add(new SimpleStaticAbility(Zone.COMMAND,
                new MultiversalHighCouncilBoostEffect()));

        // Whenever chaos ensues, for each universe, return up to one target card from that
        // Universe from your graveyard to your hand.
        Ability ability = new ChaosEnsuesTriggeredAbility(
                new ReturnFromGraveyardToHandTargetEffect(), false);
        ability.addTarget(new MultiversalHighCouncilTarget());
        this.getAbilities().add(ability);
    }

    private MultiversalHighCouncilPlane(final MultiversalHighCouncilPlane plane) {
        super(plane);
    }

    @Override
    public MultiversalHighCouncilPlane copy() {
        return new MultiversalHighCouncilPlane(this);
    }
}

class MultiversalHighCouncilBoostEffect extends ContinuousEffectImpl {

    MultiversalHighCouncilBoostEffect() {
        super(Duration.WhileOnBattlefield, Layer.PTChangingEffects_7, SubLayer.ModifyPT_7c, Outcome.BoostCreature);
        staticText = "Each creature gets +1/+1 for each different Universe among permanents its controller controls. "
                + "<i>(Magic: The Gathering and D&amp;D each count as a universe.)</i>";
    }

    private MultiversalHighCouncilBoostEffect(final MultiversalHighCouncilBoostEffect effect) {
        super(effect);
    }

    @Override
    public MultiversalHighCouncilBoostEffect copy() {
        return new MultiversalHighCouncilBoostEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        for (Permanent creature : game.getBattlefield().getActivePermanents(
                StaticFilters.FILTER_PERMANENT_CREATURES, source.getControllerId(), source, game)) {
            EnumSet<CardUniverse> universes = EnumSet.noneOf(CardUniverse.class);
            for (Permanent permanent : game.getBattlefield().getAllActivePermanents(creature.getControllerId())) {
                universes.add(CardUniverse.from(permanent));
            }
            creature.addPower(universes.size());
            creature.addToughness(universes.size());
        }
        return true;
    }
}

class MultiversalHighCouncilTarget extends TargetCardInYourGraveyard {

    MultiversalHighCouncilTarget() {
        super(0, CardUniverse.values().length);
        withTargetName("cards from your graveyard, no more than one from each Universe");
    }

    private MultiversalHighCouncilTarget(final MultiversalHighCouncilTarget target) {
        super(target);
    }

    @Override
    public MultiversalHighCouncilTarget copy() {
        return new MultiversalHighCouncilTarget(this);
    }

    @Override
    public boolean canTarget(UUID id, Ability source, Game game) {
        if (!super.canTarget(id, source, game)) {
            return false;
        }
        Card candidate = game.getCard(id);
        CardUniverse universe = CardUniverse.from(candidate);
        return targets.keySet().stream()
                .map(game::getCard)
                .noneMatch(card -> CardUniverse.from(card) == universe);
    }

    @Override
    public boolean canTarget(UUID playerId, UUID id, Ability source, Game game) {
        if (!super.canTarget(playerId, id, source, game)) {
            return false;
        }
        Card candidate = game.getCard(id);
        CardUniverse universe = CardUniverse.from(candidate);
        return targets.keySet().stream()
                .map(game::getCard)
                .noneMatch(card -> CardUniverse.from(card) == universe);
    }
}
