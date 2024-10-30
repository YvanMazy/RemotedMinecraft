package be.yvanmazy.remotedminecraft;

import be.yvanmazy.remotedminecraft.config.ProcessConfiguration;
import be.yvanmazy.remotedminecraft.controller.MinecraftController;
import be.yvanmazy.remotedminecraft.state.MinecraftState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

final class MinecraftHolderImpl implements MinecraftHolder {

    private final ProcessConfiguration configuration;
    private final CompletableFuture<MinecraftController> controllerFuture = new CompletableFuture<>();

    private MinecraftState state = MinecraftState.STARTING;
    private Process process;

    MinecraftHolderImpl(final @NotNull ProcessConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "configuration must not be null");
    }

    @Override
    public @NotNull CompletableFuture<MinecraftController> getControllerAsynchronously() {
        return this.controllerFuture;
    }

    @Override
    public @Nullable MinecraftController getController() {
        if (this.controllerFuture.isDone() && !this.controllerFuture.isCompletedExceptionally() && !this.controllerFuture.isCancelled()) {
            return this.controllerFuture.getNow(null);
        }
        return null;
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
        this.process = process;
        this.state = MinecraftState.STARTED;
        process.onExit().whenComplete((p, throwable) -> {
            this.state = MinecraftState.CLOSED;
            if (!this.controllerFuture.isDone()) {
                this.controllerFuture.cancel(true);
            }
        });
    }

    void completeExceptionally(final @NotNull Throwable throwable) {
        this.state = MinecraftState.CLOSED;
        this.controllerFuture.completeExceptionally(throwable);
    }

}