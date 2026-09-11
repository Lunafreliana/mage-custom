package mage.game.command;

import mage.constants.CardType;

import java.util.UUID;

/**
 * Runtime contract shared by face-up planar cards and cards in a planar deck.
 * Planar cards remain command-zone objects; face state and deck membership are
 * therefore explicit runtime metadata rather than zone information.
 */
public interface PlanarCard extends CommandObject {

    CardType getPlanarCardType();

    void setSourceObjectAndInitImage();

    void setControllerId(UUID controllerId);

    UUID getPlanarDeckId();

    void setPlanarDeckId(UUID planarDeckId);

    UUID getPlanarDeckOwnerId();

    void setPlanarDeckOwnerId(UUID planarDeckOwnerId);

    boolean isFaceUp();

    void setFaceUp(boolean faceUp);
}
