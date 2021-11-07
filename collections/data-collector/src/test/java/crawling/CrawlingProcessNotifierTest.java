package crawling;

import com.moneycol.datacollector.crawling.CrawlerNotifier;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.specto.hoverfly.junit5.HoverflyExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

// The Micronaut annotation changed since v2.1.3 to be the Junit5 extension
@ExtendWith(HoverflyExtension.class)
@MicronautTest
public class CrawlingProcessNotifierTest {

    @Inject
    private CrawlerNotifier crawlerNotifier;

    // IMPORTANT: This sends a real PubSub message for now
    @Property(name = "crawling.done-topic-name", value = "dev.crawler.test")
    @Test
    public void testSendDone() {
        crawlerNotifier.notifyDone("colnect/01-11-2021");
    }
}
