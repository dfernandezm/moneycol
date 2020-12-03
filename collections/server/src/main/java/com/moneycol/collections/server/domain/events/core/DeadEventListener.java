package com.moneycol.collections.server.domain.events.core;


public interface DeadEventListener<T> {
    void handleDeadEvent(T deadEvent);
    int deadEventCount();
}
