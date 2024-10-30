package be.yvanmazy.remotedminecraft;

import be.yvanmazy.remotedminecraft.config.ProcessConfiguration;
import org.jetbrains.annotations.NotNull;

public final class RemotedMinecraft {

    public static @NotNull MinecraftHolder run(final @NotNull ProcessConfiguration configuration) {
        final var holder = new MinecraftHolderImpl(configuration);
        final ProcessThread thread = new ProcessThread(holder);
        thread.start();
        return holder;
    }

    private RemotedMinecraft() throws IllegalAccessException {
        throw new IllegalAccessException("You cannot instantiate this class");
    }

}