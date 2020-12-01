package com.moneycol.collections.server.domain.events.core;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DeadEventListener {

    private List<DeadEvent> deadEvents;

    @Subscribe
    public void handleDeadEvent(DeadEvent deadEvent) {
        log.info("unhandled event [" + deadEvent.getEvent() + "]");
    }
}
