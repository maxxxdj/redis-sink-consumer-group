package com.martindev.redissinkconsumergroup.service;

import jakarta.annotation.PreDestroy;
import lombok.Data;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
@Component
@Scope("prototype")
public class ConsumerGroup {
    private String name;
    private final AtomicBoolean isActive = new AtomicBoolean(false);
    ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
    private final List<Consumer> consumers = new ArrayList<>();
    private final List<Future<?>> runningTasks = new ArrayList<>();
    private final int numberOfThreads;
    private final String consumerPrefix;
    private final BeanFactory beanFactory;
    private final StringRedisTemplate redisTemplate;
    private static final String CONSUMER_LIST_KEY = "consumer:ids";
    public ConsumerGroup(@Value("${consumer.threadPoolSize}") int numberOfThreads,
                         @Value("${consumer.prefix}")
     String consumerPrefix, BeanFactory beanFactory, StringRedisTemplate redisTemplate) {
        this.numberOfThreads = numberOfThreads;
        this.consumerPrefix = consumerPrefix;
        this.beanFactory = beanFactory;
        this.redisTemplate = redisTemplate;
    }

    public void initializeConsumers() {
        for (int i = 0; i < numberOfThreads; i++) {
            String consumerId = consumerPrefix + i;
            redisTemplate.opsForList().rightPush(CONSUMER_LIST_KEY, consumerId);
            final Consumer consumer = beanFactory.getBean(Consumer.class);
            consumer.setConsumerId(consumerId);
            addConsumer(consumer);
        }
    }

    public synchronized void start() {
        if (isActive.compareAndSet(false, true)) {
            executorService = Executors.newFixedThreadPool(consumers.size());
            for (Consumer consumer : consumers) {
                final Future<?> future = executorService.submit(consumer);
                runningTasks.add(future);
            }
        }
    }

    public synchronized void pause() {
        if (isActive.compareAndSet(true, false)) {
            for (Future<?> task : runningTasks) {
                task.cancel(true);
            }
            runningTasks.clear();
            if (executorService != null) {
                executorService.shutdownNow();
            }
        }
    }

    public void setName(String name) {
        this.name = name;
    }
    public void addConsumer(Consumer consumer){
        this.consumers.add(consumer);
    }
    @PreDestroy
    public synchronized void shutdown() {
        pause();
    }
}