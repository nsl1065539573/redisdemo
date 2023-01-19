package redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisCacheProvider implements IRedisCacheProvider {
  private final JedisPool jedisPool;

  public RedisCacheProvider(JedisPool jedisPool) {
    this.jedisPool = jedisPool;
  }


  @Override
  public Long setNx(String key, String value) {
    try (Jedis jedis = jedisPool.getResource()) {
      return jedis.setnx(key, value);
    }
  }

  @Override
  public Long setNxWithExpired(String key, String value, int expired) {
    try (Jedis jedis = jedisPool.getResource()) {
      String res = jedis.set(key, value, "NX", "EX", expired);
      if ("OK".equals(res)) {
        return 1L;
      } else {
        return 0L;
      }
    }
  }

  @Override
  public Object eval(String script, int keyCount, String... params) {
    try (Jedis jedis = jedisPool.getResource()) {
      return jedis.eval(script, keyCount, params);
    }
  }
}
