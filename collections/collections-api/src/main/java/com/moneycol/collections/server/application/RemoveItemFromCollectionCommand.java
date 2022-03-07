package com.moneycol.collections.server.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RemoveItemFromCollectionCommand {
    private String collectionId;
    private String itemId;
    private String collectorId;
}
