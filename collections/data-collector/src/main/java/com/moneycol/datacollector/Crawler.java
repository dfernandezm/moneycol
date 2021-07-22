package com.moneycol.datacollector;

import com.moneycol.datacollector.colnect.ColnectCrawlerClient;
import com.moneycol.datacollector.colnect.SelenideColnectCrawler;
import com.moneycol.datacollector.colnect.collector.GcsDataWriter;
import io.micronaut.configuration.picocli.PicocliRunner;
import picocli.CommandLine;

@CommandLine.Command(
        name = "crawl",
        description = "...",
        mixinStandardHelpOptions = true)
public class Crawler implements Runnable {

    public static void main(String[] args) throws Exception {
        PicocliRunner.run(Crawler.class, args);
    }

    public void run() {
        ColnectCrawlerClient colnectCrawler = new SelenideColnectCrawler(new GcsDataWriter());
        colnectCrawler.setupCrawler();
        colnectCrawler.crawl();
    }
}