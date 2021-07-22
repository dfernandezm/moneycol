import com.moneycol.datacollector.colnect.SelenideColnectCrawler;
import com.moneycol.datacollector.colnect.collector.CrawlingProcessState;
import com.moneycol.datacollector.colnect.collector.GcsDataWriter;
import com.moneycol.datacollector.colnect.pages.CountrySeriesListing;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GcsDataWriterTest {

    /**
     * Requires GOOGLE_APPLICATION_CREDENTIALS
     *
     */
    @Test
    public void testReadDataFromGcs() {
        GcsDataWriter gcsDataWriter = new GcsDataWriter();
        String data = gcsDataWriter.readDataFromGcs( "state.json");
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
        SelenideColnectCrawler crawler = new SelenideColnectCrawler(new GcsDataWriter());
        String currentUrl = "url3";
        CountrySeriesListing countrySeriesListing1 = CountrySeriesListing.builder().url("url1").build();
        CountrySeriesListing countrySeriesListing2 = CountrySeriesListing.builder().url("url2").build();
        CountrySeriesListing countrySeriesListing3 = CountrySeriesListing.builder().url("url3").build();
        List<CountrySeriesListing> someListings = new ArrayList<CountrySeriesListing>() {{
            add(countrySeriesListing1);
            add(countrySeriesListing2);
            add(countrySeriesListing3);
        }};

        List<CountrySeriesListing> toVisit = crawler.skipUntil(someListings, currentUrl);
        assertThat(toVisit).hasSize(1);
        assertThat(toVisit).containsExactly(countrySeriesListing3);
    }

    @Test
    public void skipsCrawledSeriesMoreThanOneTest() {
        SelenideColnectCrawler crawler = new SelenideColnectCrawler(new GcsDataWriter());
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

        List<CountrySeriesListing> toVisit = crawler.skipUntil(someListings, currentUrl);
        assertThat(toVisit).hasSize(3);
        assertThat(toVisit).containsExactly(countrySeriesListing3, countrySeriesListing4, countrySeriesListing5);
    }

}

