import com.moneycol.datacollector.colnect.SelenideColnectCrawler;
import com.moneycol.datacollector.colnect.collector.CrawlingProcessState;
import com.moneycol.datacollector.colnect.collector.GcsDataWriter;
import com.moneycol.datacollector.colnect.pages.CountrySeriesListing;
import com.moneycol.datacollector.crawling.CrawlerNotifier;
import com.moneycol.datacollector.crawling.CrawlingDoneResult;
import com.moneycol.datacollector.crawling.CrawlingProcessReporter;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GcsDataWriterTest {

    /**
     * Requires GOOGLE_APPLICATION_CREDENTIALS / connectivity to GCP.
     * For proper e2e need to use testcontainers with emulator/Hoverfly
     */
    @Test
    public void testReadDataFromGcs() {
        GcsDataWriter gcsDataWriter = new GcsDataWriter();
        String data = gcsDataWriter.readDataFromGcs("state.json");
        assertThat(data).contains("pageNumber");
        assertThat(data).contains("currentUrl");
    }

    @Test
    public void testReadStateFromJsonToObject() {
        GcsDataWriter gcsDataWriter = new GcsDataWriter();
        CrawlingProcessState state = gcsDataWriter.findState();
        assertThat(state.getCurrentUrl()).isNotNull();
        assertThat(state.getPageNumber()).isNotNull();
    }

    @Test
    public void skipsCrawledSeriesTest() {
        SelenideColnectCrawler crawler = new SelenideColnectCrawler(new GcsDataWriter(), new CrawlerNotifier(new FakePubSubNotifier()));
        String currentUrl = "url3";
        CountrySeriesListing countrySeriesListing1 = CountrySeriesListing.builder().url("url1").build();
        CountrySeriesListing countrySeriesListing2 = CountrySeriesListing.builder().url("url2").build();
        CountrySeriesListing countrySeriesListing3 = CountrySeriesListing.builder().url("url3").build();
        List<CountrySeriesListing> someListings = new ArrayList<CountrySeriesListing>() {{
            add(countrySeriesListing1);
            add(countrySeriesListing2);
            add(countrySeriesListing3);
        }};

        List<CountrySeriesListing> toVisit = crawler.skipUntilUrl(someListings, currentUrl);
        assertThat(toVisit).hasSize(1);
        assertThat(toVisit).containsExactly(countrySeriesListing3);
    }

    @Test
    public void dateIsFormattedUntilDay() {
        String testDate = "2021-10-31T10:15:30";
        String expectedDateString = "31-10-2021";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String date = LocalDateTime.parse(testDate).format(formatter);
        assertThat(date).isEqualTo(expectedDateString);
    }

    @Test
    public void skipsCrawledSeriesMoreThanOneTest() {
        SelenideColnectCrawler crawler = new SelenideColnectCrawler(new GcsDataWriter(), new CrawlerNotifier(new FakePubSubNotifier()));
        String currentUrl = "url3";
        CountrySeriesListing countrySeriesListing1 = CountrySeriesListing.builder().url("url1").build();
        CountrySeriesListing countrySeriesListing2 = CountrySeriesListing.builder().url("url2").build();
        CountrySeriesListing countrySeriesListing3 = CountrySeriesListing.builder().url("url3").build();
        CountrySeriesListing countrySeriesListing4 = CountrySeriesListing.builder().url("url4").build();
        CountrySeriesListing countrySeriesListing5 = CountrySeriesListing.builder().url("url5").build();

        List<CountrySeriesListing> someListings = new ArrayList<CountrySeriesListing>() {{
            add(countrySeriesListing1);
            add(countrySeriesListing2);
            add(countrySeriesListing3);
            add(countrySeriesListing4);
            add(countrySeriesListing5);
        }};

        List<CountrySeriesListing> toVisit = crawler.skipUntilUrl(someListings, currentUrl);
        assertThat(toVisit).hasSize(3);
        assertThat(toVisit).containsExactly(countrySeriesListing3, countrySeriesListing4, countrySeriesListing5);
    }

    private static class FakePubSubNotifier implements CrawlingProcessReporter {

        @Override
        public void sendCrawlingDone(CrawlingDoneResult crawlingDoneResult) {
            // nothing to do
        }
    }
}

