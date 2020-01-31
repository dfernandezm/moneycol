package com.moneycol.collections.server.application;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor //Jackson requires a non argument constructor for JSON deserialization
public class CollectionCreatedResult {
    private   String collectionId;
    private   String name;
    private   String description;
    private   String collectorId;
}
