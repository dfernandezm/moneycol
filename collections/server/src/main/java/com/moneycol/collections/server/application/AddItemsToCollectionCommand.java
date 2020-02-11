package com.moneycol.collections.server.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor(staticName = "of")
@Builder
public class AddItemsToCollectionCommand {
    private final String collectionId;
    private final List<CollectionItemDTO> items;
}
