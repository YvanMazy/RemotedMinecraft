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

import be.yvanmazy.remotedminecraft.MinecraftHolder;
import be.yvanmazy.remotedminecraft.RemotedMinecraft;
import be.yvanmazy.remotedminecraft.config.ProcessConfiguration;
import be.yvanmazy.remotedminecraft.controller.MinecraftController;
import be.yvanmazy.remotedminecraft.controller.agent.AgentFileBuilder;
import be.yvanmazy.remotedminecraft.controller.agent.RemotedAgent;
import be.yvanmazy.remotedminecraft.controller.exception.AgentConnectException;
import be.yvanmazy.remotedminecraft.controller.exception.AgentLoadingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.opentest4j.TestAbortedException;

import java.nio.file.Path;
import java.rmi.registry.Registry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

abstract sealed class MinecraftTestStarterImpl<T extends RemotedAgent> implements MinecraftTestStarter<T> permits MinecraftTestStarterImpl.WithLocalAgent, MinecraftTestStarterImpl.WithAgentPath {

    private static final int DEFAULT_AGENT_PORT = Registry.REGISTRY_PORT;

    private ProcessConfiguration.Builder processConfigurationBuilder;
    private int agentPort = DEFAULT_AGENT_PORT;
    private String agentId;
    private long agentConnectTimeout = 10L;
    private TimeUnit agentConnectTimeoutUnit = TimeUnit.SECONDS;
    private long gameReadyTimeout = 2L;
    private TimeUnit gameReadyTimeoutUnit = TimeUnit.MINUTES;

    @Override
    public @NotNull MinecraftTestStarter<T> config(final ProcessConfiguration.@NotNull Builder builder) {
        this.processConfigurationBuilder = Objects.requireNonNull(builder, "builder must not be null");
        return this;
    }

    @Override
    public @NotNull MinecraftTestStarter<T> agentPort(final @Range(from = 0, to = 65535) int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 0 and 65535");
        }
        this.agentPort = port;
        return this;
    }

    @Override
    public @NotNull MinecraftTestStarter<T> agentId(final @NotNull String agentId) {
        this.agentId = Objects.requireNonNull(agentId, "agentId must not be null");
        return this;
    }

    @Override
    public @NotNull MinecraftTestStarter<T> agentConnectTimeout(final long timeout, final @NotNull TimeUnit unit) {
        this.agentConnectTimeout = timeout;
        this.agentConnectTimeoutUnit = Objects.requireNonNull(unit, "unit must not be null");
        return this;
    }

    @Override
    public @NotNull MinecraftTestStarter<T> gameReadyTimeout(final long timeout, final @NotNull TimeUnit unit) {
        this.gameReadyTimeout = timeout;
        this.gameReadyTimeoutUnit = Objects.requireNonNull(unit, "unit must not be null");
        return this;
    }

    @Override
    public @NotNull T start() {
        Objects.requireNonNull(this.processConfigurationBuilder, "Process configuration must be defined before starting");
        Objects.requireNonNull(this.agentId, "Agent id must be defined before starting");

        // Run a Minecraft client
        final String agentPath = this.getAgentPath().toString();
        final ProcessConfiguration config = this.processConfigurationBuilder.jvmAgentArg(agentPath, this.agentPort).build();
        final MinecraftHolder holder = RemotedMinecraft.run(config).getReadyFuture().join();
        assumeTrue(holder.isStarted(), "Minecraft process failed to start");

        // Connect agent
        final MinecraftController<T> controller = holder.newController();
        try {
            assumeTrue(controller.connect(this.agentId, this.agentPort, this.agentConnectTimeout, this.agentConnectTimeoutUnit),
                    "Failed to connect to agent");
        } catch (final AgentConnectException e) {
            throw new TestAbortedException("Failed to connect to agent", e);
        }

        // Await game is ready (game menu)
        final T agent;
        try {
            agent = controller.awaitReady(this.gameReadyTimeout, this.gameReadyTimeoutUnit);
        } catch (final AgentLoadingException | InterruptedException e) {
            throw new TestAbortedException("Game failed to start", e);
        }

        return agent;
    }

    private static void assumeTrue(final boolean state, final String message) {
        if (!state) {
            throw new TestAbortedException(message);
        }
    }

    protected abstract @NotNull Path getAgentPath();

    static final class WithLocalAgent<T extends RemotedAgent> extends MinecraftTestStarterImpl<T> implements MinecraftTestStarter.WithLocalAgent<T> {

        private Class<?> mainClass;
        private Consumer<AgentFileBuilder> builderConsumer;
        private boolean addRemotedAgentClasses = true;
        private Path agentPath;

        @Override
        public MinecraftTestStarter.@NotNull WithLocalAgent<T> agentMainClass(final @NotNull Class<?> mainClass) {
            this.mainClass = Objects.requireNonNull(mainClass, "mainClass must not be null");
            return this;
        }

        @Override
        public MinecraftTestStarter.@NotNull WithLocalAgent<T> applyAgentBuilder(final @NotNull Consumer<AgentFileBuilder> consumer) {
            this.builderConsumer = Objects.requireNonNull(consumer, "consumer must not be null");
            return this;
        }

        @Override
        public MinecraftTestStarter.@NotNull WithLocalAgent<T> agentPath(final @NotNull Path agentPath) {
            this.agentPath = Objects.requireNonNull(agentPath, "agentPath must not be null");
            return this;
        }

        @Override
        public MinecraftTestStarter.@NotNull WithLocalAgent<T> addRemotedAgentClasses() {
            this.addRemotedAgentClasses = true;
            return this;
        }

        @Override
        public MinecraftTestStarter.@NotNull WithLocalAgent<T> removeRemotedAgentClasses() {
            this.addRemotedAgentClasses = false;
            return this;
        }

        @Override
        protected @NotNull Path getAgentPath() {
            Objects.requireNonNull(this.mainClass, "Main class must be defined before starting");

            final AgentFileBuilder builder = new AgentFileBuilder(this.mainClass);
            this.builderConsumer.accept(builder);
            if (this.addRemotedAgentClasses) {
                builder.addRemotedAgentClasses();
            }
            if (this.agentPath != null) {
                return builder.buildUnchecked(this.agentPath);
            }

            return builder.buildUnchecked();
        }

    }

    static final class WithAgentPath<T extends RemotedAgent> extends MinecraftTestStarterImpl<T> implements MinecraftTestStarter.WithAgentPath<T> {

        private Path agentPath;

        @Override
        public MinecraftTestStarter.@NotNull WithAgentPath<T> agentPath(final @NotNull Path agentPath) {
            this.agentPath = Objects.requireNonNull(agentPath, "agentPath must not be null");
            return this;
        }

        @Override
        protected @NotNull Path getAgentPath() {
            return Objects.requireNonNull(this.agentPath, "Agent path must be defined before starting");
        }

    }

}