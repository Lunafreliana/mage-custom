package mage.game.command.planes;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenCopyTargetEffect;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;
import mage.target.common.TargetSacrifice;

import java.util.UUID;

/**
 * @author The XMage Developers
 */
public final class KharashaFoothillsPlane extends Plane {

    public KharashaFoothillsPlane() {
        this.setPlaneType(Planes.PLANE_KHARASHA_FOOTHILLS);

        // Whenever a creature you control attacks a player, for each other opponent, you may create a token
        // that's a copy of that creature, tapped and attacking that opponent. Exile those tokens at the
        // beginning of the next end step.
        this.getAbilities().add(new KharashaFoothillsAttacksTriggeredAbility());

        // Whenever chaos ensues, you may sacrifice any number of creatures. If you do, Kharasha Foothills
        // deals that much damage to target creature.
        Ability ability = new ChaosEnsuesTriggeredAbility(new KharashaFoothillsChaosEffect(), false);
        ability.addTarget(new TargetPermanent(StaticFilters.FILTER_PERMANENT_CREATURE));
        this.getAbilities().add(ability);
    }

    private KharashaFoothillsPlane(final KharashaFoothillsPlane plane) {
        super(plane);
    }

    @Override
    public KharashaFoothillsPlane copy() {
        return new KharashaFoothillsPlane(this);
    }
}

class KharashaFoothillsAttacksTriggeredAbility extends TriggeredAbilityImpl {

    KharashaFoothillsAttacksTriggeredAbility() {
        super(Zone.COMMAND, new KharashaFoothillsTokenEffect());
        setTriggerPhrase("Whenever a creature you control attacks a player, ");
    }

    private KharashaFoothillsAttacksTriggeredAbility(final KharashaFoothillsAttacksTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public KharashaFoothillsAttacksTriggeredAbility copy() {
        return new KharashaFoothillsAttacksTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ATTACKER_DECLARED;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        Permanent attacker = game.getPermanent(event.getSourceId());
        UUID defenderId = game.getCombat().getDefenderId(event.getSourceId());
        if (attacker == null || !attacker.isControlledBy(getControllerId())
                || game.getPlayer(defenderId) == null) {
            return false;
        }
        this.getEffects().setValue("attackerRef", new MageObjectReference(attacker, game));
        this.getEffects().setValue("defendingPlayerId", defenderId);
        return true;
    }
}

class KharashaFoothillsTokenEffect extends OneShotEffect {

    KharashaFoothillsTokenEffect() {
        super(Outcome.Benefit);
        staticText = "for each other opponent, you may create a token that's a copy of that creature, tapped "
                + "and attacking that opponent. Exile those tokens at the beginning of the next end step";
    }

    private KharashaFoothillsTokenEffect(final KharashaFoothillsTokenEffect effect) {
        super(effect);
    }

    @Override
    public KharashaFoothillsTokenEffect copy() {
        return new KharashaFoothillsTokenEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        MageObjectReference attackerRef = (MageObjectReference) getValue("attackerRef");
        UUID defendingPlayerId = (UUID) getValue("defendingPlayerId");
        Permanent attacker = attackerRef == null ? null : attackerRef.getPermanentOrLKIBattlefield(game);
        if (controller == null || attacker == null || defendingPlayerId == null) {
            return false;
        }

        for (UUID playerId : game.getState().getPlayersInRange(controller.getId(), game)) {
            Player opponent = game.getPlayer(playerId);
            if (playerId.equals(defendingPlayerId) || opponent == null
                    || !controller.hasOpponent(playerId, game)
                    || !controller.chooseUse(Outcome.PutCreatureInPlay,
                    "Create a token copy attacking " + opponent.getLogName() + "?", source, game)) {
                continue;
            }
            CreateTokenCopyTargetEffect effect = new CreateTokenCopyTargetEffect(
                    controller.getId(), null, false, 1, true, true, playerId
            );
            effect.setSavedPermanent(attacker);
            if (effect.apply(game, source)) {
                effect.exileTokensCreatedAtNextEndStep(game, source);
            }
        }
        return true;
    }
}

class KharashaFoothillsChaosEffect extends OneShotEffect {

    KharashaFoothillsChaosEffect() {
        super(Outcome.Damage);
        staticText = "you may sacrifice any number of creatures. If you do, {this} deals that much damage "
                + "to target creature";
    }

    private KharashaFoothillsChaosEffect(final KharashaFoothillsChaosEffect effect) {
        super(effect);
    }

    @Override
    public KharashaFoothillsChaosEffect copy() {
        return new KharashaFoothillsChaosEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        UUID targetId = source.getFirstTarget();
        if (controller == null || game.getPermanent(targetId) == null
                || !controller.chooseUse(Outcome.Sacrifice, "Sacrifice any number of creatures?", source, game)) {
            return false;
        }

        TargetSacrifice sacrifice = new TargetSacrifice(
                0, Integer.MAX_VALUE, StaticFilters.FILTER_PERMANENT_CREATURE
        );
        controller.choose(Outcome.Sacrifice, sacrifice, source, game);
        int sacrificed = 0;
        for (UUID permanentId : sacrifice.getTargets()) {
            Permanent permanent = game.getPermanent(permanentId);
            if (permanent != null && permanent.sacrifice(source, game)) {
                sacrificed++;
            }
        }
        Permanent target = game.getPermanent(targetId);
        if (target != null) {
            target.damage(sacrificed, source.getSourceId(), source, game, false, true);
        }
        return true;
    }
}
