package mage.game.command;

import mage.constants.CardType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Validates one player's supplementary planar deck under rule 901.3. */
public final class IndividualPlanarDeckValidator {

    private IndividualPlanarDeckValidator() {
    }

    public static List<String> validate(List<String> cardIds) {
        List<String> errors = new ArrayList<>();
        if (cardIds.size() < 10) {
            errors.add("An individual planar deck needs at least 10 cards.");
        }
        int phenomena = 0;
        boolean hasPlane = false;
        Set<String> names = new HashSet<>();
        for (String id : cardIds) {
            PlanarCardRegistry.Metadata metadata = PlanarCardRegistry.getMetadata(id);
            if (metadata == null) {
                errors.add("Unknown planar card id: " + id);
                continue;
            }
            if (!names.add(metadata.getEnglishName().toLowerCase(Locale.ENGLISH))) {
                errors.add("Planar card names must be unique: " + metadata.getEnglishName());
            }
            if (metadata.getType() == CardType.PHENOMENON) {
                phenomena++;
            } else if (metadata.getType() == CardType.PLANE) {
                hasPlane = true;
            }
        }
        if (phenomena > 2) {
            errors.add("An individual planar deck can contain at most 2 phenomena.");
        }
        if (!hasPlane) {
            errors.add("An individual planar deck must contain a plane.");
        }
        return errors;
    }
}
