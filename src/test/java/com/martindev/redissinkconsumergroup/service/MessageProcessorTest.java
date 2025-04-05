package com.martindev.redissinkconsumergroup.service;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;

public class MessageProcessorTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private BeanFactory beanFactory;

    @Mock
    private ConsumerWorker consumerWorker;

    @Mock
    private ExecutorService executorService;

    @Mock
    private ListOperations<String, String> listOps;

    private MessageProcessor messageProcessor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        messageProcessor = new MessageProcessor(5, redisTemplate, beanFactory);

        when(redisTemplate.opsForList()).thenReturn(listOps);

        when(listOps.rightPush(anyString(), anyString())).thenReturn(1L);

        when(beanFactory.getBean(ConsumerWorker.class)).thenReturn(consumerWorker);
    }

    @Test
    void testInit() {
        messageProcessor.init();

        verify(listOps, times(5)).rightPush(eq("consumer:ids"), anyString());

        assertEquals(5, messageProcessor.getConsumerGroup().size());
    }

    @Test
    void testConsumerWorkerIsAssignedCorrectly() {
        when(beanFactory.getBean(ConsumerWorker.class)).thenReturn(consumerWorker);

        messageProcessor.init();

        verify(consumerWorker, times(5)).setConsumerId(anyString());
    }
}
