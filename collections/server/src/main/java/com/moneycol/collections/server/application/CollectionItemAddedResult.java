package com.moneycol.collections.server.application;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionItemAddedResult {
    private   String collectionId;
    private   String name;
    private   String description;
    private   String collectorId;
    private List<CollectionItemDTO> items;
}
