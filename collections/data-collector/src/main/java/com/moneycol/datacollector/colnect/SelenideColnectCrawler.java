package com.moneycol.datacollector.colnect;

import com.codeborne.selenide.Configuration;
import com.google.common.collect.Lists;
import com.moneycol.datacollector.colnect.collector.CrawlingProcessState;
import com.moneycol.datacollector.colnect.collector.DataWriter;
import com.moneycol.datacollector.colnect.pages.BanknoteData;
import com.moneycol.datacollector.colnect.pages.ColnectLandingPage;
import com.moneycol.datacollector.colnect.pages.CountryBanknotesListing;
import com.moneycol.datacollector.colnect.pages.CountrySeriesListing;
import io.micronaut.core.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
public class SelenideColnectCrawler implements ColnectCrawlerClient {

    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    private DataWriter dataWriter;

    public SelenideColnectCrawler(DataWriter dataWriter) {
        this.dataWriter = dataWriter;
    }

    public void setupCrawler() {
        String chromeDriverLocation = System.getenv("CHROME_DRIVER_LOCATION");

        if (chromeDriverLocation == null) {
            System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");
            log.info("Chrome driver location defaulted to: /usr/bin/chromedriver");
        } else {
            System.setProperty("webdriver.chrome.driver", chromeDriverLocation);
            log.info("Chrome driver location et to: {}", chromeDriverLocation);
        }

        Configuration.headless = true;
        System.setProperty("chromeoptions.args", "--user-agent=" + USER_AGENT);
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

    @VisibleForTesting
    public List<CountrySeriesListing> skipUntil(List<CountrySeriesListing> series, String seriesUrl) {
        int currentSeriesIndex = IntStream.range(0, series.size())
                .filter(j -> series.get(j).getUrl().equals(seriesUrl))
                .findFirst()
                .orElse(-1);

        if (currentSeriesIndex == -1) {
            log.info("Cannot find series url in the list -- assuming all still need to be processed");
            return series;
        }

        return series.subList(currentSeriesIndex, series.size());
    }

    private void processCountryGroup(CrawlingProcessState crawlingProcessState, List<CountrySeriesListing> countrySeriesList) {
        List<CountryBanknotesListing> countryBanknotesListings = new ArrayList<>();
        countrySeriesList.forEach(countrySeries -> {
            countrySeries.visit();
            sleep(1);
            processCountryData(crawlingProcessState, countryBanknotesListings, countrySeries);
            resetState(crawlingProcessState);
            log.info("Waiting before processing next batch");
            sleep(3);
        });

        log.info("Country group finished");
    }

    private void resetState(CrawlingProcessState crawlingProcessState) {
        log.info("Resetting state urls as series has been completed");
        crawlingProcessState.setCurrentUrl(null);
        crawlingProcessState.setSeriesUrl(null);
        crawlingProcessState.setPageNumber(null);
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
}