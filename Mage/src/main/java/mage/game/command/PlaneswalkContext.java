package mage.game.command;

import java.io.Serializable;
import java.util.UUID;

/** Explicit rules identities and cause associated with one planeswalk. */
public final class PlaneswalkContext implements Serializable {

    public enum Cause {
        PLANAR_DIE, SPELL_OR_ABILITY, PHENOMENON_STATE_BASED_ACTION, DEPARTURE, OTHER
    }

    private final UUID planeswalkingPlayerId;
    private final Cause cause;
    private final UUID sourceId;

    public PlaneswalkContext(UUID planeswalkingPlayerId, Cause cause, UUID sourceId) {
        this.planeswalkingPlayerId = planeswalkingPlayerId;
        this.cause = cause == null ? Cause.OTHER : cause;
        this.sourceId = sourceId;
    }

    public static PlaneswalkContext forPlayer(UUID playerId) {
        return new PlaneswalkContext(playerId, Cause.OTHER, null);
    }

    public UUID getPlaneswalkingPlayerId() { return planeswalkingPlayerId; }
    public Cause getCause() { return cause; }
    public UUID getSourceId() { return sourceId; }
}
