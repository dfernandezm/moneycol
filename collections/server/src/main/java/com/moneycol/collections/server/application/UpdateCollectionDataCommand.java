package com.moneycol.collections.server.application;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class UpdateCollectionDataCommand {
    private String id;
    private String name;
    private String description;
    private String collectorId;
}
