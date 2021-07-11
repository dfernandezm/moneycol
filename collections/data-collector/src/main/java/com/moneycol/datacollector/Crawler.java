package com.moneycol.datacollector;

import io.micronaut.configuration.picocli.PicocliRunner;
import picocli.CommandLine;

@CommandLine.Command(name = "my-cli-app", description = "...",
        mixinStandardHelpOptions = true)
public class Crawler implements Runnable {

    public static void main(String[] args) throws Exception {
        PicocliRunner.run(Crawler.class, args);
    }

    public void run() {
        // business logic here
        System.out.println("Hi!");
    }
}