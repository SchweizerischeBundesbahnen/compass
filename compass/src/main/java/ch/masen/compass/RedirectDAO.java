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
public class RedirectDAO {

    private ArrayList<RedirectDTO> redirects;
    private Logger log = Logger.getLogger("ch.masen.compass.RedirectDAO");
    private JedisPool redisPool;
    private String REDIRECT_PREFIX = "RC.";

    public RedirectDAO() {
        redisPool = RedisConnector.getPool();
    }

    public void addRedirect(RedirectDTO redirect) {
        Jedis redis = redisPool.getResource();

        // add new redirect to database
        if (redis.get(redirect.getId()) != null) {
            redisPool.returnResource(redis);
            throw new IllegalArgumentException("Redirect with key " + redirect.getId() + " already exists");
        } else {
            redis.set(redirect.getId(), redirect.getDestUrl().toString());
        }

        // add redirectCounter to database
        String rcKey = REDIRECT_PREFIX + redirect.getId();

        if (redis.get(rcKey) != null) {
            redisPool.returnResource(redis);
            throw new IllegalArgumentException("Redirect counter with key " + redirect.getId() + " already exists");
        } else {
            redis.set(rcKey, "0");
        }

        redisPool.returnResource(redis);

    }

    public Integer getRedirectCounter(RedirectDTO redirect) {
        Jedis redis = redisPool.getResource();
        Integer count = Integer.valueOf(redis.get(REDIRECT_PREFIX + redirect.getId()));

        redisPool.returnResource(redis);
        return count;
    }

    public void incrementRedirectCounter(RedirectDTO redirect) {
        Jedis redis = redisPool.getResource();
        redis.incr(REDIRECT_PREFIX + redirect.getId());

        redisPool.returnResource(redis);
    }

    public void deleteRedirect(RedirectDTO redirect) {
        Jedis redis = redisPool.getResource();
        redis.del(redirect.getId());
        redis.del(REDIRECT_PREFIX + redirect.getId());

        redisPool.returnResource(redis);
    }

    public void deleteRedirect(String id) {
        Jedis redis = redisPool.getResource();
        redis.del(id);
        redis.del(REDIRECT_PREFIX + id);

        redisPool.returnResource(redis);
    }

    public RedirectDTO getRedirect(String key) {
        Jedis redis = redisPool.getResource();
        RedirectDTO redirect = null;

        if (redis.get(key) != null) {
            redirect = new RedirectDTO();
            redirect.setId(key);
            redirect.setRedirectCount(getRedirectCounter(redirect));
            try {
                redirect.setDestUrl(new URL(redis.get(key)));
            } catch (MalformedURLException e) {
                log.info("Could not read destUrl from db " + e.getMessage());
            }
        }

        redisPool.returnResource(redis);
        return redirect;
    }

    public ArrayList<RedirectDTO> getAllRedirects() {
        redirects = new ArrayList<RedirectDTO>();
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
