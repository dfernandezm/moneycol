package com.moneycol.collections.server.application;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateCollectionDTO {
    private String name;
    private String description;
    private String collectorId;
}
