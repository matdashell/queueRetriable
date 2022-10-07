package com.queue.retriable;

import com.queue.retriable.annotation.RetriableQueueMethod;
import com.queue.retriable.exception.RetriableException;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Service;

@Service
public class ServiceTest {
    @RetriableQueueMethod(onAttemptsSendToQueue = "a", onMaxAttemptsSendToQueue = "b")
    public void enviar(Message message) {
        throw new RetriableException("dasd");
    }
}
