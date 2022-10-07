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
public class RetriableQueueMethodProcess {

    private final RabbitTemplate rabbitTemplate;
    private final ReflectionProcess reflectionProcess;

    public void process(JoinPoint joinPoint, RetriableException error) {
        reflectionProcess.verifyExistenceOfInArgs(joinPoint, Message.class);
        AnnotationResult annotation = reflectionProcess.getAnnotationResult(joinPoint);
        Message message = reflectionProcess.getMainArg(joinPoint, Message.class);
        incrementHeader(message);
        processAttemptMessage(annotation, error, message);
    }

    private void processAttemptMessage(AnnotationResult annotationResult, Throwable error, Message message) {

        boolean isLastExecution = isLastExecution(message, annotationResult);

        String currentQueue = isLastExecution
                ? annotationResult.getOnMaxAttemptsSendToQueue()
                : annotationResult.getOnAttemptsSendToQueue();

        String logMessage = isLastExecution
                ? annotationResult.getMessageOnMaxExecutions()
                : annotationResult.getMessageOnException();

        String body = new String(message.getBody());

        if(isFirstExecution(message)) {
            log.error("Failed in process... sending to queue [{}] - [{}] - with body [{}]",
                    currentQueue,
                    logMessage,
                    body,
                    error);
        } else {
            log.error("Failed in process... sending to queue [{}] - attempt [{} of {}] - [{}] - with body [{}]",
                    currentQueue,
                    getCurrentAttempts(message),
                    annotationResult.getMaxAttempts(),
                    logMessage,
                    body,
                    error);
        }
        rabbitTemplate.convertAndSend(currentQueue, message);
    }

    private boolean isLastExecution(Message message, AnnotationResult annotationResult) {
        Integer headerAtempent = getCurrentAttempts(message);
        return headerAtempent == annotationResult.getMaxAttempts();
    }

    private boolean isFirstExecution(Message message) {
        return getCurrentAttempts(message) == 0;
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
