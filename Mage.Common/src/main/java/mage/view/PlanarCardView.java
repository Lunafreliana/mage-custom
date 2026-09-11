package mage.view;

import mage.game.command.PlanarCard;

import java.io.Serializable;
import java.util.UUID;

/** Public information for a face-up planar card. */
public final class PlanarCardView implements Serializable {
    private final UUID id;
    private final String name;
    private final String type;
    private final String expansionSetCode;

    public PlanarCardView(PlanarCard card) {
        this.id = card.getId();
        this.name = card.getName();
        this.type = card.getPlanarCardType().toString();
        this.expansionSetCode = card.getExpansionSetCode();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getExpansionSetCode() { return expansionSetCode; }
}
