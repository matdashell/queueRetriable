package com.queue.retriable.process;

import com.queue.retriable.dto.AnnotationResult;
import com.queue.retriable.exception.RetriableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static com.queue.retriable.variable.Variable.HEADER_ATEMPENT_ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AspectJProcess {

    private final RabbitTemplate rabbitTemplate;
    private final ReflectionProcess reflectionProcess;

    public void processRetriableException(JoinPoint joinPoint, RetriableException e) {
        AnnotationResult annotationResult = reflectionProcess.getAnnotationResult(joinPoint);
        if(notIsLastExecution(joinPoint, annotationResult)) {
            processDefaultAttempentMessage(joinPoint, annotationResult, e);
        } else {
            processMaxAttempentMessage(joinPoint, annotationResult, e);
        }
    }

    private void processDefaultAttempentMessage(JoinPoint joinPoint, AnnotationResult annotationResult, Throwable e) {
        Message message = reflectionProcess.getMainArg(joinPoint, Message.class);
        incrementHeader(message);
        log.error("Failed in process... sending to queue [{}] - attempent [{} of {}] - [{}] - with body [{}]",
                annotationResult.getOnAttempentsSendToQueue(),
                getCurrentAttempents(message),
                annotationResult.getMaxAttempents(),
                annotationResult.getMessageOnException(),
                new String(message.getBody()),
                e);
        rabbitTemplate.convertAndSend(annotationResult.getOnAttempentsSendToQueue(), message);
    }

    private void processMaxAttempentMessage(JoinPoint joinPoint, AnnotationResult annotationResult, Throwable e) {
        Message message = reflectionProcess.getMainArg(joinPoint, Message.class);
        log.error("Failed in process... sending to queue [{}] - attempent [{} of {}] - [{}] - with body [{}]",
                annotationResult.getOnMaxAttempentsSendToQueue(),
                getCurrentAttempents(message),
                annotationResult.getMaxAttempents(),
                annotationResult.getMessageOnMaxExecutions(),
                new String(message.getBody()),
                e);
        rabbitTemplate.convertAndSend(annotationResult.getOnMaxAttempentsSendToQueue(), message);
    }

    private boolean notIsLastExecution(JoinPoint joinPoint, AnnotationResult annotationResult) {
        Message message = reflectionProcess.getMainArg(joinPoint, Message.class);
        Integer headerAtempent = getCurrentAttempents(message);
        return headerAtempent == null || headerAtempent < annotationResult.getMaxAttempents();
    }

    private void incrementHeader(Message message) {
        Integer headerAtempent = getCurrentAttempents(message);
        Integer newValue = headerAtempent != null ? ++headerAtempent : 0;
        message.getMessageProperties().setHeader(HEADER_ATEMPENT_ID, newValue);
    }

    private Integer getCurrentAttempents(Message message) {
        return message.getMessageProperties().getHeader(HEADER_ATEMPENT_ID);
    }
}
