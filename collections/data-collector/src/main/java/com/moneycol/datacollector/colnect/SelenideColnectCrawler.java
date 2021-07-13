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
import io.micronaut.core.util.StringUtils;
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
        String chromeDriverLocation = System.getenv("CHROME_DRIVER_LOCATION");
        if (chromeDriverLocation == null) {
            System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");
        }
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

        // find state file, skip series listings
        log.info("Checking for state file");
        CrawlingProcessState crawlingProcessState = dataWriter.findState();
        countrySeriesListings = skipUntil(countrySeriesListings, crawlingProcessState.getSeriesUrl());

        // Batches of 3 countries, then wait
        List<List<CountrySeriesListing>> countryGroups = Lists.partition(countrySeriesListings, 3);
        countryGroups.forEach(countrySeriesList -> {
            processCountryGroup(crawlingProcessState, countrySeriesList);
            log.info("Waiting 5 seconds before proceeding with next group");
            sleep(5);
        });
    }

    public List<CountrySeriesListing> skipUntil(List<CountrySeriesListing> series, String seriesUrl) {
        List<CountrySeriesListing> countrySeriesListings = new ArrayList<>();
        int i = 0;
        boolean found = false;
        while (i < series.size()) {
            CountrySeriesListing serie = series.get(i);
            if (found) {
                countrySeriesListings.add(serie);
            } else {
                found = serie.getUrl().equals(seriesUrl);
                if (found) {
                    log.info("Found url in state {}", seriesUrl);
                    countrySeriesListings.add(serie);
                } else {
                    log.info("Url not found yet, skipping {}", serie.getUrl());
                }
            }
            i++;
        }

        // Series from the one in state
        return countrySeriesListings;
    }

    private void processCountryGroup(CrawlingProcessState crawlingProcessState, List<CountrySeriesListing> countrySeriesList) {
        List<CountryBanknotesListing> countryBanknotesListings = new ArrayList<>();
        countrySeriesList.forEach(countrySeries -> {
            countrySeries.visit();
            sleep(1);
            processCountryData(crawlingProcessState, countryBanknotesListings, countrySeries);
            log.info("Waiting before processing next batch");
            sleep(3);
        });

        log.info("Country group finished");
    }

    private void processCountryData(CrawlingProcessState crawlingProcessState, List<CountryBanknotesListing> countryBanknotesListings, CountrySeriesListing countrySeries) {
        CountryBanknotesListing firstPageOfBanknoteData;
        String countryName = countrySeries.getCountryName();
        String banknotesListingUrl = crawlingProcessState.getCurrentUrl();

        if (StringUtils.isNotEmpty(banknotesListingUrl)) {
            log.info("Found saved listing url -- {}, starting from it", banknotesListingUrl);
            firstPageOfBanknoteData = CountryBanknotesListing.builder()
                                        .pageNumber(crawlingProcessState.getPageNumber())
                                        .countryName(countryName)
                                        .enclosingLink(banknotesListingUrl)
                                        .build();
            firstPageOfBanknoteData.visit();
        } else {
            firstPageOfBanknoteData = countrySeries.visitAllBanknotesListing();
            countryName = firstPageOfBanknoteData.getCountryName();
            banknotesListingUrl = firstPageOfBanknoteData.getUrl();
        }

        log.info("Traversing data for country {} found", countryName);
        countryBanknotesListings.add(firstPageOfBanknoteData);
        collectAllCountryBanknotesFrom(countrySeries.getUrl(), banknotesListingUrl, firstPageOfBanknoteData);
    }

    private List<BanknoteData> collectAllCountryBanknotesFrom(String countrySeriesUrl, String allBanknotesUrl, CountryBanknotesListing firstListing) {
        CountryBanknotesListing currentListing = firstListing;
        int pageNumber = currentListing.getPageNumber();

        List<BanknoteData> banknotesDataForCountry = processBanknotesInListing(currentListing, pageNumber);
        saveState(countrySeriesUrl, allBanknotesUrl, pageNumber);

        while (currentListing.hasMorePages()) {
            sleep(1);
            currentListing = currentListing.visitNextPage();
            pageNumber = currentListing.getPageNumber();
            processBanknotesInListing(currentListing, pageNumber);
            saveState(countrySeriesUrl, currentListing.getUrl(), pageNumber);
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

    private void saveState(String countrySeriesUrl, String countryListingUrl, Integer pageNumber) {
        CrawlingProcessState crawlingProcessState = CrawlingProcessState.builder()
                .seriesUrl(countrySeriesUrl)
                .currentUrl(countryListingUrl)
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
