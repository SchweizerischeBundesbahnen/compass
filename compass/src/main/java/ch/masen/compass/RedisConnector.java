package ch.masen.compass;

import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by igor on 29.06.15.
 */

@Component
public class RedisConnector {
    private static JedisPool pool;

    public static JedisPool getPool() {
        if (pool != null) {

            return pool;
        } else {
            String redisHost = ConfigHelper.getConfigProperties().getProperty("REDIS");
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(100);
            pool = new JedisPool(config, redisHost);

            return pool;
        }
    }
}
