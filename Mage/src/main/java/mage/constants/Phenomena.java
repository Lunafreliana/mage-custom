package mage.constants;

/** Implemented Planechase phenomenon cards. */
public enum Phenomena {
    INTERPLANAR_TUNNEL("InterplanarTunnelPhenomenon", "Phenomenon - Interplanar Tunnel"),
    MUTUAL_EPIPHANY("MutualEpiphanyPhenomenon", "Phenomenon - Mutual Epiphany"),
    OMENPATH_INSTABILITY("OmenpathInstabilityPhenomenon", "Phenomenon - Omenpath Instability", "PUNK"),
    REALITY_SHAPING("RealityShapingPhenomenon", "Phenomenon - Reality Shaping"),
    SPATIAL_MERGING("SpatialMergingPhenomenon", "Phenomenon - Spatial Merging");

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
