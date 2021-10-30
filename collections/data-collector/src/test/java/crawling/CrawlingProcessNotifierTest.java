package crawling;

import com.moneycol.datacollector.crawling.CrawlerNotifier;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

// This annotation changed since v2.1.3 to be the Junit5 extension
@MicronautTest
public class CrawlingProcessNotifierTest {

    @Inject
    private CrawlerNotifier crawlerNotifier;

    // IMPORTANT: This sends a real PubSub message for now
    @Test
    public void testSendDone() {
        crawlerNotifier.notifyDone();
    }
}
