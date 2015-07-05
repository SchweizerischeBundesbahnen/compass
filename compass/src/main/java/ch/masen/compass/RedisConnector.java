package ch.masen.compass;

import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by igor on 29.06.15.
 */

@Component
public class RedisConnector {
    private static JedisPool pool;

    public static JedisPool getPool() {
        if(pool != null) {
            return pool;
        } else {
            String redisHost = ConfigHelper.getConfigProperties().getProperty("REDIS");
            pool = new JedisPool(new JedisPoolConfig(), redisHost);

            for (final String name : RedirectsHelper.getRedirects().stringPropertyNames()) {
                Jedis redis = getPool().getResource();
                redis.set(name, RedirectsHelper.getRedirects().getProperty(name));
                pool.returnResource(redis);
            }

            return pool;
        }
    }
}
