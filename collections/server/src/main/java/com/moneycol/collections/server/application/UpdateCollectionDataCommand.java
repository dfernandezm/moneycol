package com.moneycol.collections.server.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateCollectionDataCommand {
    private String id;
    private String name;
    private String description;
    private String collectorId;
}
