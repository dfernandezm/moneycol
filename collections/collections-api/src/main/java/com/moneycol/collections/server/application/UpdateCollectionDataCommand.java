package com.moneycol.collections.server.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//TODO: commands should be validated for required fields as
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateCollectionDataCommand {
    private String collectionId;
    private String name;
    private String description;
    private String collectorId;
}
