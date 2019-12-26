package com.moneycol.collections.server.application;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CollectionCreatedResult {
    String collectionId;
    String name;
    String description;
    String collectorId;
}
