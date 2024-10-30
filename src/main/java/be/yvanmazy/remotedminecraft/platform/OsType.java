package be.yvanmazy.remotedminecraft.platform;

import org.jetbrains.annotations.NotNull;

public enum OsType {

    WINDOWS,
    LINUX,
    OSX,
    UNKNOWN;

    public static @NotNull OsType getCurrentType() {
        return fromString(System.getProperty("os.name").toLowerCase());
    }

    public static @NotNull OsType fromString(final String osId) {
        if (osId == null) {
            return UNKNOWN;
        } else if (osId.contains("win")) {
            return WINDOWS;
        } else if (osId.contains("unix") || osId.contains("linux")) {
            return LINUX;
        } else if (osId.contains("osx") || osId.contains("mac")) {
            return OSX;
        }
        return UNKNOWN;
    }

}