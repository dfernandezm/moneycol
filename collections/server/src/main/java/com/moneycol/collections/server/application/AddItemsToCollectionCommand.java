package com.moneycol.collections.server.application;

import com.moneycol.collections.server.infrastructure.api.dto.CollectionItemDto;
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
    private List<CollectionItemDto> items;
    private String collectorId;
}
