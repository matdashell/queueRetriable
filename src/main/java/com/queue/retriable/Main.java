package com.queue.retriable;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@SpringBootApplication
@RequiredArgsConstructor
public class Main {

    private final ServiceTest serviceTest;

    @PostConstruct
    void teste() {
        serviceTest.enviar(new Message(new byte[]{}));
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
