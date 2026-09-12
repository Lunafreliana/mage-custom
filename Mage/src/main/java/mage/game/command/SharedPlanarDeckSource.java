package mage.game.command;

import java.io.Serializable;

/** Configuration source for the single deck used by {@link PlanarDeckMode#SHARED}. */
public enum SharedPlanarDeckSource implements Serializable {
    TABLE_CONFIGURED,
    MERGED_PLAYER_CONTRIBUTIONS
}
