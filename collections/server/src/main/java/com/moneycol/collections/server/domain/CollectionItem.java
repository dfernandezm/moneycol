package com.moneycol.collections.server.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
@Getter
@Setter
public class CollectionItem {
    private String itemId;

    private CollectionItem() {}

    public static CollectionItem of(String itemId) {
        CollectionItem item = new CollectionItem();
        item.itemId = itemId;
        return item;
    }
}
