package mock;

import redis.AbstractRedisLock;
import redis.IRedisCacheProvider;

public class MockRedisLock extends AbstractRedisLock {
  public MockRedisLock(IRedisCacheProvider redisCacheProvider, String prefix, long waitInterval, int expireTime, long timeoutTime) {
    super(redisCacheProvider, prefix, waitInterval, expireTime, timeoutTime);
  }
}
