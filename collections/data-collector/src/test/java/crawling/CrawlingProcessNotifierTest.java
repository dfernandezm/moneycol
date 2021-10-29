package crawling;

import com.moneycol.datacollector.colnect.CrawlerNotifier;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@MicronautTest
public class CrawlingProcessNotifierTest {

    @Inject
    private CrawlerNotifier crawlerNotifier;

    @Test
    public void testSendDone() {

//        CrawlingDoneResult crawlingDoneResult = CrawlingDoneResult.builder()
//                .doneMessage("done")
//                .build();

        crawlerNotifier.notifyDone();

        String a = "";
    }
}
