package com.queue.retriable.annotation;

import org.springframework.lang.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RetriableQueueMethod {

    @NonNull
    int maxAttempents();
    String messageOnException() default "";
    String messageOnMaxExecutions() default "";
    @NonNull
    String onMaxAttempentsSendToQueue();
    @NonNull
    String onAttempentsSendToQueue();
}
