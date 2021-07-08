package com.moneycol.datacollector.colnect.pages;

import com.codeborne.selenide.SelenideElement;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

@Slf4j
public class CountrySeriesListing {

    private final String url;
    private final SelenideElement allBanknotesForCountryLink =
            $("div.navigation_box > div > strong:nth-child(3)");
    private final String countryName;

    @Builder
    public CountrySeriesListing(String url, String countryName) {
        this.url = url;
        this.countryName = countryName;
    }

    public void visit() {
        open(url);
    }

    public CountryBanknotesListing visitAllBanknotesListing() {
        // Countries like Antigua & barbuda have no link 'banknotesForCountryListingLink',
        // no Series / very low number of banknotes
        if (!allBanknotesForCountryLink.exists()) {
            log.info("Country {} has no link for banknotes listing -- all banknotes are in this page already",
                    countryName);
        } else {
            log.info("Visiting listing for country {}", countryName);
            allBanknotesForCountryLink.click();
        }

        return new CountryBanknotesListing(countryName, 1);
    }
}
