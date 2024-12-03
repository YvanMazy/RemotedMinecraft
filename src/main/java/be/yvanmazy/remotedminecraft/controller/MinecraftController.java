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

package be.yvanmazy.remotedminecraft.controller;

import be.yvanmazy.remotedminecraft.controller.agent.RemotedAgent;
import be.yvanmazy.remotedminecraft.controller.exception.AgentConnectException;
import be.yvanmazy.remotedminecraft.controller.exception.AgentLoadingException;
import be.yvanmazy.remotedminecraft.controller.exception.AgentNotLoadedException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public interface MinecraftController<T extends RemotedAgent> {

    @Contract("_ -> new")
    static <T extends RemotedAgent> @NotNull MinecraftController<T> build(final @NotNull Process process) {
        return new MinecraftControllerImpl<>(process);
    }

    default void loadAgent(final @NotNull String id) throws AgentLoadingException {
        this.loadAgent(id, 1099);
    }

    void loadAgent(final @NotNull String id, final int port) throws AgentLoadingException;

    boolean connect(final @NotNull String id, final int port) throws AgentConnectException;

    default boolean connect(final @NotNull String id,
                            final int port,
                            final long timeout,
                            final TimeUnit timeUnit) throws AgentConnectException {
        return this.connect(id, port, timeout, timeUnit, 100L);
    }

    @SuppressWarnings("BusyWait")
    default boolean connect(final @NotNull String id,
                            final int port,
                            final long timeout,
                            final TimeUnit timeUnit,
                            final long checkInterval) throws AgentConnectException {
        final long endTime = System.currentTimeMillis() + timeUnit.toMillis(timeout);

        do {
            try {
                return this.connect(id, port);
            } catch (final AgentConnectException e) {
                try {
                    Thread.sleep(checkInterval);
                } catch (final InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new AgentConnectException("Thread was interrupted during connection attempts", ie);
                }
            }
        } while (System.currentTimeMillis() < endTime);

        throw new AgentConnectException("Failed to connect within the specified timeout");
    }

    @Contract(pure = true)
    boolean isLoaded();

    @Contract(pure = true)
    boolean isReady() throws AgentLoadingException;

    @Contract(pure = true)
    T agent();

    default @NotNull T awaitReady() throws AgentLoadingException, InterruptedException {
        return this.awaitReady(50L, -1L, TimeUnit.SECONDS);
    }

    default @NotNull T awaitReady(final long timeout,
                                  final @NotNull TimeUnit timeoutUnit) throws AgentLoadingException, InterruptedException {
        return this.awaitReady(50L, timeout, timeoutUnit);
    }

    @SuppressWarnings("BusyWait")
    default @NotNull T awaitReady(final long checkInterval,
                                  final long timeout,
                                  final @NotNull TimeUnit timeoutUnit) throws AgentLoadingException, InterruptedException {
        final long endTime = timeout > 0 ? System.currentTimeMillis() + timeoutUnit.toMillis(timeout) : -1L;
        while (this.process().isAlive()) {
            if (!this.isReady()) {
                if (endTime > 0 && System.currentTimeMillis() > endTime) {
                    throw new AgentLoadingException("Agent failed to load within the specified timeout");
                }
                Thread.sleep(checkInterval);
                continue;
            }
            return this.agent();
        }
        throw new AgentNotLoadedException();
    }

    @Contract(pure = true)
    @NotNull Process process();

}