package com.moneycol.datacollector.colnect;

import com.codeborne.selenide.Configuration;
import com.google.common.collect.Lists;
import com.moneycol.datacollector.colnect.pages.BanknoteData;
import com.moneycol.datacollector.colnect.pages.ColnectLandingPage;
import com.moneycol.datacollector.colnect.pages.CountryBanknotesListing;
import com.moneycol.datacollector.colnect.pages.CountrySeriesListing;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class SelenideColnectCrawler implements ColnectCrawlerClient {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(5);

    public void setupCrawler() {
        Configuration.headless = true;
        String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
        System.setProperty("chromeoptions.args", "--user-agent=" + userAgent);
    }

    private void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public ColnectBanknotesDataSet startCrawler() {
        Configuration.headless = true;
        String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
        System.setProperty("chromeoptions.args", "--user-agent=" + userAgent);

        ColnectLandingPage colnectLandingPage = ColnectLandingPage.builder().build();
        colnectLandingPage.visit();

        List<CountrySeriesListing> countrySeriesListing = colnectLandingPage.countriesSeriesListings();
        Map<String, List<BanknoteData>> countriesData = new LinkedHashMap<>();

        List<List<CountrySeriesListing>> partitions = Lists.partition(countrySeriesListing, 3);

        // https://stackoverflow.com/questions/19348248/waiting-on-a-list-of-future
        partitions.forEach(countrySeriesList -> {
            countrySeriesList.forEach(countrySeries -> {
                log.info("Starting data for {}", countrySeries.toString());
                Future<?> countryCompletionFuture = executorService.submit(() -> {

                    countrySeries.visit();

                    CountryBanknotesListing countryBanknotesListing = countrySeries.visitAllBanknotesListing();
                    List<BanknoteData> banknoteData = countryBanknotesListing.banknoteDataForCurrentPage();
                    banknoteData.forEach(data -> log.info(data.toString()));

                    while (countryBanknotesListing.hasMorePages()) {
                        log.info("There is more pages to visit");
                        countryBanknotesListing = countryBanknotesListing.visitNextPage();
                        List<BanknoteData> nextBanknoteData = countryBanknotesListing.banknoteDataForCurrentPage();
                        nextBanknoteData.forEach(data -> log.info(data.toString()));
                        countriesData.put(countryBanknotesListing.getCountryName(), banknoteData);
                        sleep(1);
                    }

                    countriesData.keySet().stream().peek(key -> log.info(">>> Data for {}", key)).close();
                });
            });

            sleep(25);
        });

        sleep(3600000);

        return null;
    }

    public static void main(String args[]) {
        SelenideColnectCrawler selenideColnectCrawler = new SelenideColnectCrawler();
        selenideColnectCrawler.setupCrawler();
        selenideColnectCrawler.startCrawler();
    }
}
