package be.yvanmazy.remotedminecraft.config;

import be.yvanmazy.remotedminecraft.auth.Auth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

record ProcessConfigurationImpl(String version, Auth authentication, Path processJavaPath, List<String> jvmArguments,
                                List<String> gameArguments, String processMainClass, Path processDirectory,
                                boolean independent) implements ProcessConfiguration {

    private ProcessConfigurationImpl(final Builder builder) {
        this(builder.version,
                builder.authentication,
                builder.processJavaPath,
                builder.jvmArguments,
                builder.gameArguments,
                builder.processMainClass,
                builder.processDirectory,
                builder.independent);
    }

    ProcessConfigurationImpl {
        Objects.requireNonNull(version, "version must not be null");
        if (authentication == null) {
            authentication = Auth.EMPTY;
        }
        if (processJavaPath == null) {
            final String command = ProcessHandle.current().info().command().orElse(null);
            if (command == null || command.isBlank()) {
                throw new IllegalArgumentException("Java path is not found!");
            }
            processJavaPath = Path.of(command);
            if (!Files.isRegularFile(processJavaPath)) {
                throw new IllegalArgumentException("Java path is not found: '" + processJavaPath + "'");
            }
        }
        jvmArguments = jvmArguments != null ? List.copyOf(jvmArguments) : List.of();
        gameArguments = gameArguments != null ? List.copyOf(gameArguments) : List.of();
        if (processMainClass == null) {
            processMainClass = "";
        }
        if (processDirectory == null) {
            processDirectory = Path.of("");
        }
    }

    static class Builder implements ProcessConfiguration.Builder {

        private String version;
        private Auth authentication;
        private Path processJavaPath;
        private final List<String> jvmArguments = new ArrayList<>();
        private List<String> gameArguments;
        private String processMainClass;
        private Path processDirectory;
        private boolean independent;

        @Override
        public ProcessConfiguration.@NotNull Builder version(final @NotNull String version) {
            this.version = Objects.requireNonNull(version, "version must not be null");
            return this;
        }

        @Override
        public ProcessConfiguration.@NotNull Builder authentication(final @Nullable Auth authentication) {
            this.authentication = authentication;
            return this;
        }

        @Override
        public ProcessConfiguration.@NotNull Builder processJavaPath(final @Nullable Path processJavaPath) {
            this.processJavaPath = processJavaPath;
            return this;
        }

        @Override
        public ProcessConfiguration.@NotNull Builder jvmArguments(final @NotNull List<String> jvmArguments) {
            this.jvmArguments.clear();
            this.jvmArguments.addAll(Objects.requireNonNull(jvmArguments, "jvmArguments must not be null"));
            return this;
        }

        @Override
        public ProcessConfiguration.@NotNull Builder gameArguments(final @NotNull List<String> gameArguments) {
            this.gameArguments = Objects.requireNonNull(gameArguments, "gameArguments must not be null");
            return this;
        }

        @Override
        public ProcessConfiguration.@NotNull Builder jvmAgentArg(final @NotNull String path, final @Nullable String arg) {
            this.jvmArguments.add("-javaagent:" + path + (arg != null ? "=" + arg : ""));
            return this;
        }

        @Override
        public ProcessConfiguration.@NotNull Builder processMainClass(final @Nullable String processMainClass) {
            this.processMainClass = processMainClass;
            return this;
        }

        @Override
        public ProcessConfiguration.@NotNull Builder processDirectory(final @Nullable Path processDirectory) {
            this.processDirectory = processDirectory;
            return this;
        }

        @Override
        public ProcessConfiguration.@NotNull Builder independent(final boolean independent) {
            this.independent = independent;
            return this;
        }

        @Override
        public @NotNull ProcessConfiguration build() {
            return new ProcessConfigurationImpl(this);
        }

    }

}