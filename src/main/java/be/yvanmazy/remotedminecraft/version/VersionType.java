package be.yvanmazy.remotedminecraft.version;

public enum VersionType {

    OLD_ALPHA,
    OLD_BETA,
    SNAPSHOT,
    RELEASE;

    private static final VersionType[] CACHED_VALUES = values();

    public static VersionType fromString(final String string) {
        for (final VersionType type : CACHED_VALUES) {
            if (type.name().equalsIgnoreCase(string)) {
                return type;
            }
        }
        return null;
    }

    public boolean isOld() {
        return this == OLD_ALPHA || this == OLD_BETA;
    }

}