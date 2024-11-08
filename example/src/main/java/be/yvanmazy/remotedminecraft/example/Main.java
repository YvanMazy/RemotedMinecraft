package be.yvanmazy.remotedminecraft.example;

import be.yvanmazy.remotedminecraft.RemotedMinecraft;
import be.yvanmazy.remotedminecraft.config.ProcessConfiguration;
import be.yvanmazy.remotedminecraft.controller.MinecraftController;
import be.yvanmazy.remotedminecraft.controller.exception.AgentLoadingException;
import be.yvanmazy.remotedminecraft.example.agent.MyCustomAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.List;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(final String[] args) {
        String dir = System.getenv("process_dir");
        if (dir == null) {
            if (args.length < 1) {
                LOGGER.error("Please specify the process directory!");
                return;
            }
            dir = args[0];
        }

        RemotedMinecraft.run(ProcessConfiguration.newBuilder()
                .version("1.21.3")
                .processDirectory(Path.of(dir))
                .gameArguments(List.of("--quickPlayMultiplayer", "localhost:25565"))
                .build()).getReadyFuture().thenAccept(holder -> {
            final MinecraftController<MyCustomAgent> controller = holder.newController();
            try {
                controller.loadAgent(MyCustomAgent.ID);
            } catch (final AgentLoadingException e) {
                LOGGER.error("Failed to load agent", e);
                return;
            }
            try {
                final MyCustomAgent agent = controller.awaitReady();
                LOGGER.info("FPS: {}", agent.getFps());
            } catch (final AgentLoadingException | InterruptedException e) {
                LOGGER.error("Failed to await agent", e);
            } catch (final RemoteException e) {
                LOGGER.error("Failed to get fps", e);
            }
        });
    }

}