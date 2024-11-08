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

package be.yvanmazy.remotedminecraft;

import be.yvanmazy.remotedminecraft.config.ProcessConfiguration;
import be.yvanmazy.remotedminecraft.controller.MinecraftController;
import be.yvanmazy.remotedminecraft.controller.agent.RemotedAgent;
import be.yvanmazy.remotedminecraft.state.MinecraftState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

final class MinecraftHolderImpl implements MinecraftHolder {

    private final ProcessConfiguration configuration;
    private final CompletableFuture<MinecraftHolder> readyFuture = new CompletableFuture<>();

    private MinecraftState state = MinecraftState.STARTING;
    private Process process;

    MinecraftHolderImpl(final @NotNull ProcessConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "configuration must not be null");
    }

    @Override
    public @NotNull <T extends RemotedAgent> MinecraftController<T> newController() {
        return MinecraftController.build(this.process);
    }

    @Override
    public @NotNull CompletableFuture<MinecraftHolder> getReadyFuture() {
        return this.readyFuture;
    }

    @Override
    @NotNull
    public ProcessConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    @Nullable
    public Process getProcess() {
        return this.process;
    }

    @Override
    public @NotNull MinecraftState getState() {
        return this.state;
    }

    void complete(final @NotNull Process process) {
        this.process = Objects.requireNonNull(process, "process must not be null");
        if (!this.configuration.independent()) {
            ProcessManager.getInstance().register(process);
        }
        this.state = MinecraftState.STARTED;
        process.onExit().whenComplete((p, throwable) -> this.state = MinecraftState.CLOSED);
        this.readyFuture.complete(this);
    }

    void completeExceptionally(final @NotNull Throwable throwable) {
        this.state = MinecraftState.CLOSED;
        this.readyFuture.completeExceptionally(throwable);
    }

}