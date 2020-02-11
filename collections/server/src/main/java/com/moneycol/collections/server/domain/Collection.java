package com.moneycol.collections.server.domain;

import com.moneycol.collections.server.infrastructure.repository.CollectionItemNotFoundException;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;


@Getter
@Accessors(fluent = true)
public class Collection {
    private CollectionId id;
    private String name;
    private String description;
    private Collector collector;
    private List<CollectionItem> items = new ArrayList<>();

    private Collection() {}

    public static Collection withNameAndDescription(CollectionId collectionId,
                                                    String name,
                                                    String description,
                                                    Collector collector) {
        Collection c = new Collection();
        c.name = name;
        c.collector = collector;
        c.description = description;
        c.id = collectionId;
        return c;
    }

    public void addItem(CollectionItem item) {
        if(!items.contains(item)) {
            items.add(item);
        }
    }

    public void removeItem(CollectionItem item) {
        if (!items.contains(item)) {
            throw new CollectionItemNotFoundException("Item with id: " +
                    item.getItemId() + " can't be found in collection " + id.id());
        }
        items.remove(item);
    }

    public void addItems(List<CollectionItem> items) {
        items.forEach(this::addItem);
    }

    public String id() {
        return id.id();
    }
}
