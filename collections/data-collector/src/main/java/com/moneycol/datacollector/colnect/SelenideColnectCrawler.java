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
import java.util.List;
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

    @Override
    public BanknotesDataSet startCrawler() {
        ColnectLandingPage colnectLandingPage = ColnectLandingPage.builder().build();
        colnectLandingPage.visit();

        List<CountrySeriesListing> countrySeriesListings = colnectLandingPage.countrySeriesListings();

        // Batches of 3 countries, then wait
        List<List<CountrySeriesListing>> countryGroups = Lists.partition(countrySeriesListings, 3);

        countryGroups.forEach(countrySeriesList -> {
            processCountryGroup(countrySeriesList);
            log.info("Waiting 5 seconds before proceeding with next group");
            sleep(5);
        });

        return null;
    }

    private void processCountryGroup(List<CountrySeriesListing> countrySeriesList) {
        List<CountryBanknotesListing> countryBanknotesListings = new ArrayList<>();
        countrySeriesList.forEach(countrySeries -> {
            countrySeries.visit();
            sleep(1);
            processCountryData(countryBanknotesListings, countrySeries);
            log.info("Waiting before processing next batch");
            sleep(3);
        });

        log.info("Country group finished");
    }

    private void processCountryData(List<CountryBanknotesListing> countryBanknotesListings, CountrySeriesListing countrySeries) {
        CountryBanknotesListing firstPageOfBanknoteData = countrySeries.visitAllBanknotesListing();
        String country = firstPageOfBanknoteData.getCountryName();

        log.info("Traversing data for country {} found", country);
        countryBanknotesListings.add(firstPageOfBanknoteData);
        List<BanknoteData> countryBanknoteData = traverseAllCountryBanknotes(firstPageOfBanknoteData);

        if (countryBanknoteData.size() > 0) {
            BanknotesDataSet banknotesDataSet = BanknotesDataSet.builder()
                    .country(country)
                    .banknotes(countryBanknoteData)
                    .build();

            log.info("Writing data batch for {}", country);

            // do it async
            //dataWriter.writeDataBatch(banknotesDataSet);
            log.info("Data for {} successfully written", country);
        }
    }

    private List<BanknoteData> traverseAllCountryBanknotes(CountryBanknotesListing firstListing) {
        CountryBanknotesListing currentListing = firstListing;
        List<BanknoteData> banknotesDataForCountry = new ArrayList<>();
        while (currentListing.hasMorePages()) {
            log.info("New page for banknotes in {}", firstListing);
            currentListing = currentListing.visitNextPage();

            List<BanknoteData> countryBanknoteData = processBanknoteListing(currentListing);
            BanknotesDataSet banknotesDataSet = BanknotesDataSet.builder()
                    .country(firstListing.getCountryName())
                    .banknotes(countryBanknoteData)
                    .pageNumber(currentListing.getPageNumber())
                    .build();

            dataWriter.writeDataBatch(banknotesDataSet);
            banknotesDataForCountry.addAll(countryBanknoteData);
            sleep(1);
        }
        return banknotesDataForCountry;
    }

    private List<BanknoteData> processBanknoteListing(CountryBanknotesListing banknotesListing) {
        List<BanknoteData> banknoteData = banknotesListing.banknoteDataForCurrentPage();
        banknoteData.forEach(data -> log.info(data.toString()));
        return banknoteData;
    }

    public static void main(String args[]) {
        SelenideColnectCrawler selenideColnectCrawler = new SelenideColnectCrawler(new GcsDataWriter());
        selenideColnectCrawler.setupCrawler();
        selenideColnectCrawler.startCrawler();
    }
}
