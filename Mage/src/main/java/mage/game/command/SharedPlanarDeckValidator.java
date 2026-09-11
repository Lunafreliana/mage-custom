package mage.game.command;

import mage.constants.CardType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Validates the communal planar-deck restrictions from rule 901.15a. */
public final class SharedPlanarDeckValidator {

    private SharedPlanarDeckValidator() {
    }

    public static List<String> validate(List<String> cardIds, int playerCount) {
        List<String> errors = new ArrayList<>();
        int minimum = Math.min(40, 10 * playerCount);
        if (cardIds.size() < minimum) {
            errors.add("A shared planar deck for " + playerCount + " players needs at least " + minimum + " cards.");
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
        if (phenomena > 2 * playerCount) {
            errors.add("A shared planar deck for " + playerCount + " players can contain at most "
                    + (2 * playerCount) + " phenomena.");
        }
        if (!hasPlane) {
            errors.add("A shared planar deck must contain a plane.");
        }
        return errors;
    }
}
