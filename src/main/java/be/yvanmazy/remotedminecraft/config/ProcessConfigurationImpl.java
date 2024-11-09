/*
 * MIT License
 *
 * Copyright (c) 2024 Darkkraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package be.yvanmazy.remotedminecraft.config;

import be.yvanmazy.remotedminecraft.auth.Auth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

record ProcessConfigurationImpl(String version, Auth authentication, Path processJavaPath, List<String> jvmArguments,
                                List<String> gameArguments, List<Path> classpath, String processMainClass, Path processDirectory,
                                boolean independent, boolean inheritIO,
                                UnaryOperator<ProcessBuilder> processOperator) implements ProcessConfiguration {

    private ProcessConfigurationImpl(final Builder builder) {
        this(builder.version,
                builder.authentication,
                builder.processJavaPath,
                builder.jvmArguments,
                builder.gameArguments,
                builder.classpath,
                builder.processMainClass,
                builder.processDirectory,
                builder.independent,
                builder.inheritIO,
                builder.processOperator);
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
        classpath = classpath != null ? List.copyOf(classpath) : List.of();
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
        private List<Path> classpath;
        private String processMainClass;
        private Path processDirectory;
        private boolean independent;
        private boolean inheritIO = true;
        private UnaryOperator<ProcessBuilder> processOperator;

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
        public ProcessConfiguration.@NotNull Builder classpath(final @NotNull List<Path> classpath) {
            this.classpath = Objects.requireNonNull(classpath, "classpath must not be null");
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
        public ProcessConfiguration.@NotNull Builder inheritIO(final boolean inheritIO) {
            this.inheritIO = inheritIO;
            return this;
        }

        @Override
        public ProcessConfiguration.@NotNull Builder processOperator(final @Nullable UnaryOperator<ProcessBuilder> operator) {
            this.processOperator = operator;
            return this;
        }

        @Override
        public @NotNull ProcessConfiguration build() {
            return new ProcessConfigurationImpl(this);
        }

    }

}