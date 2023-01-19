package redis;

/**
 * 封装redis操作，不给客户端提供过多权限
 **/
public interface IRedisCacheProvider {
  Long setNx(String key, String value);

  Long setNxWithExpired(String key, String value, int expired);

  Object eval(String script, int keyCount, String... params);
}
