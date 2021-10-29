package com.moneycol.datacollector;

import com.moneycol.datacollector.colnect.CrawlerNotifier;
import io.micronaut.configuration.picocli.PicocliRunner;
import picocli.CommandLine.Command;

import javax.inject.Inject;

@Command(
        name = "crawl",
        description = "...",
        mixinStandardHelpOptions = true)
public class Crawler implements Runnable {

    @Inject
    private CrawlerNotifier crawlerNotifier;

    public static void main(String[] args) throws Exception {
        PicocliRunner.run(Crawler.class, args);
    }

    public void run() {
//        ColnectCrawlerClient colnectCrawler = new SelenideColnectCrawler(new GcsDataWriter());
//        colnectCrawler.setupCrawler();
//        colnectCrawler.crawl();


        crawlerNotifier.notifyDone();

        String a = "";
    }
}