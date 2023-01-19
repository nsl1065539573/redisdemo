package redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.UUID;
import java.util.concurrent.Callable;

public abstract class AbstractRedisLock {
  private static final String UNLOCK_SCRIPT =  "if redis.call(\"get\",KEYS[1])==ARGV[1]\n" +
      "then\n" +
      "    return redis.call(\"del\",KEYS[1])\n" +
      "else\n" +
      "    return 0\n" +
      "end";

  private final IRedisCacheProvider redisCacheProvider;
  private final String prefix;
  private final long waitInterval;
  private final int expireTime;
  private final long timeoutTime;

  public AbstractRedisLock(IRedisCacheProvider redisCacheProvider, String prefix, long waitInterval, int expireTime, long timeoutTime) {
    this.redisCacheProvider = redisCacheProvider;
    this.prefix = prefix;
    this.waitInterval = waitInterval;
    this.expireTime = expireTime;
    this.timeoutTime = timeoutTime;
  }

  public <T> T lockAndResult(Object suffix, Callable<T> callable) {
    try (RedisLock redisLock = lockWithWait(suffix)) {
      return callable.call();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void lockAndRun(Object suffix, Runnable runnable) {
    try (RedisLock redisLock = lockWithWait(suffix)) {
      runnable.run();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected String genKey(Object suffix) {
    return this.prefix + "_" + suffix.toString();
  }

  private RedisLock lockWithWait(Object suffix) {
    String token  = UUID.randomUUID().toString();
    String key = genKey(suffix);
    long time = this.timeoutTime;
    while (this.redisCacheProvider.setNxWithExpired(key, token, this.expireTime) != 1L) {
      if (time < 0L) {
        throw new RuntimeException("lock timeout...");
      }
      try {
        Thread.sleep(this.waitInterval);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      time -= this.waitInterval;
    }
    return new RedisLock(key, token, this);
  }

  private void unLock(RedisLock redisLock) {
    this.unLock(redisLock.key, redisLock.token);
  }

  private void unLock(String key, String token) {
    Long res = (Long) this.redisCacheProvider.eval(UNLOCK_SCRIPT, 1, key, token);
    if (res == 0) {
      throw new RuntimeException("unlock failed");
    }
  }

  public static class RedisLock implements AutoCloseable {
    private final String key;
    private final String token;
    private final AbstractRedisLock redisLock;

    public RedisLock(String key, String token, AbstractRedisLock redisLock) {
      this.key = key;
      this.token = token;
      this.redisLock = redisLock;
    }

    @Override
    public void close() {
      this.redisLock.unLock(this);
    }
  }
}
