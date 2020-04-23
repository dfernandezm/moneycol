package com.moneycol.collections.server.application;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor //Jackson requires a non argument constructor for JSON deserialization
@Builder
public class CollectionUpdatedResult {
    private   String collectionId;
    private   String name;
    private   String description;
}
