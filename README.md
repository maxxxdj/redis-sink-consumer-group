# RedisSinkConsumerGroup

## Overview

`RedisSinkConsumerGroup` is a Java application that demonstrates message processing with **Redis**, **virtual threads**, and **metrics reporting**. It uses **Spring Boot** to manage consumer workers that process messages from a Redis queue.

---

## Features

- **Virtual Threads**: Efficient concurrent processing with Java 21 virtual threads.
- **Redis Integration**: Uses Redis for message queuing.
- **Metrics Reporting**: Reports processed messages and consumer status.

---

## Setup

### 1. Docker Compose

Use **Docker Compose** to start the required services:

```bash
docker-compose up
```

This will start:

Redis: A Redis instance.

Redis Commander: A web UI to interact with Redis - http://localhost:8081.

Redis Publisher: A Python service that publishes messages to a Redis channel.
It has a 15sec delay startup period so the Java service is already listening to Redis channel.

Redis Sink Consumer Group: A Java service that consumes messages from Redis in parallel using virtual threads. 
This is where your workers (consumers) are managed.

--------
Override Group Members: To modify the number of consumer workers (group members), update the consumer.threadPoolSize environment variable in the docker-compose.yml for redis-sink-consumer-group.
For example:
```
  redis-sink-consumer-group:
    environment:
      - consumer.threadPoolSize=10 

```

run docker-compose again to perform with 10 consumers


## LOCAL CONFIGURATION

Adjust application.properties for settings:

# application.properties
consumer.threadPoolSize=5 (number of consumers)
redis.host=localhost
redis.port=6379

### Usage
Example Output:
```
Messages processed: 15
Consumer: myConsumerTask-0, consuming: true
Consumer: myConsumerTask-1, consuming: true
Consumer: myConsumerTask-2, consuming: false
...
```

