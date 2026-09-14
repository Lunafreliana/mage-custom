package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.common.CommittedCrimeTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DamageTargetEffect;
import mage.constants.Outcome;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.events.GameEvent;
import mage.players.Player;
import mage.target.common.TargetAnyTarget;

import java.util.UUID;

/**
 * @author The XMage Developers
 */
public final class TarnationPlane extends Plane {

    public TarnationPlane() {
        this.setPlaneType(Planes.PLANE_TARNATION);

        // Whenever a player commits a crime, they may draw a card.
        this.getAbilities().add(new TarnationCrimeTriggeredAbility());

        // Whenever chaos ensues, Tarnation deals 1 damage to any target.
        Ability ability = new ChaosEnsuesTriggeredAbility(new DamageTargetEffect(1), false);
        ability.addTarget(new TargetAnyTarget());
        this.getAbilities().add(ability);
    }

    private TarnationPlane(final TarnationPlane plane) {
        super(plane);
    }

    @Override
    public TarnationPlane copy() {
        return new TarnationPlane(this);
    }
}

class TarnationCrimeTriggeredAbility extends CommittedCrimeTriggeredAbility {

    TarnationCrimeTriggeredAbility() {
        super(Zone.COMMAND, new TarnationCrimeEffect(), false);
        setTriggerPhrase("Whenever a player commits a crime, ");
    }

    private TarnationCrimeTriggeredAbility(final TarnationCrimeTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public TarnationCrimeTriggeredAbility copy() {
        return new TarnationCrimeTriggeredAbility(this);
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        UUID criminalId = getCriminal(event, game);
        if (criminalId == null) {
            return false;
        }
        getEffects().setValue("criminalId", criminalId);
        return true;
    }
}

class TarnationCrimeEffect extends OneShotEffect {

    TarnationCrimeEffect() {
        super(Outcome.DrawCard);
        staticText = "they may draw a card. <i>(Targeting opponents, anything they control, "
                + "and/or cards in their graveyards is a crime.)</i>";
    }

    private TarnationCrimeEffect(final TarnationCrimeEffect effect) {
        super(effect);
    }

    @Override
    public TarnationCrimeEffect copy() {
        return new TarnationCrimeEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player criminal = game.getPlayer((UUID) getValue("criminalId"));
        if (criminal != null && criminal.chooseUse(outcome, "Draw a card?", source, game)) {
            criminal.drawCards(1, source, game);
        }
        return true;
    }
}
