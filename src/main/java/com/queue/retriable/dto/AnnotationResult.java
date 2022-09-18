package com.queue.retriable.dto;

import lombok.Getter;

@Getter
public class AnnotationResult {

    private final int maxAttempents;
    private final String messageOnException;
    private final String messageOnMaxExecutions;
    private final String onMaxAttempentsSendToQueue;
    private final String onAttempentsSendToQueue;

    public AnnotationResult(
            int maxAttempents,
            String messageOnException,
            String messageOnMaxExecutions,
            String onMaxAttempentsSendToQueue,
            String onAttempentsSendToQueue) {

        this.maxAttempents = maxAttempents;
        this.messageOnException = messageOnException;
        this.messageOnMaxExecutions = messageOnMaxExecutions;
        this.onMaxAttempentsSendToQueue = onMaxAttempentsSendToQueue;
        this.onAttempentsSendToQueue = onAttempentsSendToQueue;
    }
}
