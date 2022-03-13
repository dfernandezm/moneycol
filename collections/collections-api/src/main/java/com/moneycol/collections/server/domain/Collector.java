package com.moneycol.collections.server.domain;

import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
public class Collector {
    private CollectorId id;

    private Collector(CollectorId id) {
        this.id = id;
    }

    public static Collector withStringCollectorId(String collectorId) {
        CollectorId cid =CollectorId.of(collectorId);
        return Collector.of(cid);
    }

    public static Collector of(CollectorId collectorId) {
        return new Collector(collectorId);
    }

    public String id() {
        return id.id();
    }

    public static Collector withCollectorId(String collectorId) {
        return withStringCollectorId(collectorId);
    }
}
