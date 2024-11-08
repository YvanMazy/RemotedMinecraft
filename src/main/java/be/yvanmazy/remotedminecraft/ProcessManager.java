package be.yvanmazy.remotedminecraft;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

final class ProcessManager {

    private static final ProcessManager INSTANCE = new ProcessManager();
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessManager.class);

    private final List<Process> processes = new ArrayList<>();
    private final Object mutex = new Object();

    private ProcessManager() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopAll, "RemotedMinecraft Shutdown Hook"));
    }

    public void stopAll() {
        final Process[] array;
        synchronized (this.mutex) {
            array = this.processes.toArray(Process[]::new);
            this.processes.clear();
        }
        for (final Process process : array) {
            try {
                if (process.isAlive()) {
                    process.destroy();
                    if (!process.waitFor(5, TimeUnit.SECONDS)) {
                        process.destroyForcibly();
                    }
                }
            } catch (final Exception e) {
                LOGGER.error("Failed to shutdown process '{}'", process.pid(), e);
            }
        }
    }

    public void register(final @NotNull Process process) {
        Objects.requireNonNull(process, "process must not be null");
        synchronized (this.mutex) {
            this.processes.add(process);
        }
    }

    static ProcessManager getInstance() {
        return INSTANCE;
    }

}