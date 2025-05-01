package com.martindev.redissinkconsumergroup.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class MetricsReporter {

    private final AtomicLong messageCounter = new AtomicLong(0);
    private final MessageProcessor messageProcessor;


    public MetricsReporter(MessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
    }

    public void incrementCounter() {
        messageCounter.incrementAndGet();
    }

    @Scheduled(fixedRate = 3000)
    public void reportMessagesPerSecond() {
        final List<Consumer> group = messageProcessor.getConsumerGroup().getConsumers();
        group.forEach(el ->
                System.out.printf("Consuemr: %s, consuming: %b, processed msgs: %d%n", el.getConsumerId(), el.isConsuming(), el.getCounter().get()));
        long totalCount = group.stream()
                .map(Consumer::getCounter)
                .mapToLong(el -> el.getAndSet(0))
                .sum();
        System.out.printf("Total %d messages processed (3seconds) and per second: %d%n" , totalCount , Math.round(totalCount / 3.0f));
    }
}
