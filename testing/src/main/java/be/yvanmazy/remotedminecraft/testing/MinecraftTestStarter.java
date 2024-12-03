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

package be.yvanmazy.remotedminecraft.testing;

import be.yvanmazy.remotedminecraft.config.ProcessConfiguration;
import be.yvanmazy.remotedminecraft.controller.agent.AgentFileBuilder;
import be.yvanmazy.remotedminecraft.controller.agent.RemotedAgent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public interface MinecraftTestStarter<T extends RemotedAgent> {

    @Contract("-> new")
    static <T extends RemotedAgent> MinecraftTestStarter.@NotNull WithLocalAgent<T> newStarterLocal() {
        return new MinecraftTestStarterImpl.WithLocalAgent<>();
    }

    @Contract("-> new")
    static <T extends RemotedAgent> MinecraftTestStarter.@NotNull WithAgentPath<T> newStarter() {
        return new MinecraftTestStarterImpl.WithAgentPath<>();
    }

    @Contract("_ -> this")
    default @NotNull MinecraftTestStarter<T> config(final @NotNull Consumer<ProcessConfiguration.Builder> consumer) {
        final ProcessConfiguration.Builder builder = ProcessConfiguration.newBuilder();
        consumer.accept(builder);
        return this.config(builder);
    }

    @Contract("_ -> this")
    @NotNull MinecraftTestStarter<T> config(final @NotNull ProcessConfiguration.Builder builder);

    @Contract("_ -> this")
    @NotNull MinecraftTestStarter<T> agentId(final @NotNull String agentId);

    @Contract("_ -> this")
    @NotNull MinecraftTestStarter<T> agentPort(final @Range(from = 0, to = 65535) int port);

    @Contract("_, _ -> this")
    @NotNull MinecraftTestStarter<T> agentConnectTimeout(final long timeout, final @NotNull TimeUnit unit);

    @Contract("_, _ -> this")
    @NotNull MinecraftTestStarter<T> gameReadyTimeout(final long timeout, final @NotNull TimeUnit unit);

    @Contract("-> new")
    @NotNull StartedMinecraft<T> start();

    interface WithLocalAgent<T extends RemotedAgent> extends MinecraftTestStarter<T> {

        @Contract("_ -> this")
        @NotNull WithLocalAgent<T> agentMainClass(final @NotNull Class<?> mainClass);

        @Contract("_ -> this")
        @NotNull WithLocalAgent<T> applyAgentBuilder(final @NotNull Consumer<AgentFileBuilder> consumer);

        @Contract("_ -> this")
        @NotNull WithLocalAgent<T> agentPath(final @NotNull Path agentPath);

        @Contract("-> this")
        @NotNull WithLocalAgent<T> addRemotedAgentClasses();

        @Contract("-> this")
        @NotNull WithLocalAgent<T> removeRemotedAgentClasses();

    }

    interface WithAgentPath<T extends RemotedAgent> extends MinecraftTestStarter<T> {

        @Contract("_ -> this")
        @NotNull WithAgentPath<T> agentPath(final @NotNull Path agentPath);

    }

}