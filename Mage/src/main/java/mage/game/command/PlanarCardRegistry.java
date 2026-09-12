package mage.game.command;

import mage.constants.CardType;
import mage.constants.Phenomena;
import mage.constants.Planes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Public catalog and construction boundary for implemented planar cards.
 * Protocols use stable ids rather than Java enum or class names.
 */
public final class PlanarCardRegistry {

    private static final Map<String, Entry> ENTRIES = buildEntries();

    private PlanarCardRegistry() {
    }

    private static Map<String, Entry> buildEntries() {
        Map<String, Entry> entries = new LinkedHashMap<>();
        for (Planes plane : Planes.values()) {
            register(entries, id(CardType.PLANE, plane.name()), CardType.PLANE,
                    displayName(plane.getFullName()), plane.getFullName(), setCode(plane), () -> Plane.createPlane(plane));
                    displayName(plane.getFullName()), plane.getFullName(), plane.getSetCode(), () -> Plane.createPlane(plane));
        }
        for (Phenomena phenomenon : Phenomena.values()) {
            register(entries, id(CardType.PHENOMENON, phenomenon.name()), CardType.PHENOMENON,
                    displayName(phenomenon.getFullName()), phenomenon.getFullName(), "PCA",
                    () -> Phenomenon.createPhenomenon(phenomenon));
        }
        return Collections.unmodifiableMap(entries);
    }

    private static void register(Map<String, Entry> entries, String id, CardType type,
                                 String englishName, String imageName, String setCode,
                                 Supplier<PlanarCard> factory) {
        Entry previous = entries.put(id, new Entry(id, type, englishName, imageName, setCode, factory));
        if (previous != null) {
            throw new IllegalStateException("Duplicate planar-card id: " + id);
        }
    }

    private static String id(CardType type, String name) {
        return type.toString().toLowerCase(Locale.ENGLISH) + ":" + name.toLowerCase(Locale.ENGLISH);
    }

    private static String displayName(String fullName) {
        int separator = fullName.indexOf(" - ");
        return separator < 0 ? fullName : fullName.substring(separator + 3);
    }

    private static String setCode(Planes plane) {
        return plane == Planes.PLANE_THE_COMMAND_ZONE ? "PUNK" : "PCA";
    }

    public static List<Metadata> getAvailableCards() {
        List<Metadata> result = new ArrayList<>();
        ENTRIES.values().forEach(entry -> result.add(entry.metadata));
        return Collections.unmodifiableList(result);
    }

    public static PlanarCard create(String id) {
        Entry entry = ENTRIES.get(id);
        return entry == null ? null : entry.factory.get();
    }

    public static Metadata getMetadata(String id) {
        Entry entry = ENTRIES.get(id);
        return entry == null ? null : entry.metadata;
    }

    public static String getId(Planes plane) {
        return id(CardType.PLANE, plane.name());
    }

    public static String getId(Phenomena phenomenon) {
        return id(CardType.PHENOMENON, phenomenon.name());
    }

    private static final class Entry {
        private final Metadata metadata;
        private final Supplier<PlanarCard> factory;

        private Entry(String id, CardType type, String englishName, String imageName, String setCode,
                      Supplier<PlanarCard> factory) {
            this.metadata = new Metadata(id, type, englishName, imageName, setCode);
            this.factory = factory;
        }
    }

    public static final class Metadata implements Serializable {
        private final String id;
        private final CardType type;
        private final String englishName;
        private final String imageName;
        private final String setCode;

        private Metadata(String id, CardType type, String englishName, String imageName, String setCode) {
            this.id = id;
            this.type = type;
            this.englishName = englishName;
            this.imageName = imageName;
            this.setCode = setCode;
        }

        public String getId() { return id; }
        public CardType getType() { return type; }
        public String getEnglishName() { return englishName; }
        public String getImageName() { return imageName; }
        public String getSetCode() { return setCode; }
    }
}
