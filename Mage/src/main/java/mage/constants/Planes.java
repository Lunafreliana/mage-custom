package mage.constants;

/**
 * @author spjspj
 */
public enum Planes {
    PLANE_ACADEMY_AT_TOLARIA_WEST("AcademyAtTolariaWestPlane", "Plane - Academy at Tolaria West"),
    PLANE_AGYREM("AgyremPlane", "Plane - Agyrem"),
    PLANE_AKOUM("AkoumPlane", "Plane - Akoum"),
    PLANE_ANTARCTIC_RESEARCH_BASE("AntarcticResearchBasePlane", "Plane - Antarctic Research Base", "WHO"),
    PLANE_ASTRAL_ARENA("AstralArenaPlane", "Plane - Astral Arena"),
    PLANE_BAD_WOLF_BAY("BadWolfBayPlane", "Plane - Bad Wolf Bay", "WHO"),
    PLANE_BANT("BantPlane", "Plane - Bant"),
    PLANE_BESIEGED_VIKING_VILLAGE("BesiegedVikingVillagePlane", "Plane - Besieged Viking Village", "WHO"),
    PLANE_CITY_OF_THE_DALEKS("CityOfTheDaleksPlane", "Plane - City of the Daleks", "WHO"),
    PLANE_ELOREN_WILDS("ElorenWildsPlane", "Plane - Eloren Wilds"),
    PLANE_EDGE_OF_MALACOL("EdgeOfMalacolPlane", "Plane - Edge of Malacol"),
    PLANE_FEEDING_GROUNDS("FeedingGroundsPlane", "Plane - Feeding Grounds"),
    PLANE_FIELDS_OF_SUMMER("FieldsOfSummerPlane", "Plane - Fields of Summer"),
    PLANE_GAVONY("GavonyPlane", "Plane - Gavony"),
    PLANE_HEDRON_FIELDS_OF_AGADEEM("HedronFieldsOfAgadeemPlane", "Plane - Hedron Fields of Agadeem"),
    PLANE_HORIZON_BOUGHS("HorizonBoughsPlane", "Plane - Horizon Boughs"),
    PLANE_KHARASHA_FOOTHILLS("KharashaFoothillsPlane", "Plane - Kharasha Foothills", "MOC"),
    PLANE_INYS_HAEN("InysHaenPlane", "Plane - Inys Haen", "MOC"),
    PLANE_LAKE_SILENCIO("LakeSilencioPlane", "Plane - Lake Silencio", "WHO"),
    PLANE_LETHE_LAKE("LetheLakePlane", "Plane - Lethe Lake"),
    PLANE_NAYA("NayaPlane", "Plane - Naya"),
    PLANE_NEPHALIA("NephaliaPlane", "Plane - Nephalia"),
    PLANE_OOD_SPHERE("OodSpherePlane", "Plane - Ood Sphere", "WHO"),
    PLANE_PANOPTICON("PanopticonPlane", "Plane - Panopticon"),
    PLANE_PURSUED_BY_SOMETHING("PursuedBySomethingPlane", "Plane - Pursued by Something", "PUNK"),
    PLANE_PRAHV("PrahvPlane", "Plane - Prahv"),
    PLANE_SELESNYA_LOFT_GARDENS("SelesnyaLoftGardensPlane", "Plane - Selesnya Loft Gardens"),
    PLANE_STAIRS_TO_INFINITY("StairsToInfinityPlane", "Plane - Stairs to Infinity"),
    PLANE_TAZEEM("TazeemPlane", "Plane - Tazeem"),
    PLANE_TEN_WIZARDS_MOUNTAIN("TenWizardsMountainPlane", "Plane - Ten Wizards Mountain", "MOC"),
    PLANE_THE_GOLDEN_CITY_OF_ORAZCA("TheGoldenCityOfOrazcaPlane", "Plane - The Golden City of Orazca", "MOC"),
    PLANE_THE_GREAT_AERIE("TheGreatAeriePlane", "Plane - The Great Aerie", "MOC"),
    PLANE_THE_COMMAND_ZONE("TheCommandZonePlane", "Plane - The Command Zone", "PUNK"),
    PLANE_THE_LUX_FOUNDATION_LIBRARY("TheLuxFoundationLibraryPlane", "Plane - The Lux Foundation Library", "WHO"),
    PLANE_THE_PYRAMID_OF_MARS("ThePyramidOfMarsPlane", "Plane - The Pyramid of Mars", "WHO"),
    PLANE_TOWASHI("TowashiPlane", "Plane - Towashi", "MOC"),
    PLANE_THE_DARK_BARONY("TheDarkBaronyPlane", "Plane - The Dark Barony"),
    PLANE_THE_EON_FOG("TheEonFogPlane", "Plane - The Eon Fog"),
    PLANE_THE_GREAT_FOREST("TheGreatForestPlane", "Plane - The Great Forest"),
    PLANE_THE_MAELSTROM("TheMaelstromPlane", "Plane - The Maelstrom"),
    PLANE_THE_ZEPHYR_MAZE_FOG("TheZephyrMazePlane", "Plane - The Zephyr Maze"),
    PLANE_TRUGA_JUNGLE("TrugaJunglePlane", "Plane - Truga Jungle"),
    PLANE_TRAIL_OF_THE_MAGE_RINGS("TrailOfTheMageRingsPlane", "Plane - Trail of the Mage-Rings"),
    PLANE_TURRI_ISLAND("TurriIslandPlane", "Plane - Turri Island"),
    PLANE_UNDERCITY_REACHES("UndercityReachesPlane", "Plane - Undercity Reaches"),
    PLANE_VELIS_VEL("VelisVelPlane", "Plane - Velis Vel"),
    PLANE_WE_HOPE_YOU_LIKE_SQUIRRELS("WeHopeYouLikeSquirrelsPlane", "Plane - We Hope You Like Squirrels");

    private final String className;
    private final String fullName;
    private final String setCode;

    Planes(String className, String fullName) {
        this(className, fullName, "PCA");
    }

    Planes(String className, String fullName, String setCode) {
        this.className = className;
        this.fullName = fullName;
        this.setCode = setCode;
    }

    @Override
    public String toString() {
        return className;
    }

    public String getClassName() {
        return className;
    }

    public String getFullName() {
        return fullName;
    }

    public String getSetCode() {
        return setCode;
    }

    public static Planes fromFullName(String fullName) {
        for (Planes p : Planes.values()) {
            if (p.fullName.equals(fullName)) {
                return p;
            }
        }

        return null;
    }

    public static Planes fromClassName(String className) {
        for (Planes p : Planes.values()) {
            if (p.className.equals(className)) {
                return p;
            }
        }

        return null;
    }
}
