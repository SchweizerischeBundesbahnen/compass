package ch.sbb.compass.redis;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.sbb.compass.util.exception.KeyAlreadyExistsException;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * The data access object for the short link database.
 *
 * @author Igor Masen
 * @since 08.07.15
 */
@Component
public class ShortLinkDAO {

    private static final Logger log = Logger.getLogger(ShortLinkDAO.class.getSimpleName());
    private static final String REDIRECT_COUNTER_PREFIX = "RC.";

    @Autowired
    private JedisPool redisPool;

    /**
     * Adds a short link to the database.
     *
     * @param shortLink The short link object to put into the database.
     * @throws KeyAlreadyExistsException If the id already exists in the database.
     */
    public void addShortLink(ShortLinkDTO shortLink) throws KeyAlreadyExistsException {
        if (shortLink == null || StringUtils.isEmpty(shortLink.getId()) || shortLink.getDestUrl() == null) {
            return;
        }

        try(Jedis redis = redisPool.getResource()) {

            if (redis.get(shortLink.getId()) != null) {
                throw new KeyAlreadyExistsException(shortLink.getId());
            } else {
                redis.set(shortLink.getId(), shortLink.getDestUrl().toString());
            }

            String redirectCounterKey = REDIRECT_COUNTER_PREFIX + shortLink.getId();

            if (redis.get(redirectCounterKey) != null) {
                throw new KeyAlreadyExistsException(shortLink.getId());
            } else {
                redis.set(redirectCounterKey, "0");
            }
        }
    }

    public Integer getRedirectCounter(ShortLinkDTO shortLink) {
        Jedis redis = redisPool.getResource();
        Integer count = Integer.valueOf(redis.get(REDIRECT_COUNTER_PREFIX + shortLink.getId()));
        redis.close();
        return count;
    }

    public void incrementRedirectCounter(ShortLinkDTO shortLink) {
        Jedis redis = redisPool.getResource();
        redis.incr(REDIRECT_COUNTER_PREFIX + shortLink.getId());
        redis.close();
    }

    public void deleteShortLink(String id) {
        Jedis redis = redisPool.getResource();
        redis.del(id);
        redis.del(REDIRECT_COUNTER_PREFIX + id);
        redis.close();
    }

    public ShortLinkDTO getShortLink(String key) {
        Jedis redis = redisPool.getResource();
        ShortLinkDTO shortLink = null;
        if (redis.get(key) != null) {
            try {
                shortLink = new ShortLinkDTO(key, new URL(redis.get(key)));
                shortLink.setRedirectCount(getRedirectCounter(shortLink));
            } catch (MalformedURLException e) {
                log.warning("Could not read destUrl from db " + e.getMessage());
            }
        }
        redis.close();

        return shortLink;
    }

    public List<ShortLinkDTO> getAllShortLinks() {
        Jedis redis = redisPool.getResource();
        List<ShortLinkDTO> shortLinks = new ArrayList<>();
        Set<String> keys = redis.keys("*");

        shortLinks.addAll(
                keys.stream()
                        .filter(key -> !key.contains("RC"))
                        .map(this::getShortLink)
                        .collect(Collectors.toList()));

        redis.close();
        return shortLinks;
    }

    public void flushDb() {
        Jedis redis = redisPool.getResource();

        redis.flushDB();
        log.info("Database flushed");

        redis.close();
    }
}