package com.queue.retriable;

import com.queue.retriable.exception.RetriableException;
import com.queue.retriable.process.RetriableQueueMethodProcess;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AspectJ {

    private final RetriableQueueMethodProcess retriableQueueMethodProcess;

    @Around(value = "@annotation(com.queue.retriable.annotation.RetriableQueueMethod)")
    public void retriableAspect(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            joinPoint.proceed();
        } catch (RetriableException e) {
            retriableQueueMethodProcess.process(joinPoint, e);
        }
    }
}
