package mage.game.command.planes;

import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.ChaosEnsuesTriggeredAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.GainLifeTargetEffect;
import mage.abilities.effects.common.LoseLifeTargetEffect;
import mage.abilities.effects.common.PutOnLibraryTargetEffect;
import mage.cards.Card;
import mage.cards.CardsImpl;
import mage.constants.Planes;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.command.Plane;
import mage.game.events.GameEvent;
import mage.players.Player;
import mage.target.TargetPermanent;
import mage.target.targetpointer.FixedTarget;

/**
 * @author The XMage Developers
 */
public final class SeaOfSandPlane extends Plane {

    public SeaOfSandPlane() {
        this.setPlaneType(Planes.PLANE_SEA_OF_SAND);

        // Players reveal each card they draw.
        // Whenever a player draws a land card, that player gains 3 life.
        this.getAbilities().add(new SeaOfSandDrawTriggeredAbility(true));

        // Whenever a player draws a nonland card, that player loses 3 life.
        this.getAbilities().add(new SeaOfSandDrawTriggeredAbility(false));

        // Whenever chaos ensues, put target permanent on top of its owner's library.
        Ability ability = new ChaosEnsuesTriggeredAbility(new PutOnLibraryTargetEffect(true), false);
        ability.addTarget(new TargetPermanent());
        this.getAbilities().add(ability);
    }

    private SeaOfSandPlane(final SeaOfSandPlane plane) {
        super(plane);
    }

    @Override
    public SeaOfSandPlane copy() {
        return new SeaOfSandPlane(this);
    }
}

class SeaOfSandDrawTriggeredAbility extends TriggeredAbilityImpl {

    private final boolean land;

    SeaOfSandDrawTriggeredAbility(boolean land) {
        super(Zone.COMMAND, land ? new GainLifeTargetEffect(3) : new LoseLifeTargetEffect(3), false);
        this.land = land;
    }

    private SeaOfSandDrawTriggeredAbility(final SeaOfSandDrawTriggeredAbility ability) {
        super(ability);
        this.land = ability.land;
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DREW_CARD;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        Card card = game.getCard(event.getTargetId());
        Player player = game.getPlayer(event.getPlayerId());
        if (card == null || player == null) {
            return false;
        }
        // Only one of the paired abilities performs the mandatory reveal.
        if (land) {
            player.revealCards(this, new CardsImpl(card), game);
        }
        if (card.isLand(game) != land) {
            return false;
        }
        for (Effect effect : this.getEffects()) {
            effect.setTargetPointer(new FixedTarget(player.getId()));
        }
        return true;
    }

    @Override
    public String getRule() {
        if (land) {
            return "Players reveal each card they draw.<br>Whenever a player draws a land card, that player gains 3 life.";
        }
        return "Whenever a player draws a nonland card, that player loses 3 life.";
    }

    @Override
    public SeaOfSandDrawTriggeredAbility copy() {
        return new SeaOfSandDrawTriggeredAbility(this);
    }
}
