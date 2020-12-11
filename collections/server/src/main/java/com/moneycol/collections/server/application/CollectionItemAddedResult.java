package com.moneycol.collections.server.application;

import com.moneycol.collections.server.infrastructure.api.dto.CollectionItemDto;
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
    private List<CollectionItemDto> items;
}
