package com.moneycol.datacollector.colnect;

import com.codeborne.selenide.Configuration;
import com.google.common.collect.Lists;
import com.moneycol.datacollector.colnect.collector.CrawlingProcessState;
import com.moneycol.datacollector.colnect.collector.DataWriter;
import com.moneycol.datacollector.colnect.collector.GcsDataWriter;
import com.moneycol.datacollector.colnect.pages.BanknoteData;
import com.moneycol.datacollector.colnect.pages.ColnectLandingPage;
import com.moneycol.datacollector.colnect.pages.CountryBanknotesListing;
import com.moneycol.datacollector.colnect.pages.CountrySeriesListing;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SelenideColnectCrawler implements ColnectCrawlerClient {

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
        log.info("Waiting for {} seconds before continuing", seconds);
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException ignored) {
        }
    }


    // https://github.com/Zenika/alpine-chrome
    @Override
    public void crawl() {
        ColnectLandingPage colnectLandingPage = ColnectLandingPage.builder().build();
        colnectLandingPage.visit();

        List<CountrySeriesListing> countrySeriesListings = colnectLandingPage.countrySeriesListings();

        // TODO: recover from existing state (discard all previous)
        // Batches of 3 countries, then wait
        List<List<CountrySeriesListing>> countryGroups = Lists.partition(countrySeriesListings, 3);
        countryGroups.forEach(countrySeriesList -> {
            processCountryGroup(countrySeriesList);
            log.info("Waiting 5 seconds before proceeding with next group");
            sleep(5);
        });
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
        String allBanknotesForThisCountryUrl = firstPageOfBanknoteData.getUrl();
        String countryName = firstPageOfBanknoteData.getCountryName();

        log.info("Traversing data for country {} found", countryName);
        countryBanknotesListings.add(firstPageOfBanknoteData);
        collectAllCountryBanknotesFrom(allBanknotesForThisCountryUrl, firstPageOfBanknoteData);
    }

    private List<BanknoteData> collectAllCountryBanknotesFrom(String allBanknotesUrl, CountryBanknotesListing firstListing) {
        CountryBanknotesListing currentListing = firstListing;
        int pageNumber = currentListing.getPageNumber();

        List<BanknoteData> banknotesDataForCountry = processBanknotesInListing(currentListing, pageNumber);
        saveState(allBanknotesUrl, pageNumber);

        while (currentListing.hasMorePages()) {
            sleep(1);
            currentListing = currentListing.visitNextPage();
            pageNumber = currentListing.getPageNumber();
            processBanknotesInListing(currentListing, pageNumber);
            saveState(currentListing.getUrl(), pageNumber);
        }

        return banknotesDataForCountry;
    }

    private List<BanknoteData> processBanknotesInListing(CountryBanknotesListing currentListing, int pageNumber) {
        log.info("New page {} for banknotes in {}", pageNumber, currentListing);
        List<BanknoteData> countryBanknoteData = processBanknoteListing(currentListing);
        writeBankotesData(currentListing, pageNumber, countryBanknoteData);
        return new ArrayList<>(countryBanknoteData);
    }

    private void writeBankotesData(CountryBanknotesListing firstListing, int pageNumber, List<BanknoteData> countryBanknoteData) {
        if (countryBanknoteData.size() > 0) {
            BanknotesDataSet banknotesDataSet = BanknotesDataSet.builder()
                    .language("en")
                    .country(firstListing.getCountryName())
                    .banknotes(countryBanknoteData)
                    .pageNumber(pageNumber)
                    .build();
            dataWriter.writeDataBatch(banknotesDataSet);
        }
    }

    private List<BanknoteData> processBanknoteListing(CountryBanknotesListing banknotesListing) {
        return banknotesListing.banknoteDataForCurrentPage();
    }

    private void saveState(String url, Integer pageNumber) {
        CrawlingProcessState crawlingProcessState = CrawlingProcessState.builder()
                .countryListingUrl(url)
                .pageNumber(pageNumber)
                .build();
        dataWriter.saveState(crawlingProcessState);
    }

    public static void main(String args[]) {
        SelenideColnectCrawler selenideColnectCrawler = new SelenideColnectCrawler(new GcsDataWriter());
        selenideColnectCrawler.setupCrawler();
        selenideColnectCrawler.crawl();
    }
}
