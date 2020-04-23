package com.moneycol.collections.server.infrastructure.api.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class CollectorDTO {
    private final String collectorId;
}
