package com.moneycol.collections.server.domain;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.Accessors;

@EqualsAndHashCode
@Value
@Accessors(fluent = true)
public class CollectionItem {
    private String itemId;
}
