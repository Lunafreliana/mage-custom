package mage.cards.decks;

import mage.cards.Card;
import mage.cards.CardImpl;
import mage.constants.CardType;
import mage.game.command.PlanarCardRegistry;

/**
 * Non-castable deck-building carrier for a registry-backed Plane or Phenomenon.
 * Runtime planar objects are created by the PLANAR supplemental handler.
 */
public final class PlanarDeckCard extends CardImpl implements SupplementalDeckCard {

    private final String registryId;

    public PlanarDeckCard(String registryId) {
        super(null, requireMetadata(registryId).getEnglishName());
        PlanarCardRegistry.Metadata metadata = requireMetadata(registryId);
        this.registryId = registryId;
        this.cardType.add(metadata.getType());
        this.setExpansionSetCode(metadata.getSetCode());
        this.setCardNumber(registryId);
        this.extraDeckCard = true;
    }

    /**
     * Creates a planar carrier from its serialized set/card identity.
     *
     * The native deck format stores the stable registry id in the card-number
     * field.  Keeping this lookup here avoids teaching the ordinary card
     * repository how to instantiate non-castable runtime objects.
     */
    public static PlanarDeckCard create(String setCode, String registryId) {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(registryId);
        if (metadata == null || !metadata.getSetCode().equals(setCode)) {
            return null;
        }
        return new PlanarDeckCard(registryId);
    }

    private PlanarDeckCard(final PlanarDeckCard card) {
        super(card);
        this.registryId = card.registryId;
    }

    private static PlanarCardRegistry.Metadata requireMetadata(String registryId) {
        PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(registryId);
        if (metadata == null) {
            throw new IllegalArgumentException("Unknown planar-card registry id: " + registryId);
        }
        if (metadata.getType() != CardType.PLANE && metadata.getType() != CardType.PHENOMENON) {
            throw new IllegalArgumentException("Registry entry is not planar: " + registryId);
        }
        return metadata;
    }

    @Override
    public SupplementalDeckType getSupplementalDeckType() {
        return SupplementalDeckType.PLANAR;
    }

    @Override
    public String getSupplementalDeckId() {
        return registryId;
    }

    @Override
    public Card copy() {
        return new PlanarDeckCard(this);
    }
}
