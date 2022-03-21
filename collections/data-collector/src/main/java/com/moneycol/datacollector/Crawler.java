package com.moneycol.datacollector;

import com.moneycol.datacollector.colnect.ColnectCrawlerClient;
import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.context.env.Environment;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;

import jakarta.inject.Inject;

@Slf4j
@Command(
        name = "crawl",
        description = "...",
        mixinStandardHelpOptions = true)
public class Crawler implements Runnable {

    @Inject
    private ColnectCrawlerClient crawlerClient;

    @Inject
    private Environment environment;

    public static void main(String[] args) {
        PicocliRunner.run(Crawler.class, args);
    }

    public void run() {
        log.info("Starting crawler for Colnect site");
        log.info("PubSub done topic from environment var CRAWLING_DONE_TOPIC_NAME: {}",
                System.getenv("CRAWLING_DONE_TOPIC_NAME"));
        log.info("PubSub done topic from environment prop crawling.done-topic-name: {}",
                environment.get("crawling.done-topic-name", String.class).orElse("dev.crawler.test"));

        crawlerClient.setupCrawler();
        crawlerClient.crawl();
    }
}