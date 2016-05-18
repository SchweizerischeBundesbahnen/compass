package ch.masen.compass;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by igor on 08.07.15.
 */
public class SubdomainRedirectDAO {

    private ArrayList<SubdomainRedirectDTO> redirects;
    private Logger log = Logger.getLogger("ch.masen.compass.ShortLinkDAO");
    private JedisPool redisPool;
    private String SUBDOMAIN_REDIRECT_PREFIX = "SDR.";

    public SubdomainRedirectDAO() {
        redisPool = RedisConnector.getPool();
    }

    public void addShortLink(SubdomainRedirectDTO redirect) {
        Jedis redis = redisPool.getResource();

        // add new redirect to database
        if (redis.get(String.valueOf(redirect.getSrcUrl())) != null) {
            redisPool.returnResource(redis);
            throw new IllegalArgumentException("ShortLink with key " + redirect.getSrcUrl() + " already exists");
        } else {
            redis.set(String.valueOf(redirect.getSrcUrl()), redirect.getDestUrl().toString());
        }

        // add redirectCounter to database
        String rcKey = SUBDOMAIN_REDIRECT_PREFIX + redirect.getSrcUrl();

        if (redis.get(rcKey) != null) {
            redisPool.returnResource(redis);
            throw new IllegalArgumentException("ShortLink counter with key " + redirect.getSrcUrl() + " already exists");
        } else {
            redis.set(rcKey, "0");
        }

        redisPool.returnResource(redis);

    }

    public Integer getRedirectCounter(SubdomainRedirectDTO redirect) {
        Jedis redis = redisPool.getResource();
        Integer count = Integer.valueOf(redis.get(SUBDOMAIN_REDIRECT_PREFIX + redirect.getSrcUrl()));

        redisPool.returnResource(redis);
        return count;
    }

    public void incrementRedirectCounter(SubdomainRedirectDTO redirect) {
        Jedis redis = redisPool.getResource();
        redis.incr(SUBDOMAIN_REDIRECT_PREFIX + redirect.getSrcUrl());

        redisPool.returnResource(redis);
    }

    public void deleteShortLink(SubdomainRedirectDTO redirect) {
        Jedis redis = redisPool.getResource();
        redis.del(String.valueOf(redirect.getSrcUrl()));
        redis.del(SUBDOMAIN_REDIRECT_PREFIX + redirect.getSrcUrl());

        redisPool.returnResource(redis);
    }

    public void deleteShortLink(String id) {
        Jedis redis = redisPool.getResource();
        redis.del(id);
        redis.del(SUBDOMAIN_REDIRECT_PREFIX + id);

        redisPool.returnResource(redis);
    }

    public SubdomainRedirectDTO getRedirect(String key) throws MalformedURLException {
        Jedis redis = redisPool.getResource();
        SubdomainRedirectDTO shortLink = null;

        if (redis.get(key) != null) {
            shortLink = new SubdomainRedirectDTO();
            shortLink.setSrcUrl(new URL(key));
            shortLink.setRedirectCount(getRedirectCounter(shortLink));
            try {
                shortLink.setDestUrl(new URL(redis.get(key)));
            } catch (MalformedURLException e) {
                log.info("Could not read destUrl from db " + e.getMessage());
            }
        }

        redisPool.returnResource(redis);
        return shortLink;
    }

    public ArrayList<SubdomainRedirectDTO> getAllShortLinks() throws MalformedURLException {
        redirects = new ArrayList<SubdomainRedirectDTO>();
        Jedis redis = redisPool.getResource();

        Set<String> keys = redis.keys("*");

        for (String key : keys) {
            if (!key.contains("RC")) {
                redirects.add(getRedirect(key));
            }
        }

        redisPool.returnResource(redis);
        return redirects;
    }

    public void flushDb() {
        Jedis redis = redisPool.getResource();

        redis.flushDB();
        log.info("Database flushed");

        redisPool.returnResource(redis);
    }

}
