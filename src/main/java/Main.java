import redis.clients.jedis.Jedis;

/**
 * @Author: nansongling
 * @Date: 2022/12/20 12:19 PM
 **/
public class Main {
  public static void main(String[] args) {
    Jedis jedis = new Jedis("localhost", 6379);
    System.out.println(jedis.ping());
    jedis.close();
  }
}
