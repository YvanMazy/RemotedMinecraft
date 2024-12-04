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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public interface MinecraftHolder {

    @Contract(pure = true)
    @NotNull MinecraftState getState();

    @Contract(pure = true)
    @NotNull ProcessConfiguration getConfiguration();

    @Contract(pure = true)
    @Nullable Process getProcess();

    @Contract(pure = true)
    @NotNull CompletableFuture<MinecraftHolder> getReadyFuture();

    @Contract(pure = true)
    @NotNull <T extends RemotedAgent> MinecraftController<T> newController();

    @Contract(pure = true)
    default boolean isStarted() {
        return this.getState() == MinecraftState.STARTED;
    }

    default boolean destroy() {
        return this.destroy(3L, TimeUnit.SECONDS, 1L, TimeUnit.SECONDS);
    }

    default boolean destroy(final long timeout, final @NotNull TimeUnit timeoutUnit) {
        return this.destroy(timeout, timeoutUnit, 1L, TimeUnit.SECONDS);
    }

    default boolean destroy(final long timeout,
                            final @NotNull TimeUnit timeoutUnit,
                            final long forciblyTimeout,
                            final @NotNull TimeUnit forciblyTimeoutUnit) {
        final Process process = this.getProcess();
        if (process == null) {
            return false;
        }

        process.destroy();

        if (process.onExit().orTimeout(timeout, timeoutUnit).thenApply(p -> false).exceptionally(t -> true).join()) {
            process.destroyForcibly();
            return process.onExit().orTimeout(forciblyTimeout, forciblyTimeoutUnit).thenApply(p -> true).exceptionally(t -> false).join();
        }
        return true;
    }

}