package mage.constants;

/** Implemented Planechase phenomenon cards. */
public enum Phenomena {
    CHAOTIC_AETHER("ChaoticAetherPhenomenon", "Phenomenon - Chaotic Aether"),
    INTERPLANAR_TUNNEL("InterplanarTunnelPhenomenon", "Phenomenon - Interplanar Tunnel"),
    MUTUAL_EPIPHANY("MutualEpiphanyPhenomenon", "Phenomenon - Mutual Epiphany"),
    REALITY_SHAPING("RealityShapingPhenomenon", "Phenomenon - Reality Shaping"),
    SPATIAL_MERGING("SpatialMergingPhenomenon", "Phenomenon - Spatial Merging");

    private final String className;
    private final String fullName;

    Phenomena(String className, String fullName) {
        this.className = className;
        this.fullName = fullName;
    }

    public String getClassName() {
        return className;
    }

    public String getFullName() {
        return fullName;
    }
}
