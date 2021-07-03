package com.moneycol.datacollector.colnect.pages;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

@Slf4j
public class ColnectLandingPage {

    private final static String BANKNOTES_BY_COUNTRY_ENG_URL = "https://colnect.com/en/banknotes/countries";

    public String banknotesByCountryLink() {
        return BANKNOTES_BY_COUNTRY_ENG_URL;
    }

    public List<CountrySeriesListing> obtainCountries() {
        return $$("#pl_350 > a").stream().map(element -> {
            String countryLink = element.attr("href");
            String countryName = countryNameFrom(element.getText());
            log.info("Country {}, Link {}", countryName, countryLink);
            return new CountrySeriesListing(countryLink, countryName);
        }).collect(Collectors.toList());
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

    public static void main(String[] args) {
        ColnectLandingPage colnectLandingPage = open(BANKNOTES_BY_COUNTRY_ENG_URL, ColnectLandingPage.class);

        List<CountrySeriesListing> countrySeriesListing = colnectLandingPage.obtainCountries();
        CountrySeriesListing firstCountrySeriesListing = countrySeriesListing.get(0);
        firstCountrySeriesListing.visit();

        CountryBanknotesListing countryBanknotesListing = firstCountrySeriesListing.visitAllBanknotesListing();
        List<BanknoteData> banknoteData = countryBanknotesListing.banknoteDataForCurrentPage();
        banknoteData.forEach(data -> log.info(data.toString()));

        if (countryBanknotesListing.hasMorePages()) {
            log.info("There is more pages to visit");
            CountryBanknotesListing nextCountryBanknotesListing = countryBanknotesListing.visitNextPage();
            List<BanknoteData> nextBanknoteData = nextCountryBanknotesListing.banknoteDataForCurrentPage();
            nextBanknoteData.forEach(data -> log.info(data.toString()));
        }
    }
}
