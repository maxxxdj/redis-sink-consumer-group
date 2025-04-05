package com.martindev.redissinkconsumergroup.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.connection.Message;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

class RedisMessageConsumerTest {

    private RedisMessageConsumer redisMessageConsumer;

    @BeforeEach
    void setUp() {
        redisMessageConsumer = new RedisMessageConsumer();
    }

    @Test
    void testOnMessageAddsToQueue() throws InterruptedException {
        String testMessage = "Test123";
        Message message = Mockito.mock(Message.class);
        Mockito.when(message.getBody()).thenReturn(testMessage.getBytes(StandardCharsets.UTF_8));

        redisMessageConsumer.onMessage(message, null);

        BlockingQueue<String> queue = RedisMessageConsumer.getMessageQueue();
        assertFalse(queue.isEmpty());
        assertEquals(testMessage, queue.take());
    }
}