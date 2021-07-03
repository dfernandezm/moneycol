package com.moneycol.datacollector.colnect.pages;

import com.codeborne.selenide.SelenideElement;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

@Slf4j
public class CountryBanknotesListing {

    private static final String banknotesListSelector = "#plist_items > div";
    private static final String banknoteNameLinkSelector = "h2.item-header a";

    private String countryName;
    private Integer pageNumber;

    public CountryBanknotesListing(String countryName, Integer pageNumber) {
        this.countryName = countryName;
        this.pageNumber = (pageNumber == null) ? 1 : pageNumber;
    }

    public List<BanknoteData> banknoteDataForCurrentPage() {
        List<BanknoteData> banknoteData =
                $$(banknotesListSelector)
                        .stream()
                        .map(this::banknoteDataFor)
                        .collect(Collectors.toList());
        return banknoteData;
    }

    public boolean hasMorePages() {
       return $("a.pager_control").exists();
    }

    public CountryBanknotesListing visitNextPage() {
        $("a.pager_control").click();
        return new CountryBanknotesListing(countryName, pageNumber + 1);
    }

    private BanknoteData banknoteDataFor(SelenideElement banknoteElement) {
        String banknoteName = banknoteElement.find(By.className("item_header")).getText();
        SelenideElement dataBlock = banknoteElement.find(By.className("i_d"));

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
