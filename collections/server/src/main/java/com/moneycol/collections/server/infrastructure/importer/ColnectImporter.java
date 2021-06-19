package com.moneycol.collections.server.infrastructure.importer;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;

@Slf4j
public class ColnectImporter {

    private static final String BANKNOTES_COLLECTION_SELECTOR =
            "#collector_inventory > ul:nth-child(10) > li:nth-child(1) > a:nth-child(1)";

    public static void login() throws InterruptedException {

        Configuration.timeout = 8000;

        String loginButtonSelector = "#login > a";
        String usernameName = "signin[username]";
        String passwordName = "signin[password]";
        String loginButton = "#signin_btn";

        Selenide.open("https://colnect.com");
        $(loginButtonSelector).click();

        // username and password here!!!
        $(By.name(usernameName)).val("");
        $(By.name(passwordName)).val("");

        $(loginButton).submit();

        $("#collector_profile").should(exist);

        // Click my lists
        $("#collector_profile > a").click();
        Thread.sleep(1000);
        //$("#collector_profile > ul > li:nth-child(2) > a").click();

        $("#collector_inventory").should(exist);

        // Go to banknotes collection
        $(BANKNOTES_COLLECTION_SELECTOR).shouldBe(exist).click();
        $("#title_countries").should(exist);

        SelenideElement firstCountryElement = $("#pl_350 > a:nth-child(1)");
        firstCountryElement.shouldHave(text("Af")).click();
        Thread.sleep(1000);

        // The X for the country filter
        String countryFilterRemove = "div.filter_one._flt-country > a.f_rm.vertical-middle";
        $(countryFilterRemove).click();
        Thread.sleep(500);

        String banknotesListSelector = "#plist_items";

        SelenideElement firstBanknote = $(banknotesListSelector).should(exist).$$("div.pl-it").first();
        log.info("First banknote: {}", firstBanknote);

        //log.info("Links size {}", links);

        // list of countries, go to first country and in
        // the filter click on X
        // then navigate page by page

        //#collector_profile > ul > li:nth-child(2) > a
        //$("#collector_profile a").shouldHave(text("Odisea"));
    }

    public static void main(String[] args) throws InterruptedException {
        login();
    }
}
