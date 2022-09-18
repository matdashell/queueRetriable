package com.queue.retriable.exception;

public class RetriableException extends RuntimeException {
    public RetriableException(String message) {
        super(message);
    }
}
