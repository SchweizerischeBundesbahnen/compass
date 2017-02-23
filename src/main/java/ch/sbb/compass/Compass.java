package ch.sbb.compass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.PropertyResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

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

    /**
     * @return a WebMvcConfigurer object with a global CORS configuration
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // map to all paths, allowing all origins
                registry.addMapping("/**").allowedMethods("GET","POST","DELETE");
            }
        };
    }

    /** Starts the spring boot application. */
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Compass.class, args);
    }
}