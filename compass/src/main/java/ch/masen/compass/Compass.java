package ch.masen.compass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

@SpringBootApplication
@RestController
@EnableScheduling
public class Compass {

    private Logger log = Logger.getLogger("ch.masen.compass.Compass");
    private JedisPool redisPool = RedisConnector.getPool();
    private RedirectDAO rdao = new RedirectDAO();

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Compass.class, args);
    }

    @RequestMapping(value = "/x/**", method = RequestMethod.GET)
    void redirectShortLink(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        Jedis redis = redisPool.getResource();
        String requestUri = httpServletRequest.getRequestURI();
        String redirectKey = requestUri.split("/")[2];
        String redirectUrl = redis.get(redirectKey);

        if (redirectUrl != null) {
            httpServletResponse.setStatus(302);
            httpServletResponse.setHeader("Location", redirectUrl);
            log.info("Redirect: " + redirectKey + " " + redirectUrl);
        } else {
            httpServletResponse.sendError(404, "Not found");
        }

        redisPool.returnResource(redis);
    }

    @RequestMapping(value = "/rest/1.0/create", method = RequestMethod.POST)
    String create(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        Jedis redis = redisPool.getResource();

        String dest = httpServletRequest.getParameter("dest");
        String src = null;
        if (dest != null) {
            try {
                // check if dest is a valid url
                URL url = new URL(dest);

                //create short link
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.reset();
                md.update(dest.getBytes());
                byte[] digest = md.digest();
                BigInteger bigInteger = new BigInteger(1, digest);
                String srcLong = bigInteger.toString(16);
                //take only the first 8 chars
                src = srcLong.substring(0, 8);

                // set the short link and dest into db
                redis.set(src, dest);
                log.info("Created: " + src + " " + dest);

            } catch (NoSuchAlgorithmException e) {
                log.info("Could not find Algorithm " + e.getMessage());
            } catch (MalformedURLException e) {
                log.info("Could not create " + dest + ", message was " + e.getMessage());
                httpServletResponse.sendError(400, "Bad request: " + e.getMessage());
            } finally {
                redisPool.returnResource(redis);
            }
        } else {
            httpServletResponse.sendError(400, "Bad request");
        }
        return src;
    }

    @RequestMapping(value = "/rest/1.0/delete", method = RequestMethod.POST)
    void delete(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        Jedis redis = redisPool.getResource();
        String src = httpServletRequest.getParameter("src");
        if (src != null) {
            redis.del(src);
        } else {
            httpServletResponse.sendError(400, "Bad request");
        }
        redisPool.returnResource(redis);
    }

    @RequestMapping(value = "/rest/1.0/listall", method = RequestMethod.GET)
    Set<String> listAll() throws IOException {
        Jedis redis = redisPool.getResource();

        Set<String> keys = redis.keys("*");
        redisPool.returnResource(redis);
        return keys;
    }

    @RequestMapping(value = "/rest/1.0/dump", method = RequestMethod.GET)
    void dump() throws IOException {
        Jedis redis = redisPool.getResource();

        StringBuilder dbContent = new StringBuilder();

        Set<String> keys = listAll();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String key = it.next();
            String value = redis.get(key);
            dbContent.append(key + " = " + value + "\n");
        }

        Path redirectsFilePath = RedirectsHelper.getRedirectsFile().toPath();

        Files.write(redirectsFilePath, dbContent.toString().getBytes());

        log.info("Redis dumped to " + redirectsFilePath);
        redisPool.returnResource(redis);

    }

    @RequestMapping(value = "/rest/1.0/flushall", method = RequestMethod.GET)
    void flushall() {
        rdao.flushDb();
    }

}
