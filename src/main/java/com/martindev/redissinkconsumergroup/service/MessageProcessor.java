package com.martindev.redissinkconsumergroup.service;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Data
@Scope("prototype")
public class MessageProcessor {
    private static final String CONSUMER_LIST_KEY = "consumer:ids";
    private final ConsumerGroup consumerGroup;

    public MessageProcessor(final ConsumerGroup consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    @PostConstruct
    void init() {
        consumerGroup.setName("redis-consumer-group");
        consumerGroup.initializeConsumers();
        consumerGroup.start();
    }
}
