package be.yvanmazy.remotedminecraft.config;

import be.yvanmazy.remotedminecraft.auth.Auth;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

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
    @NotNull List<String> jvmArguments();

    @Contract(pure = true)
    @NotNull List<String> gameArguments();

    @Contract(pure = true)
    @NotNull String processMainClass();

    @Contract(pure = true)
    @NotNull Path processDirectory();

    @Contract(pure = true)
    boolean independent();

    interface Builder {

        @Contract("_ -> this")
        @NotNull Builder version(final @NotNull String version);

        @Contract("_ -> this")
        @NotNull Builder authentication(final @Nullable Auth authentication);

        @Contract("_ -> this")
        @NotNull Builder processJavaPath(final @Nullable Path processJavaPath);

        @Contract("_ -> this")
        @NotNull Builder jvmArguments(final @NotNull List<String> jvmArguments);

        @Contract("_ -> this")
        @NotNull Builder gameArguments(final @NotNull List<String> gameArguments);

        @Contract("_ -> this")
        @NotNull Builder processMainClass(final @Nullable String processMainClass);

        @Contract("_ -> this")
        @NotNull Builder processDirectory(final @Nullable Path processDirectory);

        @Contract("-> this")
        default @NotNull Builder independent() {
            return this.independent(true);
        }

        @Contract("_ -> this")
        @NotNull Builder independent(final boolean independent);

        @Contract("-> new")
        @NotNull ProcessConfiguration build();

    }

}