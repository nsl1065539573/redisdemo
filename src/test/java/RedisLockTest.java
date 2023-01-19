import mock.MockRedisLock;
import redis.AbstractRedisLock;
import redis.IRedisCacheProvider;
import redis.RedisCacheProvider;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class RedisLockTest {
  public static void main(String[] args) {
    JedisPool jedisPool = new JedisPool("localhost", 6379);
    IRedisCacheProvider redisCacheProvider = new RedisCacheProvider(jedisPool);
    AbstractRedisLock redisLock = new MockRedisLock(redisCacheProvider, "test", 100L, 60, 60000L);
    redisLock.lockAndRun("test", () -> {
      System.out.println("lock and run");
    });
    ExecutorService executorService = new ThreadPoolExecutor(5, 5, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
    List<Integer> list = new ArrayList<>();
    list.add(0);
    List<Future<Boolean>> futures = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      futures.add(executorService.submit(() -> {
        for (int j = 0; j < 100; j++) {
          redisLock.lockAndRun("test", () -> {
            list.set(0, list.get(0) + 1);
          });
        }
        return Boolean.TRUE;
      }));
    }

    for (Future<Boolean> future : futures) {
      try {
        future.get();
      } catch (Exception e) {
        return;
      }
    }
    if (list.get(0) != 1000) {
      throw new RuntimeException("should be 1000");
    }
    executorService.shutdown();
  }
}
