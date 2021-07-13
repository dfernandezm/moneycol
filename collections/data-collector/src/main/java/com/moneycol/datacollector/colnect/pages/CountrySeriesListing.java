package com.moneycol.datacollector.colnect.pages;

import com.codeborne.selenide.SelenideElement;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

/**
 * This is the listing page with all series of banknotes for a given country.
 * It can be accessed by clicking in each country link in the countries list.
 *
 * Example: https://colnect.com/en/banknotes/series/country/2-Albania
 *
 * The objective is using the link at the bottom of the page
 * 'Showing X Series , X Banknotes' to obtain the full banknotes list.
 *
 */
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

    public String getCountryName() {
        return this.countryName;
    }

    public String getUrl() {
        return url;
    }

    /**
     * Open this this page url
     *
     */
    public void visit() {
        open(url);
    }

    /**
     * Visit the link for the full list of banknotes.
     *
     * Countries like Antigua & Barbuda don't have this link as there's
     * no Series / very low number of banknotes.
     *
     * @return the first listing page or this page if link does not exist
     */
    public CountryBanknotesListing visitAllBanknotesListing() {
        String enclosingLink = "";
        if (allBanknotesForCountryLink.exists()) {
            enclosingLink = allBanknotesForCountryLink.attr("href");
            log.info("Visiting listing for country {}", countryName);
            allBanknotesForCountryLink.click();
        } else {
            log.info("Country {} has no link for banknotes listing -- all banknotes are in this page already",
                    countryName);
            enclosingLink = url;
        }

        return new CountryBanknotesListing(countryName, 1, enclosingLink);
    }
}
