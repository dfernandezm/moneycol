package com.moneycol.collections.server.application;

import com.moneycol.collections.server.infrastructure.api.dto.CollectionItemDTO;
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
