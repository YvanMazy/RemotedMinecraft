package be.yvanmazy.remotedminecraft.util;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;

public final class FileUtil {

    private FileUtil() throws IllegalAccessException {
        throw new IllegalAccessException("You cannot instantiate a utility class");
    }

    public static @NotNull String toLauncherString(final @NotNull Path path) {
        return path.toAbsolutePath().toString().replace(File.separatorChar, '/');
    }

}