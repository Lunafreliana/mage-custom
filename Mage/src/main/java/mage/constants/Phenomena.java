package mage.constants;

/** Implemented Planechase phenomenon cards. */
public enum Phenomena {
    CHAOTIC_AETHER("ChaoticAetherPhenomenon", "Phenomenon - Chaotic Aether"),
    FIXED_POINT_IN_TIME("FixedPointInTimePhenomenon", "Phenomenon - Fixed Point in Time", "WHO"),
    INTERPLANAR_TUNNEL("InterplanarTunnelPhenomenon", "Phenomenon - Interplanar Tunnel"),
    MUTUAL_EPIPHANY("MutualEpiphanyPhenomenon", "Phenomenon - Mutual Epiphany"),
    OMENPATH_INSTABILITY("OmenpathInstabilityPhenomenon", "Phenomenon - Omenpath Instability", "PUNK"),
    REALITY_SHAPING("RealityShapingPhenomenon", "Phenomenon - Reality Shaping"),
    SPATIAL_MERGING("SpatialMergingPhenomenon", "Phenomenon - Spatial Merging"),
    TEAM_UP("TeamUpPhenomenon", "Phenomenon - Team-Up!", "PUNK"),
    TIME_DISTORTION("TimeDistortionPhenomenon", "Phenomenon - Time Distortion");

    private final String className;
    private final String fullName;
    private final String setCode;

    Phenomena(String className, String fullName) {
        this(className, fullName, "PCA");
    }

    Phenomena(String className, String fullName, String setCode) {
        this.className = className;
        this.fullName = fullName;
        this.setCode = setCode;
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
}
