package ch.sbb.compass;

import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

@SpringBootApplication
@RestController
@EnableScheduling
public class Compass {

    private Logger log = Logger.getLogger("Compass");
    @Autowired
    private ShortLinkDAO shortLinkDAO;
    private Gson gson = new Gson();

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Compass.class, args);
    }

    // Redirect shortlinks
    @RequestMapping(value = "/x/**", method = RequestMethod.GET)
    void redirectShortLink(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        Long startTime = System.currentTimeMillis();
        String requestUri = httpServletRequest.getRequestURI();
        String redirectKey = requestUri.split("/")[2];
        ShortLinkDTO redirect = shortLinkDAO.getShortLink(redirectKey);

        if (redirect != null) {
            String redirectUrl = redirect.getDestUrl().toString();
            httpServletResponse.setStatus(302);
            httpServletResponse.setHeader("Location", redirectUrl);
            shortLinkDAO.incrementRedirectCounter(redirect);
            log.info("Redirect: " + redirectKey + " " + redirectUrl + " took " + (System.currentTimeMillis() - startTime) + " ms");
        } else {
            httpServletResponse.sendError(404, "Not found");
        }

    }

    @CrossOrigin
    @RequestMapping(value = "/rest/1.0/shortlink/create", method = RequestMethod.POST)
    String create(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        Long startTime = System.currentTimeMillis();
        String dest = httpServletRequest.getParameter("dest");
        String id;
        ShortLinkDTO shortLink = null;

        if (dest != null) {
            try {
                // check if dest is a valid url
                URL url = new URL(dest);

                // create short link
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.reset();
                md.update(dest.getBytes());
                byte[] digest = md.digest();
                BigInteger bigInteger = new BigInteger(1, digest);
                String idLong = bigInteger.toString(16);
                // take only the first 8 chars
                id = idLong.substring(0, 8);

                // set the short link and dest into db
                shortLink = new ShortLinkDTO(id, new URL(dest));
                shortLinkDAO.addShortLink(shortLink);
                log.info("Created: " + shortLink.getId() + " " + shortLink.getDestUrl() + " took " + (System.currentTimeMillis() - startTime) + " ms");

            } catch (IllegalArgumentException e) {
                if (e.getMessage().contains("already exists")) {
                    if(shortLink.getId().equals(shortLinkDAO.getShortLink(shortLink.getId()).getId())) {
                        log.info(e.getMessage() + " " + shortLink.getDestUrl() + " took " + (System.currentTimeMillis() - startTime) + " ms");
                    } else {
                        log.warning(e.getMessage() + " collision for key " + shortLink.getId() +
                                ". RedirectLink " + shortLink.getDestUrl() + " is not equal " +
                                shortLinkDAO.getShortLink(shortLink.getId()).getDestUrl());
                    }
                }

            } catch (NoSuchAlgorithmException e) {
                log.info("Could not find Algorithm " + e.getMessage());
            } catch (MalformedURLException e) {
                log.info("Could not create " + dest + ", message was " + e.getMessage());
                httpServletResponse.sendError(400, "Bad request: " + e.getMessage());
            }
        } else {
            httpServletResponse.sendError(400, "Bad request");
        }

        return gson.toJson(shortLink);
    }

    @RequestMapping(value = "/rest/1.0/shortlink/delete", method = RequestMethod.DELETE)
    void delete(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        String id = httpServletRequest.getParameter("id");
        if (id != null) {
            shortLinkDAO.deleteShortLink(id);
        } else {
            httpServletResponse.sendError(400, "Bad request");
        }
    }

    @RequestMapping(value = "/rest/1.0/shortlink/getall", method = RequestMethod.GET)
    String getAll() throws IOException {
        return gson.toJson(shortLinkDAO.getAllShortLinks());
    }
}