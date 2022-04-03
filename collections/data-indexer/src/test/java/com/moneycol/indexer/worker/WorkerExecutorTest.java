package com.moneycol.indexer.worker;

import com.moneycol.indexer.tracker.FanOutTracker;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Disabled("Disabled as the injection requires firestore access")
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WorkerExecutorTest implements TestPropertyProvider {

    @Inject
    private FanOutTracker fanOutTracker;

    @Disabled("This test connects directly to pubsub")
    @Test
    public void publishIntermediateResultTest() {
        BanknoteData banknoteData = new BanknoteData();
        banknoteData.setCatalogCode("Wor:7");
        banknoteData.setCountry("Abkazia");
        List<BanknoteData> list = new ArrayList<>();
        list.add(banknoteData);

        BanknotesDataSet banknotesDataSet = BanknotesDataSet.builder()
                .banknotes(list)
                .country("Abkazia")
                .filename("p1.json")
                .pageNumber(1)
                .language("en")
                .build();

        fanOutTracker.publishIntermediateResult(banknotesDataSet);
    }

    @Nonnull
    @Override
    public Map<String, String> getProperties() {
        return Map.of(
                "fanout.pub-sub.sink-topic-name","dev.moneycol.indexer.sink");
    }
}
