package ch.sbb.compass;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by igor on 08.07.15.
 */
@Component
public class ShortLinkDAO {

    private ArrayList<ShortLinkDTO> shortLinks;
    private Logger log = Logger.getLogger("ShortLinkDAO");
    private JedisPool redisPool;
    private String SHORTLINK_PREFIX = "RC.";

    @Autowired
    private RedisConnector redisConnector;

    public void addShortLink(ShortLinkDTO shortLink) {
        Jedis redis = redisPool.getResource();

        // add new shortLink to database
        if (redis.get(shortLink.getId()) != null) {
            redisPool.returnResource(redis);
            throw new IllegalArgumentException("ShortLink with key " + shortLink.getId() + " already exists");
        } else {
            redis.set(shortLink.getId(), shortLink.getDestUrl().toString());
        }

        // add redirectCounter to database
        String rcKey = SHORTLINK_PREFIX + shortLink.getId();

        if (redis.get(rcKey) != null) {
            redisPool.returnResource(redis);
            throw new IllegalArgumentException("ShortLink counter with key " + shortLink.getId() + " already exists");
        } else {
            redis.set(rcKey, "0");
        }

        redisPool.returnResource(redis);

    }

    public Integer getRedirectCounter(ShortLinkDTO shortLink) {
        Jedis redis = redisPool.getResource();
        Integer count = Integer.valueOf(redis.get(SHORTLINK_PREFIX + shortLink.getId()));

        redisPool.returnResource(redis);
        return count;
    }

    public void incrementRedirectCounter(ShortLinkDTO shortLink) {
        Jedis redis = redisPool.getResource();
        redis.incr(SHORTLINK_PREFIX + shortLink.getId());

        redisPool.returnResource(redis);
    }

    public void deleteShortLink(ShortLinkDTO shortLink) {
        Jedis redis = redisPool.getResource();
        redis.del(shortLink.getId());
        redis.del(SHORTLINK_PREFIX + shortLink.getId());

        redisPool.returnResource(redis);
    }

    public void deleteShortLink(String id) {
        Jedis redis = redisPool.getResource();
        redis.del(id);
        redis.del(SHORTLINK_PREFIX + id);

        redisPool.returnResource(redis);
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

        redisPool.returnResource(redis);
        return shortLink;
    }

    public ArrayList<ShortLinkDTO> getAllShortLinks() {
        shortLinks = new ArrayList<ShortLinkDTO>();
        Jedis redis = redisPool.getResource();

        Set<String> keys = redis.keys("*");

        for (String key : keys) {
            if (!key.contains("RC")) {
                shortLinks.add(getShortLink(key));
            }
        }

        redisPool.returnResource(redis);
        return shortLinks;
    }

    public void flushDb() {
        Jedis redis = redisPool.getResource();

        redis.flushDB();
        log.info("Database flushed");

        redisPool.returnResource(redis);
    }

    @PostConstruct
    private void init() {
        redisPool = redisConnector.getPool();
    }
}
