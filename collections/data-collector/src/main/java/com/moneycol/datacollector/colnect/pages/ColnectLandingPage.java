package com.moneycol.datacollector.colnect.pages;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.SelenideElement;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

@Slf4j
public class ColnectLandingPage {

    private final static String BANKNOTES_BY_COUNTRY_ENG_URL = "https://colnect.com/en/banknotes/countries";
    private static final ExecutorService executorService = Executors.newFixedThreadPool(5);

    private final List<SelenideElement> countriesLinks = $$("#pl_350 > a");
    private String url;

    @Builder
    public ColnectLandingPage(String url) {
        this.url = url != null ? url : BANKNOTES_BY_COUNTRY_ENG_URL;
    }

    public List<CountrySeriesListing> countrySeriesListings() {
        return countriesLinks
                .parallelStream()
                .map(this::countriesListingPageFrom)
                .collect(Collectors.toList());
    }

    private CountrySeriesListing countriesListingPageFrom(SelenideElement countryLinkEl) {
        String countryLink = countryLinkEl.attr("href");
        String countryName = countryNameFrom(countryLinkEl.getText());
        log.info("Getting links for Country {}, Link {}", countryName, countryLink);
        return CountrySeriesListing.builder()
                .url(countryLink)
                .countryName(countryName)
                .build();
    }

    private String countryNameFrom(String text) {
        String toRemove = text.substring(text.lastIndexOf("("), text.length());
        String countryName = text.replace(toRemove, "");
        return countryName.trim();
    }

    private void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException ignored) {
        }
    }

    public void visit() {
        open(url);
    }

    public static void main(String[] args) {
        Configuration.headless = true;
        String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
        System.setProperty("chromeoptions.args", "--user-agent=" + userAgent);

        ColnectLandingPage colnectLandingPage = open(BANKNOTES_BY_COUNTRY_ENG_URL, ColnectLandingPage.class);
        List<CountrySeriesListing> countrySeriesListing = colnectLandingPage.countrySeriesListings();
        Map<String, List<BanknoteData>> countriesData = new LinkedHashMap<>();

        List<List<CountrySeriesListing>> partitions = Lists.partition(countrySeriesListing, 3);

        // https://stackoverflow.com/questions/19348248/waiting-on-a-list-of-future
        partitions.forEach(countrySeriesList -> {
            countrySeriesList.forEach(countrySeries -> {
                        log.info("Starting data for {}", countrySeries.toString());
                        Future<?> countryCompletionFuture = executorService.submit(() -> {

                            //CountrySeriesListing firstCountrySeriesListing = countrySeries;
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
                                colnectLandingPage.sleep(1);
                            }

                            countriesData.keySet().stream().peek(key -> log.info(">>> Data for {}", key)).close();
                        });

                    });

            colnectLandingPage.sleep(25);
        });

        colnectLandingPage.sleep(3600000);
    }
}
