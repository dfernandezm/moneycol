package com.moneycol.datacollector.colnect.pages;

import com.codeborne.selenide.SelenideElement;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

@Slf4j
public class ColnectLandingPage {

    private final static String BANKNOTES_BY_COUNTRY_ENG_URL = "https://colnect.com/en/banknotes/countries";
    private static final ExecutorService executorService = Executors.newFixedThreadPool(5);

    private final List<SelenideElement> countriesLinks = $$("#pl_350 > a");
    private final String url;

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

    public void visit() {
        open(url);
    }
}
