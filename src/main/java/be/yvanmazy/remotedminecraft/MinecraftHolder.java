package be.yvanmazy.remotedminecraft;

import be.yvanmazy.remotedminecraft.config.ProcessConfiguration;
import be.yvanmazy.remotedminecraft.controller.MinecraftController;
import be.yvanmazy.remotedminecraft.controller.agent.RemotedAgent;
import be.yvanmazy.remotedminecraft.state.MinecraftState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

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

}