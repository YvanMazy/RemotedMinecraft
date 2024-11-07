package be.yvanmazy.remotedminecraft.controller;

import be.yvanmazy.remotedminecraft.controller.agent.RemotedAgent;
import be.yvanmazy.remotedminecraft.controller.exception.AgentLoadingException;
import be.yvanmazy.remotedminecraft.controller.exception.AgentNotLoadedException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface MinecraftController<T extends RemotedAgent> {

    static <T extends RemotedAgent> MinecraftController<T> build(final @NotNull Process process) {
        return new MinecraftControllerImpl<>(process);
    }

    default void loadAgent(final @NotNull String id) throws AgentLoadingException {
        this.loadAgent(id, 1099);
    }

    void loadAgent(final @NotNull String id, final int port) throws AgentLoadingException;

    @Contract(pure = true)
    boolean isLoaded();

    @Contract(pure = true)
    boolean isReady() throws AgentLoadingException;

    @Contract(pure = true)
    T agent();

    default @NotNull T awaitReady() throws AgentLoadingException, InterruptedException {
        return this.awaitReady(50L);
    }

    default @NotNull T awaitReady(final long checkInterval) throws AgentLoadingException, InterruptedException {
        while (this.process().isAlive()) {
            if (!this.isReady()) {
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