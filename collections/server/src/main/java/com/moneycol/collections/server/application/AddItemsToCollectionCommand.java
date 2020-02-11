package com.moneycol.collections.server.application;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor(staticName = "of")
public class AddItemsToCollectionCommand {
    private final String collectionId;
    private final List<CollectionItemDTO> items;
}
