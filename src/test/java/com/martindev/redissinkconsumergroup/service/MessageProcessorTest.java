package com.martindev.redissinkconsumergroup.service;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

class MessageProcessorTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ListOperations<String, String> listOps;

    @Mock
    private ConsumerGroup consumerGroup;

    private MessageProcessor messageProcessor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        messageProcessor = new MessageProcessor(consumerGroup);

        when(redisTemplate.opsForList()).thenReturn(listOps);
    }

    @Test
    void testInit() {
        messageProcessor.init();

        verify(consumerGroup).setName("redis-consumer-group");
        verify(consumerGroup).initializeConsumers();
        verify(consumerGroup).start();
    }
}
