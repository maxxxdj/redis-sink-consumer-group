package com.martindev.redissinkconsumergroup.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Scope("prototype")
public class ConsumerWorker implements Runnable {
    @Setter
    @Getter
    private String consumerId;
    private final StringRedisTemplate redisTemplate;
    private final MetricsReporter metricsReporter;
    private final Random random = new Random();
    @Getter
    private boolean isConsuming;
    @Getter
    private final AtomicLong counter = new AtomicLong(0);
    private Timer timer;

    public ConsumerWorker(StringRedisTemplate redisTemplate,
                          @Lazy MetricsReporter metricsReporter) {
        this.redisTemplate = redisTemplate;
        this.metricsReporter = metricsReporter;
    }

    @PostConstruct
    void init() {
        this.isConsuming = false;
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                isConsuming = counter.get() != 0;
            }
        }, 0, 3000);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                final String message = RedisMessageConsumer.getMessageQueue().take();
                isConsuming = true;
                final String modifiedJson = message.substring(0, message.length() - 1)
                        + ", \"random_field\": " + random.nextInt()
                        + ", \"consumer_id\": \"" + consumerId + "\" }";
                sendToStream(modifiedJson);
                metricsReporter.incrementCounter();
                this.counter.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("ERROR " + e.getMessage());
                break;
            }
        }
    }

    void sendToStream(final String messageData) {
        redisTemplate.opsForStream().add(
                StreamRecords.newRecord()
                        .in("messages:processed")
                        .ofObject(messageData));
    }

    @PreDestroy
    void destroy() {
        this.isConsuming = false;
        if (timer != null)
            timer.cancel();
    }
}
