import uuid
import redis
import time
import random
from datetime import datetime, timedelta

redis_host = "redis"
redis_port = 6379
target_duration = timedelta(seconds=10)
batch_size = 10000

def publisher():
    try:
        connection = redis.Redis(host=redis_host, port=redis_port, decode_responses=True)
    except redis.ConnectionError:
        print("Error: Failed to connect to Redis server")
        exit(1)

    start_time = datetime.now()
    total_messages = 0

    try:
        while datetime.now() - start_time < target_duration:
            p = connection.pipeline()
            for _ in range(batch_size):
                message = f'{{"message_id": "{str(uuid.uuid4())}"}}'
                p.publish("messages:published", message)
            p.execute()
            total_messages += batch_size
            time.sleep(random.uniform(0.1, 0.5))
    except Exception as e:
        print(f"Error: {e}")
    finally:
        connection.publish("messages:published", '{"message_id": "END"}')
        print(f"Total messages published: {total_messages}")

if __name__ == "__main__":
    publisher()