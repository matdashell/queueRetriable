package com.queue.retriable.process;

import com.queue.retriable.dto.AnnotationResult;
import com.queue.retriable.exception.RetriableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static com.queue.retriable.variable.Variable.HEADER_ATTEMPT_ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AspectJProcess {

    private final RabbitTemplate rabbitTemplate;
    private final ReflectionProcess reflectionProcess;

    public void processRetriableException(JoinPoint joinPoint, RetriableException e) {
        reflectionProcess.verifyExistenceOfInArgs(joinPoint, Message.class);
        AnnotationResult annotationResult = reflectionProcess.getAnnotationResult(joinPoint);
        if(notIsLastExecution(joinPoint, annotationResult)) {
            processDefaultAttemptMessage(joinPoint, annotationResult, e);
        } else {
            processMaxAttemptMessage(joinPoint, annotationResult, e);
        }
    }

    private void processDefaultAttemptMessage(JoinPoint joinPoint, AnnotationResult annotationResult, Throwable e) {
        Message message = reflectionProcess.getMainArg(joinPoint, Message.class);
        incrementHeader(message);
        log.error("Failed in process... sending to queue [{}] - attempent [{} of {}] - [{}] - with body [{}]",
                annotationResult.getOnAttemptsSendToQueue(),
                getCurrentAttempts(message),
                annotationResult.getMaxAttempts(),
                annotationResult.getMessageOnException(),
                new String(message.getBody()),
                e);
        rabbitTemplate.convertAndSend(annotationResult.getOnAttemptsSendToQueue(), message);
    }

    private void processMaxAttemptMessage(JoinPoint joinPoint, AnnotationResult annotationResult, Throwable e) {
        Message message = reflectionProcess.getMainArg(joinPoint, Message.class);
        log.error("Failed in process... sending to queue [{}] - attempent [{} of {}] - [{}] - with body [{}]",
                annotationResult.getOnMaxAttemptsSendToQueue(),
                getCurrentAttempts(message),
                annotationResult.getMaxAttempts(),
                annotationResult.getMessageOnMaxExecutions(),
                new String(message.getBody()),
                e);
        rabbitTemplate.convertAndSend(annotationResult.getOnMaxAttemptsSendToQueue(), message);
    }

    private boolean notIsLastExecution(JoinPoint joinPoint, AnnotationResult annotationResult) {
        Message message = reflectionProcess.getMainArg(joinPoint, Message.class);
        Integer headerAtempent = getCurrentAttempts(message);
        return headerAtempent == null || headerAtempent < annotationResult.getMaxAttempts();
    }

    private void incrementHeader(Message message) {
        Integer headerAtempent = getCurrentAttempts(message);
        Integer newValue = headerAtempent != null ? ++headerAtempent : 0;
        message.getMessageProperties().setHeader(HEADER_ATTEMPT_ID, newValue);
    }

    private Integer getCurrentAttempts(Message message) {
        return message.getMessageProperties().getHeader(HEADER_ATTEMPT_ID);
    }
}
