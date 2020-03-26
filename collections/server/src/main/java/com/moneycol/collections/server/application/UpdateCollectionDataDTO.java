package com.moneycol.collections.server.application;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCollectionDataDTO {
    private String name;
    private String description;
    private String collectorId; //TODO: cannot be here, populated by auth
}
