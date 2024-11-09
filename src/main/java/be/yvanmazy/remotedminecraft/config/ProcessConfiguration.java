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
import be.yvanmazy.remotedminecraft.util.FileUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.function.UnaryOperator;

public interface ProcessConfiguration {

    @Contract("-> new")
    static @NotNull ProcessConfiguration.Builder newBuilder() {
        return new ProcessConfigurationImpl.Builder();
    }

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
    @NotNull List<Path> classpath();

    @Contract(pure = true)
    @NotNull String processMainClass();

    @Contract(pure = true)
    @NotNull Path processDirectory();

    @Contract(pure = true)
    boolean independent();

    @Contract(pure = true)
    boolean inheritIO();

    @Contract(pure = true)
    UnaryOperator<ProcessBuilder> processOperator();

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
        default @NotNull Builder jvmAgentArg(final int port) {
            return this.jvmAgentArg(FileUtil.getSelf().toString(), String.valueOf(port));
        }

        @Contract("_ -> this")
        default @NotNull Builder jvmAgentArg(final @Nullable String arg) {
            return this.jvmAgentArg(FileUtil.getSelf().toString(), arg);
        }

        @Contract("_, _ -> this")
        @NotNull Builder jvmAgentArg(final @NotNull String path, final @Nullable String arg);

        @Contract("_ -> this")
        @NotNull Builder gameArguments(final @NotNull List<String> gameArguments);

        @Contract("_ -> this")
        @NotNull Builder classpath(final @NotNull List<Path> classpath);

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

        @Contract("_ -> this")
        @NotNull Builder inheritIO(final boolean inheritIO);

        @Contract("_ -> this")
        @NotNull Builder processOperator(final @Nullable UnaryOperator<ProcessBuilder> operator);

        @Contract("-> new")
        @NotNull ProcessConfiguration build();

    }

}