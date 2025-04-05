package com.martindev.redissinkconsumergroup.service;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Data
public class MessageProcessor {
    private static final String CONSUMER_LIST_KEY = "consumer:ids";
    private List<ConsumerWorker> consumerGroup = new ArrayList<>();
    private final int numberOfThreads;
    private final StringRedisTemplate redisTemplate;
    private final BeanFactory beanFactory;
    private final String consumerPrefix;

    public MessageProcessor(@Value("${consumer.threadPoolSize}")final int numberOfThreads,
                            final StringRedisTemplate redisTemplate,
                            final BeanFactory beanFactory,
                            final @Value("${consumer.prefix}") String consumerPrefix) {
        this.numberOfThreads = numberOfThreads;
        this.redisTemplate = redisTemplate;
        this.beanFactory = beanFactory;
        this.consumerPrefix = consumerPrefix;
    }

    @PostConstruct
    void init(){
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        for (int i = 0; i < numberOfThreads; i++) {
            final String consumerId = consumerPrefix + i;
            redisTemplate.opsForList().rightPush(CONSUMER_LIST_KEY, consumerId);
            final ConsumerWorker consumer = beanFactory.getBean(ConsumerWorker.class);
            consumer.setConsumerId(consumerId);
            this.consumerGroup.add(consumer);
            executorService.submit(consumer);
        }
    }
}
