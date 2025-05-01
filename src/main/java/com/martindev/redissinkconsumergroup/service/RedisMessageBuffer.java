package com.martindev.redissinkconsumergroup.service;

import lombok.Getter;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class RedisMessageBuffer implements MessageListener {
    @Getter
    private static final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    //TODO we can add here executor as well if we want to scale the push to the inner buffer

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String msg = new String(message.getBody());
        try {
            messageQueue.put(msg);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("ERROR + " + e.getMessage());
        }
    }
}
