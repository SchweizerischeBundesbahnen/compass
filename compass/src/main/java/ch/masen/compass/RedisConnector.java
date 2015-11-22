package ch.masen.compass;

import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.logging.Logger;

/**
 * Created by igor on 29.06.15.
 */

@Component
public class RedisConnector {
    private static JedisPool pool;

    private static Logger log = Logger.getLogger("ch.masen.compass.RedisConnector");


    public static JedisPool getPool() {
        if (pool != null) {

            return pool;
        } else {
            String redisHost = ConfigHelper.getConfigProperties().getProperty("REDIS");
            if(redisHost != null) {
                JedisPoolConfig config = new JedisPoolConfig();
                config.setMaxTotal(100);
                pool = new JedisPool(config, redisHost);

            } else {
                log.warning("Could not read hostname for redis host");

            }

        }
        return pool;
    }
}
