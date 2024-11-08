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

import be.yvanmazy.remotedminecraft.RemotedMinecraft;
import be.yvanmazy.remotedminecraft.config.ProcessConfiguration;
import be.yvanmazy.remotedminecraft.controller.MinecraftController;
import be.yvanmazy.remotedminecraft.controller.exception.AgentConnectException;
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

        LOGGER.info("Starting...");
        RemotedMinecraft.run(ProcessConfiguration.newBuilder()
                .version("1.21.3")
                .processDirectory(Path.of(dir))
                .gameArguments(List.of("--quickPlayMultiplayer", "localhost:25565"))
                .jvmAgentArg(1099)
                .build()).getReadyFuture().thenAccept(holder -> {
            LOGGER.info("Started! Loading agent...");
            final MinecraftController<MyCustomAgent> controller = holder.newController();
            try {
                controller.connect(MyCustomAgent.ID, 1099);
            } catch (final AgentConnectException e) {
                LOGGER.error("Failed to connect agent", e);
                return;
            }
            try {
                final MyCustomAgent agent = controller.awaitReady();
                LOGGER.info("FPS: {}", agent.getFps());
            } catch (final AgentLoadingException | InterruptedException e) {
                LOGGER.error("Failed to await agent", e);
            } catch (final RemoteException e) {
                LOGGER.error("Failed to get fps", e);
            } finally {
                controller.process().destroy();
            }
        });
    }

}