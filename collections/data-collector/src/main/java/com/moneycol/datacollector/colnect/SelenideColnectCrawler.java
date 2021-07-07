package com.moneycol.datacollector.colnect;

import com.codeborne.selenide.Configuration;
import com.google.common.collect.Lists;
import com.moneycol.datacollector.colnect.collector.DataWriter;
import com.moneycol.datacollector.colnect.collector.GcsDataWriter;
import com.moneycol.datacollector.colnect.pages.BanknoteData;
import com.moneycol.datacollector.colnect.pages.ColnectLandingPage;
import com.moneycol.datacollector.colnect.pages.CountryBanknotesListing;
import com.moneycol.datacollector.colnect.pages.CountrySeriesListing;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class SelenideColnectCrawler implements ColnectCrawlerClient {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private static final String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    private DataWriter dataWriter;

    public SelenideColnectCrawler(DataWriter dataWriter) {
        this.dataWriter = dataWriter;
    }

    public void setupCrawler() {
        Configuration.headless = true;
        System.setProperty("chromeoptions.args", "--user-agent=" + userAgent);
    }

    private void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException ignored) {
        }
    }

    // https://stackoverflow.com/questions/19348248/waiting-on-a-list-of-future

    @Override
    public ColnectBanknotesDataSet startCrawler() {
        ColnectLandingPage colnectLandingPage = ColnectLandingPage.builder().build();
        colnectLandingPage.visit();

        List<CountrySeriesListing> countrySeriesListings = colnectLandingPage.countrySeriesListings();
        Map<String, List<BanknoteData>> countriesData = new LinkedHashMap<>();

        // Batches of 3 countries, then wait
        List<List<CountrySeriesListing>> countryGroups = Lists.partition(countrySeriesListings, 3);

        countryGroups.forEach(countrySeriesList -> {
            processCountry(countriesData, countrySeriesList);
            sleep(1);
        });

        sleep(3600000);

        return null;
    }

    private void processCountry(Map<String, List<BanknoteData>> countriesData, List<CountrySeriesListing> countrySeriesList) {

        List<CountryBanknotesListing> countryBanknotesListings = new ArrayList<>();


        countrySeriesList.forEach(countrySeries -> {
            countrySeries.visit();
            sleep(1);
            CountryBanknotesListing firstPageOfBanknoteData = countrySeries.visitAllBanknotesListing();
            log.info("Data for country {} found", firstPageOfBanknoteData.getCountryName());
            countryBanknotesListings.add(firstPageOfBanknoteData);
            log.info("Waiting");
            sleep(3);
        });

        log.info("Listing finished");
    }

    private void traverseCountryBanknotesListing(CountryBanknotesListing firstListing) {
        CountryBanknotesListing currentListing = firstListing;
        while (firstListing.hasMorePages()) {
            log.info("New page for banknotes in {}", firstListing);
            currentListing = currentListing.visitNextPage();
            processBanknoteListing(currentListing);
            sleep(1);
        }
    }

    private void processBanknoteListing(CountryBanknotesListing banknotesListing) {
        List<BanknoteData> banknoteData = banknotesListing.banknoteDataForCurrentPage();
        banknoteData.forEach(data -> log.info(data.toString()));
    }

    public static void main(String args[]) {
        SelenideColnectCrawler selenideColnectCrawler = new SelenideColnectCrawler(new GcsDataWriter());
        selenideColnectCrawler.setupCrawler();
        selenideColnectCrawler.startCrawler();
    }
}
