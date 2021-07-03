package com.moneycol.datacollector.colnect.pages;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

@Slf4j
public class CountrySeriesListing {

    private String url;
    private String banknotesForCountryListingLink = "div.navigation_box > div > strong:nth-child(3)";
    private String countryName;

    public CountrySeriesListing(String url, String countryName) {
        this.url = url;
        this.countryName = countryName;
    }

    public void visit() {
        open(url);
    }

    public CountryBanknotesListing visitAllBanknotesListing() {
        log.info("Visiting listing for country {}", countryName);
        $(By.cssSelector(banknotesForCountryListingLink)).click();
        return new CountryBanknotesListing(countryName, 1);
    }
}
