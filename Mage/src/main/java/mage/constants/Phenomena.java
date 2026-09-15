package mage.constants;

/** Implemented Planechase phenomenon cards. */
public enum Phenomena {
    INTERPLANAR_TUNNEL("InterplanarTunnelPhenomenon", "Phenomenon - Interplanar Tunnel", "PCA"),
    MUTUAL_EPIPHANY("MutualEpiphanyPhenomenon", "Phenomenon - Mutual Epiphany", "PCA"),
    REALITY_SHAPING("RealityShapingPhenomenon", "Phenomenon - Reality Shaping", "PCA"),
    SPATIAL_MERGING("SpatialMergingPhenomenon", "Phenomenon - Spatial Merging", "PCA"),
    TEAM_UP("TeamUpPhenomenon", "Phenomenon - Team-Up!", "PUNK");

    private final String className;
    private final String fullName;
    private final String setCode;

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
