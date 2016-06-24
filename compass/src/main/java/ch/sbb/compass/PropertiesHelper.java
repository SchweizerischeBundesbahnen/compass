package ch.sbb.compass;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PropertiesHelper {

    private static final Logger log = Logger.getLogger("PropertiesHelper");

    @Value("${REDIS_HOST}")
    private String REDIS_HOST;

    public String getRedisHost() {
        return REDIS_HOST;
    }
}