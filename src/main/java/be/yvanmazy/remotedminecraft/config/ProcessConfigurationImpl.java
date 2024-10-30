package be.yvanmazy.remotedminecraft.config;

import be.yvanmazy.remotedminecraft.auth.Auth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

record ProcessConfigurationImpl(String version, Auth authentication, Path processJavaPath, String processJvmOptions,
                                String processMainClass, Path processDirectory) implements ProcessConfiguration {

    private ProcessConfigurationImpl(final Builder builder) {
        this(builder.version,
                builder.authentication,
                builder.processJavaPath,
                builder.processJvmOptions,
                builder.processMainClass,
                builder.processDirectory);
    }

    ProcessConfigurationImpl {
        Objects.requireNonNull(version, "version must not be null");
        if (authentication == null) {
            authentication = Auth.EMPTY;
        }
        if (processJavaPath == null) {
            processJavaPath = Path.of(System.getProperty("java.home"));
        }
        if (processJvmOptions == null) {
            processJvmOptions = "";
        }
        if (processMainClass == null) {
            processMainClass = "";
        }
        if (processDirectory == null) {
            processDirectory = Path.of("");
        }
    }

    public static class Builder implements ProcessConfiguration.Builder {

        private String version;
        private Auth authentication;
        private Path processJavaPath;
        private String processJvmOptions;
        private String processMainClass;
        private Path processDirectory;

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
        public ProcessConfiguration.@NotNull Builder processJvmOptions(final @Nullable String jvmOptions) {
            this.processJvmOptions = jvmOptions;
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
        public @NotNull ProcessConfiguration build() {
            return new ProcessConfigurationImpl(this);
        }

    }

}