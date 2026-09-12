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
