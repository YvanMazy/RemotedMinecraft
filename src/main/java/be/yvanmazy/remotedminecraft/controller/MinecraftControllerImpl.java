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
import be.yvanmazy.remotedminecraft.util.FileUtil;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Objects;

final class MinecraftControllerImpl<T extends RemotedAgent> implements MinecraftController<T> {

    private final Process process;

    private boolean loaded;
    private T remotedAgent;

    MinecraftControllerImpl(final @NotNull Process process) {
        this.process = Objects.requireNonNull(process, "process must not be null");
    }

    @Override
    public void loadAgent(final @NotNull String id, final int port) throws AgentLoadingException {
        try {
            final VirtualMachine machine = VirtualMachine.attach(String.valueOf(this.process.pid()));

            final Path path = FileUtil.getSelf(StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass());
            if (Files.notExists(path)) {
                throw new AgentLoadingException("Agent file not found!");
            }
            machine.loadAgent(path.toAbsolutePath().toString(), String.valueOf(port));

            machine.detach();
        } catch (final AttachNotSupportedException | IOException e) {
            throw new AgentLoadingException("Failed to attach process", e);
        } catch (final AgentLoadException | AgentInitializationException e) {
            throw new AgentLoadingException("Failed to load agent", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean connect(final @NotNull String id, final int port) throws AgentConnectException {
        try {
            final T agent = (T) LocateRegistry.getRegistry(port).lookup(id);

            if (agent != null) {
                return this.loaded = (this.remotedAgent = agent).isLoaded();
            }
        } catch (final RemoteException | NotBoundException e) {
            throw new AgentConnectException("Failed to connect", e);
        }
        return false;
    }

    @Override
    public boolean isLoaded() {
        return this.loaded;
    }

    @Override
    public boolean isReady() {
        this.checkLoaded();
        try {
            return this.remotedAgent.isReady();
        } catch (final RemoteException e) {
            return false;
        }
    }

    @Override
    public T agent() {
        this.checkLoaded();
        return this.remotedAgent;
    }

    @Override
    public @NotNull Process process() {
        return this.process;
    }

    private void checkLoaded() throws AgentNotLoadedException {
        if (!this.loaded) {
            throw new AgentNotLoadedException();
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        final var that = (MinecraftControllerImpl<?>) obj;
        return Objects.equals(this.process, that.process);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.process);
    }

    @Override
    public String toString() {
        return "MinecraftControllerImpl[" + "process=" + this.process + ']';
    }

}