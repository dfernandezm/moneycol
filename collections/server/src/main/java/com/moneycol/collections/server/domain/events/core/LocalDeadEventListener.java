package com.moneycol.collections.server.domain.events.core;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LocalDeadEventListener implements DeadEventListener<DeadEvent>{

    private List<DeadEvent> deadEvents = new ArrayList<>();

    @Subscribe
    @Override
    public void handleDeadEvent(DeadEvent deadEvent) {
        log.info("Unhandled event [" + deadEvent.getEvent().toString() + "]");
        deadEvents.add(deadEvent);
        log.info("Count: {}", deadEventCount());
    }

    @Override
    public int deadEventCount() {
        return deadEvents.size();
    }
}
