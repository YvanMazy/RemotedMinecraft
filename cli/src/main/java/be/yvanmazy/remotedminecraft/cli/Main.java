package be.yvanmazy.remotedminecraft.cli;

import be.yvanmazy.remotedminecraft.config.ProcessConfiguration;

import java.nio.file.Path;

public class Main {

    public static void main(final String[] args) {
        if (args.length != 1 || !args[0].equals("test")) {
            System.err.println("Not released yet!");
            return;
        }

        final ProcessConfiguration config =
                ProcessConfiguration.newBuilder().version("1.21.2").processDirectory(Path.of(System.getenv("process_dir"))).build();

        System.out.println(config);
    }

}