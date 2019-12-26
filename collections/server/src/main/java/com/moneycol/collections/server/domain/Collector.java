package com.moneycol.collections.server.domain;

import lombok.Value;
import lombok.experimental.Accessors;

@Value(staticConstructor = "of")
@Accessors(fluent = true)
public class Collector {
    private CollectorId id;
}
