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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

@SpringBootApplication
@RestController
@EnableScheduling
public class Compass {

    private Logger log = Logger.getLogger("ch.masen.compass.Compass");
    private JedisPool redisPool = RedisConnector.getPool();

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Compass.class, args);
    }

    @RequestMapping(value = "/**", method = RequestMethod.GET)
    void home(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        URL baseUrl = new URL(ConfigHelper.getConfigProperties().getProperty("BASEURL"));
        String baseHostAndPort = baseUrl.getHost() + baseUrl.getPort();
        String requestedHostAndPort = httpServletRequest.getRemoteHost() + httpServletRequest.getRemotePort();

        if (!baseHostAndPort.equalsIgnoreCase(requestedHostAndPort)) {
            String requestUrl = httpServletRequest.getRequestURL().toString();
            Jedis redis = redisPool.getResource();
            String redirectUrl = redis.get(requestUrl);
            redisPool.returnResource(redis);

            if (redirectUrl != null) {
                httpServletResponse.setStatus(302);
                httpServletResponse.setHeader("Location", redirectUrl);
                log.info("Redirect: " + redirectUrl + " " + redirectUrl);
            } else {
                httpServletResponse.sendError(404, "Not found");
            }
        }

    }

    @RequestMapping(value = "/redirect/**", method = RequestMethod.GET)
    void redirect(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
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

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    void create(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        Jedis redis = redisPool.getResource();

        String src = httpServletRequest.getParameter("src");
        String dest = httpServletRequest.getParameter("dest");
        if (src != null && dest != null) {
            redis.set(src, dest);
            log.info("Created: " + src + " " + dest);
        } else {
            httpServletResponse.sendError(400, "Bad request");
        }
        redisPool.returnResource(redis);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
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

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    Set<String> list() throws IOException {
        Jedis redis = redisPool.getResource();

        Set<String> keys = redis.keys("*");
        redisPool.returnResource(redis);
        return keys;
    }

    @RequestMapping(value = "/dump", method = RequestMethod.GET)
    void dump() throws IOException {
        Jedis redis = redisPool.getResource();

        StringBuilder dbContent = new StringBuilder();

        Set<String> keys = list();
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

    @RequestMapping(value = "/flushall", method = RequestMethod.GET)
    void flushall() {
        Jedis redis = redisPool.getResource();

        redis.flushDB();
        log.info("Database flushed");
        redisPool.returnResource(redis);
    }

}
