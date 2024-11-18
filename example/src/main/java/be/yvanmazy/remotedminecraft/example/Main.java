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

package be.yvanmazy.remotedminecraft.example;

import be.yvanmazy.remotedminecraft.MinecraftHolder;
import be.yvanmazy.remotedminecraft.RemotedMinecraft;
import be.yvanmazy.remotedminecraft.config.ProcessConfiguration;
import be.yvanmazy.remotedminecraft.controller.MinecraftController;
import be.yvanmazy.remotedminecraft.controller.agent.AgentFileBuilder;
import be.yvanmazy.remotedminecraft.controller.exception.AgentConnectException;
import be.yvanmazy.remotedminecraft.controller.exception.AgentLoadingException;
import be.yvanmazy.remotedminecraft.controller.exception.AgentNotLoadedException;
import be.yvanmazy.remotedminecraft.example.agent.AgentMain;
import be.yvanmazy.remotedminecraft.example.agent.MyCustomAgent;
import be.yvanmazy.remotedminecraft.example.agent.MyCustomAgentImpl;
import be.yvanmazy.remotedminecraft.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final int AGENT_PORT = 1099;

    public static void main(final String[] args) {
        String dir = System.getenv("process_dir");
        if (dir == null) {
            if (args.length < 1) {
                LOGGER.error("Please specify the process directory!");
                return;
            }
            dir = args[0];
        }

        LOGGER.info("Starting...");

        // Build tiny agent jar
        final Path agentPath = new AgentFileBuilder(AgentMain.class).addClasses(MyCustomAgent.class, MyCustomAgentImpl.class)
                .addRemotedAgentClasses()
                .buildUnchecked();
        LOGGER.info("Agent is built: {}", agentPath);

        // Copy a Log4J configuration without writing
        final Path logConfiguration;
        try {
            logConfiguration = FileUtil.copyResource("no-write-log4j2.xml");
        } catch (final IOException e) {
            LOGGER.error("Failed to copy no-write-log4j2.xml", e);
            return;
        }

        // Run an example instance
        // With this setup you can run multiple instances simultaneously
        RemotedMinecraft.run(ProcessConfiguration.newBuilder().version("1.21.3") // Game version
                .processDirectory(Path.of(dir)) // Process directory (like .minecraft)
                .gameArguments(List.of("--quickPlayMultiplayer", "localhost:25565")) // Argument for quickly connecting to a server
                .jvmArguments(List.of("-Dlog4j.configurationFile=" + logConfiguration)) // Use the no-write configuration for logging
                .inheritIO(false) // Don't inherit logging
                .jvmAgentArg(agentPath.toString(), AGENT_PORT) // Register agent
                .build()).getReadyFuture().exceptionally(throwable -> {
            LOGGER.error("Failed to run process", throwable);
            return null;
        }).thenAccept(Main::onStart);
    }

    private static void onStart(final MinecraftHolder holder) {
        if (holder == null) {
            return;
        }
        LOGGER.info("Started! Loading agent... {}", Thread.currentThread().getName());
        // Creating a new controller
        final MinecraftController<MyCustomAgent> controller = holder.newController();
        try {
            if (!controller.connect(MyCustomAgent.ID, AGENT_PORT, 5L, TimeUnit.MILLISECONDS, 1L)) {
                LOGGER.error("Failed to connect agent");
                return;
            }
        } catch (final AgentConnectException e) {
            LOGGER.error("Failed to connect agent", e);
            return;
        }
        try {
            // Await game is ready (game menu)
            final MyCustomAgent agent = controller.awaitReady();
            // Wait 5 seconds (not required)
            Thread.sleep(5_000L);
            // Get Game FPS
            LOGGER.info("FPS: {}", agent.getFps());
        } catch (final AgentNotLoadedException e) {
            LOGGER.error("Agent is not loaded", e);
        } catch (final AgentLoadingException | InterruptedException e) {
            LOGGER.error("Failed to await agent", e);
        } catch (final Exception e) {
            LOGGER.error("Failed to get fps", e);
        } finally {
            // Destroy the process
            controller.process().destroy();
        }
        // Await the process to exit
        if (controller.process().onExit().orTimeout(5, TimeUnit.SECONDS).thenApply(p -> false).exceptionally(t -> true).join()) {
            LOGGER.error("Failed to shutdown process");
        }
    }

}