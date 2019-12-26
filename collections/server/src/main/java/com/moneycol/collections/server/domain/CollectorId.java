package com.moneycol.collections.server.domain;

import com.moneycol.collections.server.domain.base.Id;
import lombok.Value;

@Value
public class CollectorId extends Id<String> {

    private CollectorId(String collectorId) {
       super(collectorId);
    }

    public static CollectorId of(String collectorId) {
        return new CollectorId(collectorId);
    }
}
