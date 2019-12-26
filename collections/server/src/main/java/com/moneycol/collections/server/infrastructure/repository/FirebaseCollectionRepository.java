package com.moneycol.collections.server.infrastructure.repository;

import com.moneycol.collections.server.domain.Collection;
import com.moneycol.collections.server.domain.CollectionId;
import com.moneycol.collections.server.domain.CollectionRepository;
import com.moneycol.collections.server.domain.CollectorId;

import java.util.List;

public class FirebaseCollectionRepository implements CollectionRepository {
    @Override
    public Collection create(Collection collection) {
        return null;
    }

    @Override
    public Collection update(Collection collection) {
        return null;
    }

    @Override
    public void delete(CollectionId collectionId) {

    }

    @Override
    public Collection byId(CollectionId collectionId) {
        return null;
    }

    @Override
    public List<Collection> byCollector(CollectorId collectorId) {
        return null;
    }
}
