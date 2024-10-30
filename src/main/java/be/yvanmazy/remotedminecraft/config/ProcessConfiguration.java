package be.yvanmazy.remotedminecraft.config;

import be.yvanmazy.remotedminecraft.auth.Auth;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public interface ProcessConfiguration {

    @Contract("-> new")
    static @NotNull ProcessConfiguration.Builder newBuilder() {
        return new ProcessConfigurationImpl.Builder();
    }

    // TODO: Add classpath option
    // TODO: Add inheritIO option
    // TODO: Add unary operator for processBuilder option

    @Contract(pure = true)
    @NotNull String version();

    @Contract(pure = true)
    @NotNull Auth authentication();

    @Contract(pure = true)
    @NotNull Path processJavaPath();

    @Contract(pure = true)
    @NotNull String processJvmOptions();

    @Contract(pure = true)
    @NotNull String processMainClass();

    @Contract(pure = true)
    @NotNull Path processDirectory();

    interface Builder {

        @Contract("_ -> this")
        @NotNull Builder version(final @NotNull String version);

        @Contract("_ -> this")
        @NotNull Builder authentication(final @Nullable Auth authentication);

        @Contract("_ -> this")
        @NotNull Builder processJavaPath(final @Nullable Path processJavaPath);

        @Contract("_ -> this")
        @NotNull Builder processJvmOptions(final @Nullable String jvmOptions);

        @Contract("_ -> this")
        @NotNull Builder processMainClass(final @Nullable String processMainClass);

        @Contract("_ -> this")
        @NotNull Builder processDirectory(final @Nullable Path processDirectory);

        @Contract("-> new")
        @NotNull ProcessConfiguration build();

    }

}