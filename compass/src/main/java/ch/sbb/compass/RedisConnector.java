package ch.sbb.compass;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by igor on 29.06.15.
 */
@Component
public class RedisConnector {

    private static final Logger log = Logger.getLogger(RedisConnector.class.getSimpleName());

    private JedisPool pool;

    @Autowired
    private PropertiesHelper propertiesHelper;

    public JedisPool getPool() {
        if (pool != null) {

            return pool;
        } else {
            final String redisHost = propertiesHelper.getRedisHost();
            if (redisHost != null) {
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