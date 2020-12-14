package com.moneycol.collections.server.domain;

import com.moneycol.collections.server.domain.base.Id;
import lombok.EqualsAndHashCode;
import lombok.Value;


@Value
@EqualsAndHashCode(callSuper = true)
public class CollectionId extends Id<String> {

    private CollectionId(String collectionId) {
       super(collectionId);
    }

    public static CollectionId of(String collectionId) {
        return new CollectionId(collectionId);
    }
}
