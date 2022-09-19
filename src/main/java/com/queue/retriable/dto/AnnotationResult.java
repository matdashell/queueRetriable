package com.queue.retriable.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnnotationResult {

    private final int maxAttempts;
    private final String messageOnException;
    private final String messageOnMaxExecutions;
    private final String onMaxAttemptsSendToQueue;
    private final String onAttemptsSendToQueue;

}
