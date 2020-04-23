package com.moneycol.collections.server.infrastructure.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CollectionDTO {
    private String id;
    private String name;
    private String description;
    private List<CollectionItemDTO> items;
}
