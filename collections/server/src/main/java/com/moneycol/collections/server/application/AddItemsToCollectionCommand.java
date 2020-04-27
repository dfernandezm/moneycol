package com.moneycol.collections.server.application;

import com.moneycol.collections.server.infrastructure.api.dto.CollectionItemDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddItemsToCollectionCommand {
    private String collectionId;
    private List<CollectionItemDTO> items;
    private String collectorId;
}
