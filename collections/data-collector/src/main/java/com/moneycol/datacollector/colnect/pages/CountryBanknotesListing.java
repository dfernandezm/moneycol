package com.moneycol.datacollector.colnect.pages;

import com.codeborne.selenide.SelenideElement;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

/**
 * This represents the Banknotes Listing page for a given country.
 * It contains a pager box with all page numbers as links and up to 10
 * banknotes in each page
 *
 */
@Slf4j
public class CountryBanknotesListing {

    private static final List<SelenideElement> banknotesList = $$("#plist_items > div");
    private final List<SelenideElement> pageLinks = $$("a.pager_page");
    private final SelenideElement pagerQuick = $("input.pager_quick");

    private final String countryName;
    private final Integer pageNumber;

    @Builder
    public CountryBanknotesListing(String countryName, Integer pageNumber) {
        this.countryName = countryName;
        this.pageNumber = (pageNumber == null) ? 1 : pageNumber;
    }

    public String getCountryName() {
        return countryName;
    }

    public List<BanknoteData> banknoteDataForCurrentPage() {
        log.info("Total banknotes in page {}", banknotesList.size());
        return banknotesList
                .parallelStream()
                .map(this::parseBanknoteData)
                .collect(Collectors.toList());
    }

    public boolean hasMorePages() {
        Optional<SelenideElement> maybeNextPage = nextPageElement(pageNumber + 1);
        return maybeNextPage.isPresent();
    }

    private Optional<SelenideElement> nextPageElement(int number) {
        return pageLinks
                .stream()
                .filter(pageLinkEl -> pagerLinkIsForPageNumber(number, pageLinkEl))
                .findFirst();
    }

    public CountryBanknotesListing visitNextPage() {
        int nextPageNumber = pageNumber + 1;
        log.info("Going to visit next page: {}", pageNumber);
        Optional<SelenideElement> pageLinkElement = nextPageElement(nextPageNumber);
        pageLinkElement.ifPresent(SelenideElement::click);
        return CountryBanknotesListing.builder()
                .countryName(countryName)
                .pageNumber(nextPageNumber)
                .build();
    }

    private boolean pagerLinkIsForPageNumber(int number, SelenideElement link) {
        return link.getText().equals(number + "");
    }

    private BanknoteData parseBanknoteData(SelenideElement banknoteDataListBlock) {

        String banknoteName = banknoteDataListBlock.find(By.className("item_header")).getText();
        SelenideElement dataBlock = banknoteDataListBlock.find(By.className("i_d"));

        List<SelenideElement> attributeNames =  dataBlock.findAll(By.cssSelector("dl > dt"));
        List<SelenideElement> attributeValues = dataBlock.findAll(By.cssSelector("dl > dd"));
        log.info("Banknote Name {}", banknoteName);
        Map<String, String> banknoteDataRaw = new HashMap<>();

        IntStream.range(0, attributeNames.size()).forEach(i -> {
            String name = attributeNames.get(i).getText();
            String value = attributeValues.get(i).getText();
            log.info("Banknote data -> name: {}, value: {}", name, value);
            banknoteDataRaw.put(name, value);
        });

        String year = banknoteDataRaw.get("Issued on:");

        return BanknoteData.builder()
                .name(banknoteName)
                .year(Integer.parseInt(year))
                .country(countryName)
                .build();
        }
}
