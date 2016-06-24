package ch.sbb.compass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import org.springframework.core.env.PropertyResolver;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@SpringBootApplication
public class Compass {

    /** The maximum number of pools allowed to be created. */
    private static final int MAX_TOTAL_POOLS = 100;

    /**
     * Configures the {@link JedisPool} bean used in the components of this application.
     *
     * @param propertyResolver The autowired dependency used to configure the pool.
     * @return a preconfigured {@link JedisPool} object
     */
    @Bean
    public JedisPool getJedisPool(PropertyResolver propertyResolver) {
        final String redisHost = propertyResolver.getProperty("redis.host");
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(MAX_TOTAL_POOLS);
        return new JedisPool(config, redisHost);
    }

    /** Starts the spring boot application. */
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Compass.class, args);
    }
}