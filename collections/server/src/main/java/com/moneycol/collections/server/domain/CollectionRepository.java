package com.moneycol.collections.server.domain;

import java.util.List;

public interface CollectionRepository {
    Collection create(Collection collection);
    Collection update(Collection collection);
    void delete(CollectionId collectionId);
    Collection byId(CollectionId collectionId);
    List<Collection> byCollector(CollectorId collectorId);
}
