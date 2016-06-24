package ch.sbb.compass.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This helper class holds all application specific properties.
 * 
 * @author Igor Masen
 * @author Kerem Adigüzel
 * @since 24.06.16
 */
@Component
public class PropertiesHelper {

    @Value("${redis.host}")
    private String redisHost;
    @Value("${base.url}")
    private String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getRedisHost() {
        return redisHost;
    }
}