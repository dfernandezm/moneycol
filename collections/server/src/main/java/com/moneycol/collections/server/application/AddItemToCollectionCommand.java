package com.moneycol.collections.server.application;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class AddItemToCollectionCommand {
    private final String collectionId;
    private final CollectionItemDTO item;
}
