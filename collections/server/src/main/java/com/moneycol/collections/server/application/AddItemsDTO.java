package com.moneycol.collections.server.application;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddItemsDTO {
    private List<CollectionItemDTO> items;
}
