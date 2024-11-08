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

            final Path path = FileUtil.getSelf();
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
            this.remotedAgent = (T) LocateRegistry.getRegistry(port).lookup(id);

            if (this.remotedAgent != null && this.remotedAgent.isLoaded()) {
                return this.loaded = true;
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