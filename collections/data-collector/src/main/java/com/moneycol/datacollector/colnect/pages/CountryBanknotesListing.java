package com.moneycol.datacollector.colnect.pages;

import com.codeborne.selenide.SelenideElement;
import com.google.common.base.Stopwatch;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

/**
 * This represents the Banknotes Listing page for a given country.
 * It contains a pager box with all page numbers as links and up to 10
 * banknotes in each page
 */
@Slf4j
public class CountryBanknotesListing {

    private static final List<SelenideElement> banknotesList = $$("#plist_items > div");
    private final List<SelenideElement> pageLinks = $$("a.pager_page");

    @Getter
    private final String countryName;

    @Getter
    private final Integer pageNumber;

    private final String enclosingLink;

    @Builder
    public CountryBanknotesListing(String countryName, Integer pageNumber, String enclosingLink) {
        this.countryName = countryName;
        this.pageNumber = (pageNumber == null) ? 1 : pageNumber;
        this.enclosingLink = enclosingLink;
    }

    public String getUrl() {
        return enclosingLink;
    }

    public void visit() {
        open(enclosingLink);
    }

    public List<BanknoteData> banknoteDataForCurrentPage() {
        log.info("Total banknotes in page {}", banknotesList.size());
        return banknotesList
                .stream()
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
        log.info("Going to visit page {} for country {}", nextPageNumber, countryName);
        Optional<SelenideElement> pageLinkElement = nextPageElement(nextPageNumber);

        String enclosingLink = "";
        if (pageLinkElement.isPresent()) {
            enclosingLink = pageLinkElement.get().attr("href");
        }
        pageLinkElement.ifPresent(SelenideElement::click);

        return CountryBanknotesListing.builder()
                .countryName(countryName)
                .enclosingLink(enclosingLink)
                .pageNumber(nextPageNumber)
                .build();
    }

    private boolean pagerLinkIsForPageNumber(int number, SelenideElement link) {
        return link.getText().equals(number + "");
    }

    private BanknoteData parseBanknoteData(SelenideElement banknoteDataListBlock) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        String banknoteName = banknoteDataListBlock.find(By.className("item_header")).getText();
        SelenideElement dataBlock = banknoteDataListBlock.find(By.className("i_d"));

        List<SelenideElement> attributeNames = dataBlock.findAll(By.cssSelector("dl > dt"));
        List<SelenideElement> attributeValues = dataBlock.findAll(By.cssSelector("dl > dd"));
        log.info("Banknote Name {} process - took so far {} ms", banknoteName, stopwatch.elapsed().toMillis());
        Map<String, String> banknoteDataRaw = new HashMap<>();

        IntStream.range(0, attributeNames.size()).forEach(i -> {
            String name = attributeNames.get(i).getText();
            String value = attributeValues.get(i).getText();
            banknoteDataRaw.put(name, value);
        });

        // May contain dates like 1945-05-01, so parseInt is not good idea
        String series = banknoteDataRaw.get("Series:");
        String year = banknoteDataRaw.get("Issued on:");
        String catalogCode = banknoteDataRaw.get("Catalog codes:");
        String description = banknoteDataRaw.get("Description:");
        String score = banknoteDataRaw.get("Score:");
        String composition = banknoteDataRaw.get("Composition:");
        String size = banknoteDataRaw.get("Size:");
        String distribution = banknoteDataRaw.get("Distribution:");
        String themes = banknoteDataRaw.get("Themes:");
        String faceValue = banknoteDataRaw.get("Face Value:");
        faceValue = faceValue != null ? faceValue : banknoteDataRaw.get("Face value:");
        boolean hasVariants = banknoteDataRaw.get("Variants:") != null;

        log.info("Banknote Name {} process - took so far {} ms", banknoteName, stopwatch.elapsed().toMillis());
        return BanknoteData.builder()
                .catalogCode(catalogCode)
                .country(countryName)
                .name(banknoteName)
                .year(year)
                .composition(composition)
                .faceValue(faceValue)
                .size(size)
                .score(score)
                .description(description)
                .distribution(distribution)
                .themes(themes)
                .hasVariants(hasVariants)
                .originalLink(enclosingLink)
                .series(series)
                .build();
        }
}
