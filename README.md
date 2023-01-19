### redisdemo
搞一些redis的使用操作进来，自己学习用

#### redisLock
基于redis的分布式锁，
如果程序没有获取到锁则会同步等待。
入口在`AbstractRedisLock`，通过封装的`IRedisCacheProvider`进行操作，封装要用到的操作，避免线上不安全的操作暴露给调用者。

##### 使用demo
基于springboot的使用形式，可将`AbstractRedisLock`类的子类声明为组件使用
```java
import redis.AbstractRedisLock;
import redis.IRedisCacheProvider;

@Compent
public class A extends AbstractRedisLock {
  @Autowirted
  public A(IRedisCacheProvider redisCacheProvider, String prefix, long waitInterval, int expireTime, long timeoutTime) {
    super(redisCacheProvider, prefix, waitInterval, expireTime, timeoutTime);
  }
}
```
