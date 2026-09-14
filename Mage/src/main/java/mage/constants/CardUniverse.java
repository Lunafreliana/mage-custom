package mage.constants;

import mage.MageObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The creative universe represented by a card printing. This is primarily used
 * by the Mystery Booster 2 playtest card Multiversal High Council.
 */
public enum CardUniverse {
    MAGIC(),
    DUNGEONS_AND_DRAGONS("AFR", "AFC", "CLB"),
    WARHAMMER_40000("40K"),
    TRANSFORMERS("BOT"),
    THE_LORD_OF_THE_RINGS("LTR", "LTC"),
    DOCTOR_WHO("WHO"),
    JURASSIC_WORLD("REX"),
    FALLOUT("PIP"),
    ASSASSINS_CREED("ACR"),
    FINAL_FANTASY("FIN", "FIC", "FCA"),
    MARVEL("LMAR", "MAR", "MSH", "MSC", "SPM", "SPE"),
    AVATAR_THE_LAST_AIRBENDER("TLA", "TLE");

    private final Set<String> setCodes;

    CardUniverse(String... setCodes) {
        this.setCodes = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(setCodes)));
    }

    public static CardUniverse from(MageObject object) {
        if (object != null) {
            String setCode = object.getExpansionSetCode();
            for (CardUniverse universe : values()) {
                if (universe.setCodes.contains(setCode)) {
                    return universe;
                }
            }
        }
        return MAGIC;
    }

    public boolean isUniversesBeyond() {
        return this != MAGIC && this != DUNGEONS_AND_DRAGONS;
    }
}
