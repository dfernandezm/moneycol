package com.moneycol.collections.server.domain;

import lombok.Value;
import lombok.experimental.Accessors;

@Value(staticConstructor = "of")
@Accessors(fluent = true)
public class Collector {
    private CollectorId id;

    public static Collector withCollectorId(String collectorId) {
        CollectorId cid =CollectorId.of(collectorId);
        return Collector.of(cid);
    }

    public String id() {
        return id.id();
    }
}
